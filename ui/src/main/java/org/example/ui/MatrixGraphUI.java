package org.example.ui;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import org.example.core.Dijkstra;
import org.example.core.Edge;
import org.example.core.MatrixGraph;
import org.example.core.TraversalStep;

import java.io.*;
import java.util.*;

public class MatrixGraphUI {

    private BorderPane root;
    private Pane graphPane;
    private ScrollPane graphScrollPane;
    private Text matrixDisplay;
    private MatrixGraph graph;
    private Timeline currentAnimation;
    
    private double currentScale = 1.0;

    private Map<Integer, Circle> nodes = new HashMap<>();
    private Map<Integer, Text> nodeLabels = new HashMap<>();

    // [新增] 用于记录当前选中的节点ID (用于连线)
    private Integer selectedNodeId = null;

    private static class EdgeUI {
        Line line;
        Text label;
        EdgeUI(Line line, Text label) { this.line = line; this.label = label; }
    }

    private Map<String, EdgeUI> edges = new HashMap<>();

    public MatrixGraphUI(MatrixGraph graph) {
        this.graph = graph;
        
        root = new BorderPane();
        root.setPrefSize(1050, 600);
        
        graphPane = new Pane();
        graphPane.setPrefSize(2000, 2000); 
        graphPane.setStyle("-fx-background-color: #f8f9fa;");

        // [新增] 点击空白处取消选中
        graphPane.setOnMouseClicked(e -> {
            if (e.getTarget() == graphPane) {
                if (selectedNodeId != null) {
                    resetStyles();
                    selectedNodeId = null;
                    updateLog("已取消选中");
                }
            }
        });
        
        Group scrollContent = new Group(graphPane);
        
        graphScrollPane = new ScrollPane(scrollContent);
        graphScrollPane.setPrefSize(650, 600);
        graphScrollPane.setPannable(true); 
        graphScrollPane.setStyle("-fx-background-color: transparent; -fx-border-color: #dee2e6; -fx-border-width: 1;");
        
        Button zoomInBtn = createZoomButton("放大", 1.2);
        Button zoomOutBtn = createZoomButton("缩小", 0.8);
        
        VBox zoomControls = new VBox(10, zoomInBtn, zoomOutBtn);
        zoomControls.setAlignment(Pos.CENTER);
        zoomControls.setPadding(new Insets(20));
        zoomControls.setPickOnBounds(false);
        zoomControls.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        
        StackPane centerStack = new StackPane();
        centerStack.getChildren().addAll(graphScrollPane, zoomControls);
        StackPane.setAlignment(zoomControls, Pos.TOP_RIGHT);

        VBox matrixPane = new VBox(10);
        matrixPane.setPadding(new Insets(15));
        matrixPane.setPrefHeight(350);
        matrixPane.setStyle("-fx-background-color: #ffffff; -fx-border-color: #dee2e6; -fx-border-width: 1;");
        
        Text matrixTitle = new Text("数据与日志");
        matrixTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-fill: #2c3e50;");
        
        ScrollPane textScrollPane = new ScrollPane();
        textScrollPane.setFitToWidth(false); 
        textScrollPane.setPrefHeight(300);
        textScrollPane.setStyle("-fx-background-color: transparent; -fx-background: #ffffff;");
        
        matrixDisplay = new Text();
        matrixDisplay.setStyle("-fx-font-family: 'Monaco', 'Menlo', 'Consolas', monospace; -fx-font-size: 12px; -fx-fill: #34495e;");
        
        textScrollPane.setContent(matrixDisplay);
        matrixPane.getChildren().addAll(matrixTitle, textScrollPane);
        
        VBox rightPane = new VBox(10);
        rightPane.setPrefWidth(380); 
        rightPane.setPadding(new Insets(0, 0, 0, 10));
        rightPane.getChildren().add(matrixPane); 
        VBox.setVgrow(matrixPane, Priority.ALWAYS);
        
        root.setCenter(centerStack);
        root.setRight(rightPane);
        
        // 初始加载
        for (int i = 0; i < 5; i++) addVertex(i, -1, -1);
        applyCircularLayout();
        updateMatrixDisplay();
        centerContent(); 
    }

