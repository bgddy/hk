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
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
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
    
    // [修改] 分离矩阵显示和日志显示
    private VBox matrixVisualContainer; // 用于显示图形化矩阵
    private Text logDisplay;            // 仅用于显示日志
    private StringBuilder logHistory = new StringBuilder();

    private MatrixGraph graph;
    private Timeline currentAnimation;
    
    private double currentScale = 1.0;

    private Map<Integer, Circle> nodes = new HashMap<>();
    private Map<Integer, Text> nodeLabels = new HashMap<>();

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
        root.setPrefSize(1150, 650); // 稍微调大窗口
        
        // --- 中间：绘图区域 ---
        graphPane = new Pane();
        graphPane.setPrefSize(2000, 2000); 
        graphPane.setStyle("-fx-background-color: #f8f9fa;");

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

        // --- 右侧：数据与日志面板 ---
        VBox rightPane = new VBox(0);
        rightPane.setPrefWidth(420); 
        rightPane.setStyle("-fx-background-color: #ffffff; -fx-border-color: #dee2e6; -fx-border-width: 0 0 0 1;");

        // 1. 矩阵标题
        Label matrixTitle = new Label(" 邻接矩阵 (Adjacency Matrix)");
        matrixTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-padding: 10; -fx-background-color: #ecf0f1; -fx-border-color: #bdc3c7; -fx-border-width: 0 0 1 0;");
        matrixTitle.setMaxWidth(Double.MAX_VALUE);

        // 2. 矩阵可视化容器
        matrixVisualContainer = new VBox();
        matrixVisualContainer.setAlignment(Pos.TOP_CENTER);
        matrixVisualContainer.setPadding(new Insets(15));
        matrixVisualContainer.setStyle("-fx-background-color: #ffffff;");
        
        ScrollPane matrixScroll = new ScrollPane(matrixVisualContainer);
        matrixScroll.setFitToWidth(true);
        matrixScroll.setPrefHeight(350); // 给矩阵区域固定高度
        matrixScroll.setStyle("-fx-background-color: transparent; -fx-background: #ffffff;");

        // 3. 日志标题
        Label logTitle = new Label(" 运行日志");
        logTitle.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #555; -fx-padding: 8; -fx-background-color: #f0f0f0; -fx-border-color: #e0e0e0; -fx-border-width: 1 0 1 0;");
        logTitle.setMaxWidth(Double.MAX_VALUE);
        
        // 4. 日志内容
        logDisplay = new Text();
        logDisplay.setStyle("-fx-font-family: 'Segoe UI', sans-serif; -fx-font-size: 11px; -fx-fill: #34495e;");
        
        ScrollPane logScroll = new ScrollPane(logDisplay);
        logScroll.setFitToWidth(true);
        logScroll.setStyle("-fx-background-color: transparent; -fx-background: #ffffff;");
        logDisplay.wrappingWidthProperty().bind(logScroll.widthProperty().subtract(20));
        
        rightPane.getChildren().addAll(matrixTitle, matrixScroll, logTitle, logScroll);
        VBox.setVgrow(logScroll, Priority.ALWAYS); // 日志占满剩余空间
        
        root.setCenter(centerStack);
        root.setRight(rightPane);
        
        // 初始加载
        for (int i = 0; i < 5; i++) addVertex(i, -1, -1);
        applyCircularLayout();
        updateMatrixDisplay();
        centerContent(); 
    }

    // [核心优化] 渲染图形化矩阵
    private void renderMatrix() {
        matrixVisualContainer.getChildren().clear();

        // 1. 获取所有有效的顶点 (过滤掉已删除的点)
        List<Integer> validVertices = new ArrayList<>();
        int n = graph.verticesNumber();
        for(int i=0; i<n; i++) {
            if(graph.isVertexExists(i)) validVertices.add(i);
        }

        if (validVertices.isEmpty()) {
            matrixVisualContainer.getChildren().add(new Text("无数据 (图为空)"));
            return;
        }

        // 2. 创建 Grid
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(2); 
        grid.setVgap(2);
        grid.setStyle("-fx-background-color: #ecf0f1; -fx-padding: 2;"); // 网格线颜色

        // 3. 渲染表头
        // 左上角空白
        grid.add(createCell("", Color.web("#bdc3c7"), true), 0, 0);

        for (int i = 0; i < validVertices.size(); i++) {
            int vId = validVertices.get(i);
            // 列头 (上方)
            grid.add(createCell(String.valueOf(vId), Color.web("#dfe6e9"), true), i + 1, 0);
            // 行头 (左侧)
            grid.add(createCell(String.valueOf(vId), Color.web("#dfe6e9"), true), 0, i + 1);
        }

        // 4. 渲染数据单元格
        for (int i = 0; i < validVertices.size(); i++) {
            for (int j = 0; j < validVertices.size(); j++) {
                int u = validVertices.get(i);
                int v = validVertices.get(j);
                
                int weight = graph.getEdge(u, v);
                
                // 样式逻辑
                String text;
                Color bgColor;
                boolean isHeader = false;

                if (u == v) {
                    // 对角线
                    text = (weight == 0 || weight == Integer.MAX_VALUE) ? "0" : String.valueOf(weight);
                    bgColor = Color.web("#f7f9f9"); // 极淡灰
                } else if (weight > 0 && weight < Integer.MAX_VALUE) {
                    // 有效边
                    text = String.valueOf(weight);
                    bgColor = Color.web("#d6eaf8"); // 浅蓝高亮
                } else {
                    // 无边 (通常 0 或 INF)
                    text = "∞"; // 或者 "0"
                    bgColor = Color.WHITE;
                }
                
                grid.add(createCell(text, bgColor, isHeader), j + 1, i + 1);
            }
        }

        matrixVisualContainer.getChildren().add(grid);
    }

    // [新增] 辅助方法：创建单元格
    private StackPane createCell(String text, Color bg, boolean isHeader) {
        StackPane cell = new StackPane();
        double size = 30; // 单元格大小
        
        Rectangle rect = new Rectangle(size, size);
        rect.setFill(bg);
        
        Text t = new Text(text);
        if (isHeader) {
            t.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            t.setFill(Color.web("#2c3e50"));
        } else {
            t.setFont(Font.font("Consolas", 12));
            if (text.equals("∞") || text.equals("0")) {
                t.setFill(Color.LIGHTGRAY);
            } else {
                t.setFill(Color.BLACK);
            }
        }
        
        cell.getChildren().addAll(rect, t);
        return cell;
    }

    // [修改] 更新日志显示
    private void updateLog(String msg) {
        if (msg == null || msg.isEmpty()) return;
        String time = java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
        logHistory.insert(0, String.format("[%s] %s\n", time, msg));
        if (logHistory.length() > 3000) logHistory.setLength(3000);
        logDisplay.setText(logHistory.toString());
    }

    // [修改] 统一刷新入口
    private void updateMatrixDisplay() {
        if (graph != null) {
            renderMatrix();
        }
    }

    // === 以下逻辑与之前保持一致 ===

    private void handleNodeClick(int clickedId) {
        if (selectedNodeId == null) {
            selectedNodeId = clickedId;
            Circle c = nodes.get(clickedId);
            if (c != null) c.setFill(Color.CYAN); 
            updateLog("选中起点: " + clickedId);
        } else {
            if (selectedNodeId == clickedId) {
                resetStyles();
                selectedNodeId = null;
                updateLog("取消选中");
            } else {
                TextInputDialog dialog = new TextInputDialog("1");
                dialog.setTitle("添加边");
                dialog.setHeaderText(selectedNodeId + " -> " + clickedId);
                dialog.setContentText("权重:");

                Optional<String> result = dialog.showAndWait();
                result.ifPresent(wStr -> {
                    try {
                        int w = Integer.parseInt(wStr);
                        addEdge(selectedNodeId, clickedId, w);
                        updateLog("添加边: " + selectedNodeId + " -> " + clickedId + " (w:" + w + ")");
                    } catch (NumberFormatException ex) {
                        updateLog("无效权重");
                    }
                });
                resetStyles();
                selectedNodeId = null;
            }
        }
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
                if (weight != 0 && weight != Integer.MAX_VALUE) {
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
                    removeEdge(Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[1].trim()));
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
                    edgesToAdd.add(new int[]{u, v, w});
                } catch (Exception e) { }
            }
        }
        
        if (!loadedPositions.isEmpty()) {
            for (Integer id : loadedPositions.keySet()) {
                if (!nodes.containsKey(id)) addVertex(id, -1, -1);
                Circle c = nodes.get(id);
                double[] pos = loadedPositions.get(id);
                c.setCenterX(pos[0]); c.setCenterY(pos[1]);
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
        btn.setStyle("-fx-background-color: white; -fx-border-color: #bbb; -fx-border-radius: 5; -fx-font-size: 12px; -fx-font-weight: bold; -fx-cursor: hand;");
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
                    c.setCenterX(newX); c.setCenterY(newY);
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
                if (weight > 0 && weight != Integer.MAX_VALUE) {
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
        logHistory.setLength(0);
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
            
            updateLog("Dijkstra 搜索: " + start + " -> " + end);
            
            if (path.isEmpty() && start != end) {
                updateLog("结果: 不可达");
            } else {
                updateLog("路径: " + path);
                updateLog("总权重: " + dijkstra.getShortestDistance(end));
                animatePath(path);
            }
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
            
            updateLog("Dijkstra 全图结果:");
            updateLog(dijkstra.getAllPathsResult(start));
            
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
                                if (oldUI != null) {
                                    oldUI.line.setStroke(Color.LIGHTGRAY);
                                    oldUI.line.setStrokeWidth(2);
                                }
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
                handleNodeClick(id);
            }
        });
    }

    public void removeVertex(int id) {
        if (!nodes.containsKey(id)) return;
        graph.setVertexExists(id, false); // 标记删除
        graphPane.getChildren().removeAll(nodes.get(id), nodeLabels.get(id));
        nodes.remove(id); nodeLabels.remove(id);
        
        Iterator<Map.Entry<String, EdgeUI>> it = edges.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry<String, EdgeUI> entry = it.next();
            String[] parts = entry.getKey().split("-");
            int u = Integer.parseInt(parts[0]);
            int v = Integer.parseInt(parts[1]);
            if(u == id || v == id) {
                graph.delEdge(u, v);
                graphPane.getChildren().removeAll(entry.getValue().line, entry.getValue().label);
                it.remove();
            }
        }
        updateMatrixDisplay();
    }

    public void addEdge(int from, int to, int weight) {
        if (from == to) return;
        graph.setEdge(from, to, weight);
        String key = Math.min(from,to) + "-" + Math.max(from,to);
        if (edges.containsKey(key)) {
            edges.get(key).label.setText(String.valueOf(weight));
        } else {
            Line line = new Line(); line.setStrokeWidth(2); line.setStroke(Color.GRAY);
            Text text = new Text(String.valueOf(weight)); text.setFill(Color.DARKRED);
            graphPane.getChildren().add(0, line); graphPane.getChildren().add(text);
            edges.put(key, new EdgeUI(line, text));
        }
        updateConnectedEdges(from);
        updateMatrixDisplay();
    }

    public void removeEdge(int from, int to) {
        graph.delEdge(from, to);
        String key = Math.min(from,to) + "-" + Math.max(from,to);
        EdgeUI ui = edges.remove(key);
        if (ui != null) graphPane.getChildren().removeAll(ui.line, ui.label);
        updateMatrixDisplay();
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
        if(c1!=null && c2!=null){
            ui.line.setStartX(c1.getCenterX()); ui.line.setStartY(c1.getCenterY());
            ui.line.setEndX(c2.getCenterX()); ui.line.setEndY(c2.getCenterY());
            ui.label.setX((c1.getCenterX()+c2.getCenterX())/2);
            ui.label.setY((c1.getCenterY()+c2.getCenterY())/2-5);
        }
    }

    private void applyCircularLayout() {
        int n = nodes.size();
        if (n == 0) return;
        double radius = 150 * currentScale; 
        double centerX = 1000; double centerY = 1000;
        int i = 0;
        List<Integer> sortedKeys = nodes.keySet().stream().sorted().toList();
        for (Integer id : sortedKeys) {
            double angle = 2 * Math.PI * i / n - Math.PI / 2;
            Circle c = nodes.get(id);
            if (c != null) { 
                double x = centerX + radius * Math.cos(angle);
                double y = centerY + radius * Math.sin(angle);
                c.setCenterX(x); c.setCenterY(y);
                Text t = nodeLabels.get(id); if(t!=null) { t.setX(x-6); t.setY(y+6); }
            }
            i++;
        }
        updateAllEdges();
    }
    // [补全] 保存图结构到文件
    public void saveGraph() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("保存图 (DSL)");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        fileChooser.setInitialFileName("matrix_graph.txt");
        File file = fileChooser.showSaveDialog(root.getScene().getWindow());
        
        if (file == null) return;
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(getGraphDSL());
            updateLog("DSL 保存成功");
        } catch (IOException ex) { 
            ex.printStackTrace(); 
            updateLog("保存失败: " + ex.getMessage());
        }
    }

    // [补全] 从文件加载图结构
    public void loadGraph() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("打开图 (DSL)");
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
            updateLog("DSL 加载成功");
        } catch (Exception ex) { 
            ex.printStackTrace(); 
            updateLog("加载失败: " + ex.getMessage());
        }
    }
}