    // [新增] 处理节点点击事件 (选中/连线)
    private void handleNodeClick(int clickedId) {
        if (selectedNodeId == null) {
            // 1. 还没有选中起点，当前点击作为起点
            selectedNodeId = clickedId;
            Circle c = nodes.get(clickedId);
            if (c != null) c.setFill(Color.CYAN); // 变色提示
            updateLog("已选中起点: " + clickedId + "，请点击另一个节点进行连线...");
        } else {
            // 2. 已经有起点，当前点击作为终点
            if (selectedNodeId == clickedId) {
                // 如果点了自己，取消选中
                resetStyles();
                selectedNodeId = null;
                updateLog("取消选中");
            } else {
                // 弹出对话框输入权重
                TextInputDialog dialog = new TextInputDialog("1");
                dialog.setTitle("添加边");
                dialog.setHeaderText("创建边: " + selectedNodeId + " -> " + clickedId);
                dialog.setContentText("请输入权重:");

                Optional<String> result = dialog.showAndWait();
                result.ifPresent(wStr -> {
                    try {
                        int w = Integer.parseInt(wStr);
                        addEdge(selectedNodeId, clickedId, w);
                        updateLog("成功添加边: " + selectedNodeId + " -> " + clickedId + " (权重: " + w + ")");
                    } catch (NumberFormatException ex) {
                        updateLog("无效权重，操作取消");
                    }
                });
                
                // 连线完成后重置状态
                resetStyles();
                selectedNodeId = null;
            }
        }
    }

    // [新增] 更新日志显示
    private void updateLog(String msg) {
        if (msg == null || msg.isEmpty()) return;
        String currentText = matrixDisplay.getText();
        // 简单地追加在最前面，或者保留矩阵信息
        // 为了不破坏矩阵显示，我们将日志追加在矩阵下方
        // 由于 updateMatrixDisplay 会重置文本，这里我们暂时追加
        matrixDisplay.setText(graph.getMatrixString() + "\n\n[Log] " + msg);
    }

    public String getGraphDSL() {
        StringBuilder sb = new StringBuilder();
        for (Integer id : nodes.keySet()) {
            sb.append("NODE ").append(id).append("\n"); 
        }
        int n = graph.verticesNumber();
        for (int i = 0; i < n; i++) {
            if (!graph.isVertexExists(i)) continue;
            for (int j = 0; j < n; j++) {
                if (!graph.isVertexExists(j)) continue;
                int weight = graph.getEdge(i, j);
                if (weight != 0) {
                    sb.append(i).append(" -> ").append(j).append(" : ").append(weight).append("\n");
                }
            }
        }
        return sb.toString();
    }

    public void renderFromDSL(String dslText) {
        if (dslText == null || dslText.trim().isEmpty()) return;
        
        String[] lines = dslText.split("\n");
        List<int[]> edgesToAdd = new ArrayList<>();
        Map<Integer, double[]> loadedPositions = new HashMap<>();

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;
            
            if (line.equals("RESET")) {
                clearInternalGraphState();
                continue;
            }

            if (line.startsWith("DEL NODE")) {
                try {
                    int id = Integer.parseInt(line.replace("DEL NODE", "").trim());
                    removeVertex(id);
                } catch (Exception e) {}
                continue;
            }

            if (line.startsWith("DEL") && line.contains("->")) {
                try {
                    String clean = line.replace("DEL", "").trim();
                    String[] parts = clean.split("->");
                    int u = Integer.parseInt(parts[0].trim());
                    int v = Integer.parseInt(parts[1].trim());
                    removeEdge(u, v);
                } catch (Exception e) {}
                continue;
            }

            if (line.startsWith("POS")) {
                try {
                    String[] parts = line.split(" ");
                    int id = Integer.parseInt(parts[1]);
                    double x = Double.parseDouble(parts[2]);
                    double y = Double.parseDouble(parts[3]);
                    loadedPositions.put(id, new double[]{x, y});
                } catch (Exception e) {}
                continue;
            }

            if (line.contains("->") && !line.startsWith("DEL")) {
                try {
                    String[] parts = line.split("->");
                    int u = Integer.parseInt(parts[0].trim());
                    String rightPart = parts[1].trim();
                    int v; int w = 1;
                    if (rightPart.contains(":")) {
                        String[] vw = rightPart.split(":");
                        v = Integer.parseInt(vw[0].trim());
                        w = Integer.parseInt(vw[1].trim());
                    } else { v = Integer.parseInt(rightPart); }
                    
                    while (graph.verticesNumber() <= Math.max(u, v)) { graph.addVertex(); }
                    addVertex(u, -1, -1); addVertex(v, -1, -1);
                    
                    int currentWeight = graph.getEdge(u, v);
                    if (currentWeight != w) {
                        edgesToAdd.add(new int[]{u, v, w});
                    }
                } catch (Exception e) { System.out.println("DSL 解析错误: " + line); }
            }
        }
        
        if (!loadedPositions.isEmpty()) {
            for (Integer id : loadedPositions.keySet()) {
                if (!nodes.containsKey(id)) addVertex(id, -1, -1);
                Circle c = nodes.get(id);
                double[] pos = loadedPositions.get(id);
                c.setCenterX(pos[0]);
                c.setCenterY(pos[1]);
                Text t = nodeLabels.get(id);
                if(t!=null) { t.setX(pos[0]-6); t.setY(pos[1]+6); }
            }
        } else {
            if (dslText.contains("RESET") && !dslText.contains("POS")) {
                applyCircularLayout();
            }
        }

        for (int[] edge : edgesToAdd) { addEdge(edge[0], edge[1], edge[2]); }
        updateAllEdges();
        updateMatrixDisplay();
        updateLog("DSL 渲染完成");
    }

    public void centerContent() {
        Platform.runLater(() -> {
            graphScrollPane.setHvalue(0.5);
            graphScrollPane.setVvalue(0.5);
        });
    }

    private Button createZoomButton(String text, double factor) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: white; -fx-border-color: #bbb; -fx-border-radius: 5; -fx-background-radius: 5; -fx-font-size: 12px; -fx-font-weight: bold; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 3, 0, 0, 1);");
        btn.setPrefSize(50, 30);
        btn.setOnAction(e -> zoom(factor));
        return btn;
    }
    
    private void zoom(double factor) {
        double newScale = currentScale * factor;
        if (newScale >= 0.1 && newScale <= 10.0) {
            currentScale = newScale;
            
            double centerX = graphPane.getPrefWidth() / 2;
            double centerY = graphPane.getPrefHeight() / 2;
            
            for (Integer id : nodes.keySet()) {
                Circle c = nodes.get(id);
                if (c != null) {
                    double dx = c.getCenterX() - centerX;
                    double dy = c.getCenterY() - centerY;
                    double newX = centerX + dx * factor;
                    double newY = centerY + dy * factor;
                    
                    newX = Math.max(20, Math.min(graphPane.getPrefWidth() - 20, newX));
                    newY = Math.max(20, Math.min(graphPane.getPrefHeight() - 20, newY));
                    
                    c.setCenterX(newX);
                    c.setCenterY(newY);
                    Text t = nodeLabels.get(id);
                    if (t != null) { t.setX(newX - 6); t.setY(newY + 6); }
                }
            }
            updateAllEdges();
        }
    }

    public Pane getPane() { return root; }

    public void generateRandomGraph() {
        for (EdgeUI edgeUI : edges.values()) {
            graphPane.getChildren().removeAll(edgeUI.line, edgeUI.label);
        }
        edges.clear();
        graph.generateRandomGraph();
        int n = graph.verticesNumber();
        
        applyCircularLayout(); 
        
        for (int i = 0; i < n; i++) {
            if (!graph.isVertexExists(i)) continue;
            for (int j = 0; j < n; j++) {
                if (!graph.isVertexExists(j)) continue;
                int weight = graph.getEdge(i, j);
                if (weight > 0) {
                    if (i < j || (i == j)) { addEdge(i, j, weight); }
                }
            }
        }
        updateMatrixDisplay();
        updateLog("随机图生成完毕");
    }

    public void resetToDefault() {
        clearInternalGraphState();
        for (int i = 0; i < 5; i++) { addVertex(i, -1, -1); }
        applyCircularLayout();
        updateMatrixDisplay();
        updateLog("已恢复初始设置");
        centerContent();
    }

    private void clearInternalGraphState() {
        graph.clearAllEdges();
        int currentMax = graph.verticesNumber();
        for(int i=0; i<currentMax; i++) graph.setVertexExists(i, false);
        nodes.clear();
        nodeLabels.clear();
        graphPane.getChildren().clear();
        edges.clear();
    }

    // Dijkstra Methods
    public void performDijkstra(String startText, String endText) {
        resetStyles();
        stopAnimation();
        try {
            int start = Integer.parseInt(startText.trim());
            int end = Integer.parseInt(endText.trim());
            if (!graph.isVertexExists(start) || !graph.isVertexExists(end)) {
                updateLog("错误: 顶点不存在");
                return;
            }
            Dijkstra dijkstra = new Dijkstra(graph);
            List<Integer> path = dijkstra.findShortestPath(start, end);
            StringBuilder sb = new StringBuilder(graph.getMatrixString());
            sb.append("\n\n").append(dijkstra.getProcessLog());
            if (path.isEmpty() && start != end) {
                sb.append("\n结果: 无法从 ").append(start).append( " 到达 ").append(end);
            } else {
                sb.append("\n=== 最短路径结果 ===\n");
                sb.append("路径: ");
                for (int i = 0; i < path.size(); i++) { sb.append(path.get(i)).append(i < path.size() - 1 ? " -> " : ""); }
                sb.append("\n总权重: ").append(dijkstra.getShortestDistance(end));
                animatePath(path);
            }
            matrixDisplay.setText(sb.toString());
        } catch (NumberFormatException e) { updateLog("错误: 请输入有效的顶点编号"); }
    }

    public void performDijkstraAll(String startText) {
        resetStyles();
        stopAnimation();
        try {
            int start = Integer.parseInt(startText.trim());
            if (!graph.isVertexExists(start)) {
                updateLog("错误: 顶点不存在");
                return;
            }
            Dijkstra dijkstra = new Dijkstra(graph);
            dijkstra.findShortestPath(start, -1); 
            StringBuilder sb = new StringBuilder(graph.getMatrixString());
            sb.append("\n\n").append(dijkstra.getAllPathsResult(start));
            matrixDisplay.setText(sb.toString());
            animateSteps(dijkstra.getSteps());
        } catch (NumberFormatException e) {
            updateLog("错误: 请输入有效的起点ID");
        }
    }
    
    private void stopAnimation() {
        if (currentAnimation != null) {
            currentAnimation.stop();
            currentAnimation = null;
        }
    }

    private void animateSteps(List<TraversalStep> steps) {
        if (steps == null || steps.isEmpty()) return;
        currentAnimation = new Timeline();
        double delayPerStep = 800;
        Map<Integer, String> currentPathEdges = new HashMap<>();
        
        for (int i = 0; i < steps.size(); i++) {
            TraversalStep step = steps.get(i);
            double time = (i + 1) * delayPerStep;
            
            KeyFrame kf = new KeyFrame(Duration.millis(time), e -> {
                String stepEdgeKey = "";
                if (step.getEdge() != null) {
                    int u = step.getEdge().getMfrom();
                    int v = step.getEdge().getMto();
                    stepEdgeKey = Math.min(u, v) + "-" + Math.max(u, v);
                }

                switch (step.getType()) {
                    case VISIT: 
                        if(step.getVertexId() != -1) highlightNode(step.getVertexId(), Color.ORANGE);
                        break;
                    case VISIT_EDGE:
                        if (!currentPathEdges.containsValue(stepEdgeKey)) {
                            if (step.getLineIndex() == 5) highlightEdge(step.getEdge(), Color.CORNFLOWERBLUE);
                            else if (step.getLineIndex() == 6) highlightEdge(step.getEdge(), Color.LIGHTGRAY);
                        }
                        break;
                    case RELAX_SUCCESS:
                        Edge newEdge = step.getEdge();
                        int targetNode = newEdge.getMto();
                        if (currentPathEdges.containsKey(targetNode)) {
                            String oldKey = currentPathEdges.get(targetNode);
                            if (edges.containsKey(oldKey) && !oldKey.equals(stepEdgeKey)) {
                                EdgeUI oldUI = edges.get(oldKey);
                                oldUI.line.setStroke(Color.LIGHTGRAY);
                                oldUI.line.setStrokeWidth(2);
                            }
                        }
                        highlightEdge(newEdge, Color.RED);
                        currentPathEdges.put(targetNode, stepEdgeKey);
                        highlightNode(targetNode, Color.LIGHTGREEN);
                        break;
                }
            });
            currentAnimation.getKeyFrames().add(kf);
        }
        currentAnimation.play();
    }

    private void highlightNode(int id, Color color) {
        Circle c = nodes.get(id);
        if (c != null) {
            c.setFill(color);
            Timeline pulse = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(c.radiusProperty(), 20)),
                new KeyFrame(Duration.millis(200), new KeyValue(c.radiusProperty(), 25)),
                new KeyFrame(Duration.millis(400), new KeyValue(c.radiusProperty(), 20))
            );
            pulse.play();
        }
    }

    private void highlightEdge(Edge edge, Color color) {
        if (edge == null) return;
        int u = edge.getMfrom();
        int v = edge.getMto();
        int min = Math.min(u, v);
        int max = Math.max(u, v);
        String key = min + "-" + max;
        EdgeUI ui = edges.get(key);
        if (ui != null) {
            ui.line.setStroke(color);
            if (color.equals(Color.LIGHTGRAY) || color.equals(Color.GRAY)) {
                ui.line.setStrokeWidth(2);
            } else {
                ui.line.setStrokeWidth(4);
            }
        }
    }
    
    private void animatePath(List<Integer> path) {
        if (path.size() < 1) return;
        currentAnimation = new Timeline();
        for (int i = 0; i < path.size(); i++) {
            final int index = i;
            final int vertexId = path.get(index);
            KeyFrame kfVertex = new KeyFrame(Duration.millis(i * 800), e -> {
                Circle c = nodes.get(vertexId);
                if (c != null) { c.setFill(Color.GOLD); c.setRadius(25); }
            });
            currentAnimation.getKeyFrames().add(kfVertex);
            if (i < path.size() - 1) {
                final int nextVertexId = path.get(i + 1);
                KeyFrame kfEdge = new KeyFrame(Duration.millis(i * 800 + 400), e -> {
                    String key = Math.min(vertexId, nextVertexId) + "-" + Math.max(vertexId, nextVertexId);
                    EdgeUI edgeUI = edges.get(key);
                    if (edgeUI != null) { edgeUI.line.setStroke(Color.RED); edgeUI.line.setStrokeWidth(4); }
                });
                currentAnimation.getKeyFrames().add(kfEdge);
            }
        }
        currentAnimation.play();
    }

    private void resetStyles() {
        for (Circle c : nodes.values()) { c.setFill(Color.LIGHTBLUE); c.setStroke(Color.BLACK); c.setRadius(20); }
        for (EdgeUI e : edges.values()) { e.line.setStroke(Color.GRAY); e.line.setStrokeWidth(2); }
    }

    private void updateMatrixDisplay() {
        if (graph != null) { matrixDisplay.setText(graph.getMatrixString()); }
    }

    public void addVertex(int id) {
        addVertex(id, -1, -1);
    }

    private void addVertex(int id, double x, double y) {
        if (nodes.containsKey(id)) return;
        if (id >= graph.verticesNumber()) { while (graph.verticesNumber() <= id) { graph.addVertex(); } }
        graph.setVertexExists(id, true);
        Circle circle = new Circle(20, Color.LIGHTBLUE);
        circle.setStroke(Color.BLACK);
        circle.setStrokeWidth(2);
        
        if (x == -1 || y == -1) {
            circle.setCenterX(1000 + (Math.random()-0.5)*200);
            circle.setCenterY(1000 + (Math.random()-0.5)*200);
        } else {
            circle.setCenterX(x);
            circle.setCenterY(y);
        }
        
        enableDrag(circle, id);
        Text label = new Text(String.valueOf(id));
        label.setX(circle.getCenterX() - 6); 
        label.setY(circle.getCenterY() + 6);
        label.setMouseTransparent(true); 
        
        graphPane.getChildren().addAll(circle, label);
        nodes.put(id, circle);
        nodeLabels.put(id, label);
        
        updateMatrixDisplay();
    }
    
    // [修改] 启用拖拽并集成点击连线逻辑
    private void enableDrag(Circle circle, int id) {
        final class InteractionState { 
            double startX, startY; 
            boolean isDragging = false; 
        }
        final InteractionState state = new InteractionState();

        circle.setOnMousePressed(e -> {
            state.startX = circle.getCenterX() - e.getX();
            state.startY = circle.getCenterY() - e.getY();
            state.isDragging = false; 
            circle.setCursor(javafx.scene.Cursor.MOVE);
            e.consume();
        });
        
        circle.setOnMouseDragged(e -> {
            state.isDragging = true;
            double newX = e.getX() + state.startX;
            double newY = e.getY() + state.startY;
            newX = Math.max(20, Math.min(graphPane.getPrefWidth() - 20, newX));
            newY = Math.max(20, Math.min(graphPane.getPrefHeight() - 20, newY));
            circle.setCenterX(newX); circle.setCenterY(newY);
            Text label = nodeLabels.get(id);
            if (label != null) { label.setX(newX - 6); label.setY(newY + 6); }
            updateConnectedEdges(id);
            e.consume();
        });

        circle.setOnMouseReleased(e -> {
            circle.setCursor(javafx.scene.Cursor.HAND);
            if (!state.isDragging) {
                // 如果没有发生拖动，则视为点击，触发连线逻辑
                handleNodeClick(id);
            }
        });
    }

    private void updateConnectedEdges(int id) {
        for (Map.Entry<String, EdgeUI> entry : edges.entrySet()) {
            String[] parts = entry.getKey().split("-");
            int from = Integer.parseInt(parts[0]);
            int to = Integer.parseInt(parts[1]);
            if (from == id || to == id) {
                updateSingleEdge(entry.getValue(), from, to);
            }
        }
    }
    
    private void updateAllEdges() {
        for (Map.Entry<String, EdgeUI> entry : edges.entrySet()) {
            String[] parts = entry.getKey().split("-");
            updateSingleEdge(entry.getValue(), Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
        }
    }
    
    private void updateSingleEdge(EdgeUI ui, int v1, int v2) {
        Circle c1 = nodes.get(v1); Circle c2 = nodes.get(v2);
        if(c1 != null && c2 != null) {
            Line line = ui.line;
            line.setStartX(c1.getCenterX()); line.setStartY(c1.getCenterY());
            line.setEndX(c2.getCenterX());   line.setEndY(c2.getCenterY());
            ui.label.setX((c1.getCenterX() + c2.getCenterX()) / 2);
            ui.label.setY((c1.getCenterY() + c2.getCenterY()) / 2 - 5);
        }
    }

    private void applyCircularLayout() {
        int n = nodes.size();
        if (n == 0) return;
        double baseRadius = Math.max(200, n * 20);
        double radius = baseRadius * currentScale;
        double centerX = graphPane.getPrefWidth() / 2;
        double centerY = graphPane.getPrefHeight() / 2;
        
        int i = 0;
        List<Integer> sortedKeys = nodes.keySet().stream().sorted().toList();
        for (Integer vertexId : sortedKeys) {
            double angle = 2 * Math.PI * i / n - Math.PI / 2;
            double x = centerX + radius * Math.cos(angle);
            double y = centerY + radius * Math.sin(angle);
            Circle circle = nodes.get(vertexId);
            if (circle != null) {
                circle.setCenterX(x); circle.setCenterY(y);
                Text t = nodeLabels.get(vertexId);
                if (t != null) { t.setX(x - 6); t.setY(y + 6); }
            }
            i++;
        }
        updateAllEdges();
    }

    public void removeVertex(int id) {
        if (!nodes.containsKey(id)) return;
        Circle circle = nodes.remove(id);
        Text label = nodeLabels.remove(id);
        if (circle != null) graphPane.getChildren().remove(circle);
        if (label != null) graphPane.getChildren().remove(label);
        graph.setVertexExists(id, false);
        Iterator<Map.Entry<String, EdgeUI>> it = edges.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, EdgeUI> entry = it.next();
            String key = entry.getKey();
            String[] parts = key.split("-");
            int from = Integer.parseInt(parts[0]);
            int to = Integer.parseInt(parts[1]);
            if (from == id || to == id) {
                graph.delEdge(from, to);
                graphPane.getChildren().removeAll(entry.getValue().line, entry.getValue().label);
                it.remove();
            }
        }
        updateMatrixDisplay();
    }

    public void addEdge(int from, int to, int weight) {
        if (!nodes.containsKey(from)) addVertex(from);
        if (!nodes.containsKey(to)) addVertex(to);
        if (!nodes.containsKey(from) || !nodes.containsKey(to)) return;
        
        // 统一 Key
        int min = Math.min(from, to);
        int max = Math.max(from, to);
        String edgeKey = min + "-" + max;
        
        if (edges.containsKey(edgeKey)) {
            EdgeUI edgeUI = edges.get(edgeKey);
            edgeUI.label.setText(String.valueOf(weight));
            graph.setEdge(from, to, weight); 
            return;
        }
        
        graph.setEdge(from, to, weight);
        Circle c1 = nodes.get(from); Circle c2 = nodes.get(to);
        Line line = new Line(c1.getCenterX(), c1.getCenterY(), c2.getCenterX(), c2.getCenterY());
        line.setStrokeWidth(2); line.setStroke(Color.GRAY);
        Text text = new Text((c1.getCenterX() + c2.getCenterX()) / 2, (c1.getCenterY() + c2.getCenterY()) / 2 - 5, String.valueOf(weight));
        text.setFill(Color.DARKRED);
        text.setMouseTransparent(true); 
        
        graphPane.getChildren().add(0, line); 
        graphPane.getChildren().add(text);
        edges.put(edgeKey, new EdgeUI(line, text));
        updateMatrixDisplay(); 
    }

    public void removeEdge(int from, int to) {
        graph.delEdge(from, to);
        String key = Math.min(from, to) + "-" + Math.max(from, to); 
        EdgeUI edgeUI = edges.remove(key);
        if (edgeUI != null) { graphPane.getChildren().removeAll(edgeUI.line, edgeUI.label); }
        updateMatrixDisplay();
    }
    
    public void saveGraph() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("保存邻接矩阵图 (DSL)");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        fileChooser.setInitialFileName("matrix_graph.txt");
        File file = fileChooser.showSaveDialog(root.getScene().getWindow());
        
        if (file == null) return;
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("# Vertices Positions\n");
            for (Map.Entry<Integer, Circle> entry : nodes.entrySet()) {
                writer.write(String.format("POS %d %.2f %.2f\n", entry.getKey(), entry.getValue().getCenterX(), entry.getValue().getCenterY()));
            }
            
            writer.write("\n# Edges\n");
            int n = graph.verticesNumber();
            for (int i = 0; i < n; i++) {
                if (!graph.isVertexExists(i)) continue;
                for (int j = i + 1; j < n; j++) {
                    if (!graph.isVertexExists(j)) continue;
                    int weight = graph.getEdge(i, j);
                    if (weight != 0) {
                        writer.write(String.format("%d -> %d : %d\n", i, j, weight));
                    }
                }
            }
            System.out.println("DSL 保存成功");
        } catch (IOException ex) { 
            ex.printStackTrace(); 
            matrixDisplay.setText("保存失败: " + ex.getMessage());
        }
    }
    
    public void loadGraph() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("打开图文件 (DSL)");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        File file = fileChooser.showOpenDialog(root.getScene().getWindow());
        
        if (file == null) return;
        
        try {
            StringBuilder dslContent = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    dslContent.append(line).append("\n");
                }
            }
            resetToDefault(); 
            renderFromDSL(dslContent.toString());
            System.out.println("DSL 加载成功");
        } catch (Exception ex) { 
            ex.printStackTrace(); 
            matrixDisplay.setText("加载失败: " + ex.getMessage());
        }
    }
}