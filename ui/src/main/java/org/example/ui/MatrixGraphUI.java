package org.example.ui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import org.example.core.Dijkstra;
import org.example.core.MatrixGraph;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MatrixGraphUI {

    private BorderPane root;
    private Pane graphPane;
    private Text matrixDisplay;
    private MatrixGraph graph;
    
    // 布局缩放因子
    private double currentScale = 1.0;

    private Map<Integer, Circle> nodes = new HashMap<>();
    private Map<Integer, Text> nodeLabels = new HashMap<>();

    private static class EdgeUI {
        Line line;
        Text label;
        EdgeUI(Line line, Text label) { this.line = line; this.label = label; }
    }

    // 存储边UI的Map (Key: "from-to")
    private Map<String, EdgeUI> edges = new HashMap<>();

    public MatrixGraphUI(MatrixGraph graph) {
        this.graph = graph;
        
        // 创建根布局
        root = new BorderPane();
        root.setPrefSize(1050, 600);
        
        // ================== 左侧：绘图区域 ==================
        graphPane = new Pane();
        // 初始大小，后续根据节点布局调整
        graphPane.setPrefSize(2000, 2000); 
        graphPane.setStyle("-fx-background-color: #f8f9fa;");
        
        // 使用 Group 包裹以支持居中和滚动
        Group scrollContent = new Group(graphPane);
        
        // 使用 ScrollPane 包裹 scrollContent
        ScrollPane graphScrollPane = new ScrollPane(scrollContent);
        graphScrollPane.setPrefSize(650, 600);
        graphScrollPane.setPannable(true); // 允许鼠标拖拽画布滚动
        
        graphScrollPane.setStyle("-fx-background-color: transparent; -fx-border-color: #dee2e6; -fx-border-width: 1;");
        
        // --- 缩放控制按钮 ---
        Button zoomInBtn = createZoomButton("放大", 1.2);
        Button zoomOutBtn = createZoomButton("缩小", 0.8);
        
        VBox zoomControls = new VBox(10, zoomInBtn, zoomOutBtn);
        zoomControls.setAlignment(Pos.CENTER);
        zoomControls.setPadding(new Insets(20));
        zoomControls.setPickOnBounds(false);
        
        // 使用 StackPane 将缩放控件叠加在 ScrollPane 上
        StackPane centerStack = new StackPane();
        centerStack.getChildren().addAll(graphScrollPane, zoomControls);
        StackPane.setAlignment(zoomControls, Pos.BOTTOM_RIGHT);

        // ================== 右侧：数据与日志 ==================
        VBox matrixPane = new VBox(10);
        matrixPane.setPadding(new Insets(15));
        matrixPane.setPrefHeight(350);
        matrixPane.setStyle("-fx-background-color: #ffffff; -fx-border-color: #dee2e6; -fx-border-width: 1;");
        
        Text matrixTitle = new Text("数据与日志");
        matrixTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-fill: #2c3e50;");
        
        // 滚动面板防止矩阵过大
        ScrollPane textScrollPane = new ScrollPane();
        textScrollPane.setFitToWidth(false); // 允许水平滚动
        textScrollPane.setPrefHeight(300);
        textScrollPane.setStyle("-fx-background-color: transparent; -fx-background: #ffffff;");
        
        matrixDisplay = new Text();
        matrixDisplay.setStyle("-fx-font-family: 'Monaco', 'Menlo', 'Consolas', monospace; -fx-font-size: 12px; -fx-fill: #34495e;");
        
        textScrollPane.setContent(matrixDisplay);
        matrixPane.getChildren().addAll(matrixTitle, textScrollPane);
        
        // 组合右侧面板
        VBox rightPane = new VBox(10);
        rightPane.setPrefWidth(380); // 限制宽度
        rightPane.setPadding(new Insets(0, 0, 0, 10));
        rightPane.getChildren().add(matrixPane); // 只加显示区，控制区在 MainApp
        VBox.setVgrow(matrixPane, Priority.ALWAYS);
        
        root.setCenter(centerStack); // 放在中间
        root.setRight(rightPane);        // 放在右边
        
        // 初始滚动条居中
        graphScrollPane.setHvalue(0.5);
        graphScrollPane.setVvalue(0.5);
        
        // 初始化显示
        updateMatrixDisplay();
    }
    
    private Button createZoomButton(String text, double factor) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: white; -fx-border-color: #bbb; -fx-border-radius: 5; -fx-background-radius: 5; -fx-font-size: 12px; -fx-font-weight: bold; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 3, 0, 0, 1);");
        btn.setPrefSize(50, 30);
        btn.setOnAction(e -> zoom(factor));
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #999; -fx-border-radius: 5; -fx-background-radius: 5; -fx-font-size: 12px; -fx-font-weight: bold; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 4, 0, 0, 1);"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: white; -fx-border-color: #bbb; -fx-border-radius: 5; -fx-background-radius: 5; -fx-font-size: 12px; -fx-font-weight: bold; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 3, 0, 0, 1);"));
        return btn;
    }
    
    private void zoom(double factor) {
        double newScale = currentScale * factor;
        if (newScale >= 0.1 && newScale <= 10.0) {
            currentScale = newScale;
            updateNodePositions(); // 重新计算布局
        }
    }

    public Pane getPane() {
        return root;
    }

    public void generateRandomGraph() {
        for (EdgeUI edgeUI : edges.values()) {
            graphPane.getChildren().removeAll(edgeUI.line, edgeUI.label);
        }
        edges.clear();

        graph.generateRandomGraph();

        int n = graph.verticesNumber();
        for (int i = 0; i < n; i++) {
            if (!graph.isVertexExists(i)) continue;
            for (int j = 0; j < n; j++) {
                if (!graph.isVertexExists(j)) continue;
                int weight = graph.getEdge(i, j);
                if (weight > 0) {
                    if (i < j || (i == j)) {
                         addEdge(i, j, weight); 
                    }
                }
            }
        }

        updateNodePositions();
        updateMatrixDisplay();
        matrixDisplay.setText(matrixDisplay.getText() + "\n\n[随机图生成完毕]");
    }

    public void renderFromDSL(String dslText) {
        if (dslText == null || dslText.trim().isEmpty()) return;
        
        clearInternalGraphState();
        
        String[] lines = dslText.split("\n");
        List<int[]> edgesToAdd = new ArrayList<>();
        
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;
            
            if (line.contains("->")) {
                try {
                    String[] parts = line.split("->");
                    int u = Integer.parseInt(parts[0].trim());
                    String rightPart = parts[1].trim();
                    int v;
                    int w = 1;
                    
                    if (rightPart.contains(":")) {
                        String[] vw = rightPart.split(":");
                        v = Integer.parseInt(vw[0].trim());
                        w = Integer.parseInt(vw[1].trim());
                    } else {
                        v = Integer.parseInt(rightPart);
                    }
                    
                    while (graph.verticesNumber() <= Math.max(u, v)) {
                        graph.addVertex();
                    }
                    
                    addVertex(u);
                    addVertex(v);
                    edgesToAdd.add(new int[]{u, v, w});
                    
                } catch (Exception e) {
                    System.out.println("DSL 解析错误: " + line);
                }
            }
        }
        
        for (int[] edge : edgesToAdd) {
            addEdge(edge[0], edge[1], edge[2]);
        }
        
        updateNodePositions();
        updateMatrixDisplay();
        matrixDisplay.setText(matrixDisplay.getText() + "\n\n[DSL 渲染完成]");
    }

    public void resetToDefault() {
        clearInternalGraphState();
        for (int i = 0; i < 5; i++) {
            addVertex(i);
        }
        updateNodePositions();
        updateMatrixDisplay();
        matrixDisplay.setText(matrixDisplay.getText() + "\n\n[已恢复初始设置]");
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

    public void performDijkstra(String startText, String endText) {
        resetStyles();
        try {
            int start = Integer.parseInt(startText.trim());
            int end = Integer.parseInt(endText.trim());
            
            if (!graph.isVertexExists(start) || !graph.isVertexExists(end)) {
                matrixDisplay.setText(graph.getMatrixString() + "\n\n错误: 顶点不存在");
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
                for (int i = 0; i < path.size(); i++) {
                    sb.append(path.get(i)).append(i < path.size() - 1 ? " -> " : "");
                }
                sb.append("\n总权重: ").append(dijkstra.getShortestDistance(end));
                animatePath(path);
            }
            matrixDisplay.setText(sb.toString());
            
        } catch (NumberFormatException e) {
            matrixDisplay.setText(graph.getMatrixString() + "\n\n错误: 请输入有效的顶点编号");
        }
    }
    
    private void animatePath(List<Integer> path) {
        if (path.size() < 1) return;
        Timeline timeline = new Timeline();
        for (int i = 0; i < path.size(); i++) {
            final int index = i;
            final int vertexId = path.get(index);
            KeyFrame kfVertex = new KeyFrame(Duration.millis(i * 800), e -> {
                Circle c = nodes.get(vertexId);
                if (c != null) {
                    c.setFill(Color.GOLD);
                    c.setRadius(25);
                }
            });
            timeline.getKeyFrames().add(kfVertex);
            if (i < path.size() - 1) {
                final int nextVertexId = path.get(i + 1);
                KeyFrame kfEdge = new KeyFrame(Duration.millis(i * 800 + 400), e -> {
                    String key = vertexId + "-" + nextVertexId;
                    EdgeUI edgeUI = edges.get(key);
                    if (edgeUI != null) {
                        edgeUI.line.setStroke(Color.RED);
                        edgeUI.line.setStrokeWidth(4);
                    } else {
                        String revKey = nextVertexId + "-" + vertexId;
                        EdgeUI revEdgeUI = edges.get(revKey);
                        if (revEdgeUI != null) {
                            revEdgeUI.line.setStroke(Color.RED);
                            revEdgeUI.line.setStrokeWidth(4);
                        }
                    }
                });
                timeline.getKeyFrames().add(kfEdge);
            }
        }
        timeline.play();
    }

    private void resetStyles() {
        for (Circle c : nodes.values()) {
            c.setFill(Color.LIGHTBLUE);
            c.setStroke(Color.BLACK);
            c.setRadius(20);
        }
        for (EdgeUI e : edges.values()) {
            e.line.setStroke(Color.GRAY);
            e.line.setStrokeWidth(2);
        }
    }

    private void updateMatrixDisplay() {
        if (graph != null) {
            String matrixString = graph.getMatrixString();
            matrixDisplay.setText(matrixString);
        }
    }

    public void addVertex(int id) {
        if (nodes.containsKey(id)) return;
        if (id >= graph.verticesNumber()) {
            while (graph.verticesNumber() <= id) {
                graph.addVertex();
            }
        }
        graph.setVertexExists(id, true);
        Circle circle = new Circle(20, Color.LIGHTBLUE);
        circle.setStroke(Color.BLACK);
        circle.setStrokeWidth(2);
        enableDrag(circle, id);
        Text label = new Text(String.valueOf(id));
        graphPane.getChildren().addAll(circle, label);
        nodes.put(id, circle);
        nodeLabels.put(id, label);
        updateNodePositions();
        updateMatrixDisplay();
    }
    
    private void enableDrag(Circle circle, int id) {
        final class Delta { double x, y; }
        final Delta dragDelta = new Delta();
        circle.setOnMousePressed(e -> {
            dragDelta.x = circle.getCenterX() - e.getX();
            dragDelta.y = circle.getCenterY() - e.getY();
            circle.setCursor(javafx.scene.Cursor.MOVE);
        });
        circle.setOnMouseDragged(e -> {
            double newX = e.getX() + dragDelta.x;
            double newY = e.getY() + dragDelta.y;
            // 限制拖拽范围
            newX = Math.max(20, Math.min(graphPane.getPrefWidth() - 20, newX));
            newY = Math.max(20, Math.min(graphPane.getPrefHeight() - 20, newY));
            circle.setCenterX(newX);
            circle.setCenterY(newY);
            Text label = nodeLabels.get(id);
            if (label != null) {
                label.setX(newX - 6);
                label.setY(newY + 6);
            }
            updateConnectedEdges(id);
        });
        circle.setOnMouseReleased(e -> circle.setCursor(javafx.scene.Cursor.HAND));
    }

    private void updateConnectedEdges(int id) {
        for (Map.Entry<String, EdgeUI> entry : edges.entrySet()) {
            String[] parts = entry.getKey().split("-");
            int from = Integer.parseInt(parts[0]);
            int to = Integer.parseInt(parts[1]);
            if (from == id || to == id) {
                Circle c1 = nodes.get(from);
                Circle c2 = nodes.get(to);
                if(c1 != null && c2 != null) {
                    Line line = entry.getValue().line;
                    line.setStartX(c1.getCenterX()); line.setStartY(c1.getCenterY());
                    line.setEndX(c2.getCenterX());   line.setEndY(c2.getCenterY());
                    Text label = entry.getValue().label;
                    label.setX((c1.getCenterX() + c2.getCenterX()) / 2);
                    label.setY((c1.getCenterY() + c2.getCenterY()) / 2 - 5);
                }
            }
        }
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
        updateNodePositions();
        updateMatrixDisplay();
    }

    public void addEdge(int from, int to, int weight) {
        if (!nodes.containsKey(from)) addVertex(from);
        if (!nodes.containsKey(to)) addVertex(to);
        if (!nodes.containsKey(from) || !nodes.containsKey(to)) return;

        String edgeKey = from + "-" + to;
        if (edges.containsKey(edgeKey)) {
            EdgeUI oldEdge = edges.get(edgeKey);
            graphPane.getChildren().removeAll(oldEdge.line, oldEdge.label);
            edges.remove(edgeKey);
        }
        graph.setEdge(from, to, weight);
        Circle c1 = nodes.get(from);
        Circle c2 = nodes.get(to);
        Line line = new Line(c1.getCenterX(), c1.getCenterY(), c2.getCenterX(), c2.getCenterY());
        line.setStrokeWidth(2);
        line.setStroke(Color.GRAY);
        Text text = new Text((c1.getCenterX() + c2.getCenterX()) / 2, (c1.getCenterY() + c2.getCenterY()) / 2 - 5, String.valueOf(weight));
        text.setFill(Color.DARKRED);
        graphPane.getChildren().add(0, line); 
        graphPane.getChildren().add(text);
        edges.put(edgeKey, new EdgeUI(line, text));
        updateMatrixDisplay(); 
    }

    public void removeEdge(int from, int to) {
        graph.delEdge(from, to);
        String key = from + "-" + to;
        EdgeUI edgeUI = edges.remove(key);
        if (edgeUI != null) {
            graphPane.getChildren().removeAll(edgeUI.line, edgeUI.label);
        }
        updateMatrixDisplay();
    }

    private void updateNodePositions() {
        int n = nodes.size();
        if (n == 0) return;

        // 动态计算画布大小
        double baseRadius = Math.max(200, n * 20);
        // 使用 currentScale 调整点间距
        double radius = baseRadius * currentScale;
        
        double requiredSize = Math.max(2000, radius * 2 + 400);
        graphPane.setPrefSize(requiredSize, requiredSize);

        double centerX = requiredSize / 2;
        double centerY = requiredSize / 2;

        int i = 0;
        List<Integer> sortedKeys = nodes.keySet().stream().sorted().toList();
        
        for (Integer vertexId : sortedKeys) {
            double angle = 2 * Math.PI * i / n - Math.PI / 2;
            double x = centerX + radius * Math.cos(angle);
            double y = centerY + radius * Math.sin(angle);
            
            Circle circle = nodes.get(vertexId);
            if (circle != null) {
                circle.setCenterX(x);
                circle.setCenterY(y);
                Text t = nodeLabels.get(vertexId);
                if (t != null) {
                    t.setX(x - 6);
                    t.setY(y + 6);
                }
            }
            i++;
        }
        
        for (Map.Entry<String, EdgeUI> entry : edges.entrySet()) {
            String[] parts = entry.getKey().split("-");
            int from = Integer.parseInt(parts[0]);
            int to = Integer.parseInt(parts[1]);
            Circle c1 = nodes.get(from);
            Circle c2 = nodes.get(to);
            if (c1 != null && c2 != null) {
                Line l = entry.getValue().line;
                l.setStartX(c1.getCenterX()); l.setStartY(c1.getCenterY());
                l.setEndX(c2.getCenterX());   l.setEndY(c2.getCenterY());
                Text t = entry.getValue().label;
                t.setX((c1.getCenterX() + c2.getCenterX()) / 2);
                t.setY((c1.getCenterY() + c2.getCenterY()) / 2 - 5);
            }
        }
    }
    
    public void saveGraph() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("保存邻接矩阵图");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Graph Files", "*.graph"));
        fileChooser.setInitialFileName("matrix_graph.graph");
        File file = fileChooser.showSaveDialog(root.getScene().getWindow());
        if (file == null) return;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (Map.Entry<Integer, Circle> entry : nodes.entrySet()) {
                int id = entry.getKey();
                Circle c = entry.getValue();
                writer.write(String.format("V,%d,%.2f,%.2f", id, c.getCenterX(), c.getCenterY()));
                writer.newLine();
            }
            int n = graph.verticesNumber();
            for (int i = 0; i < n; i++) {
                if (!graph.isVertexExists(i)) continue;
                for (int j = 0; j < n; j++) {
                    if (!graph.isVertexExists(j)) continue;
                    int weight = graph.getEdge(i, j);
                    if (weight != 0) {
                        writer.write(String.format("E,%d,%d,%d", i, j, weight));
                        writer.newLine();
                    }
                }
            }
            System.out.println("保存成功");
        } catch (IOException ex) { ex.printStackTrace(); }
    }
    
    public void loadGraph() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("打开图文件");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Graph Files", "*.graph"));
        File file = fileChooser.showOpenDialog(root.getScene().getWindow());
        if (file == null) return;
        clearInternalGraphState();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 0) continue;
                if (parts[0].equals("V")) {
                    int id = Integer.parseInt(parts[1]);
                    double x = Double.parseDouble(parts[2]);
                    double y = Double.parseDouble(parts[3]);
                    addVertex(id);
                    Circle c = nodes.get(id);
                    if (c != null) {
                        c.setCenterX(x); c.setCenterY(y);
                        Text t = nodeLabels.get(id);
                        if(t != null) { t.setX(x-6); t.setY(y+6); }
                    }
                } else if (parts[0].equals("E")) {
                    int from = Integer.parseInt(parts[1]);
                    int to = Integer.parseInt(parts[2]);
                    int weight = Integer.parseInt(parts[3]);
                    addEdge(from, to, weight);
                }
            }
            updateMatrixDisplay();
            System.out.println("加载成功");
        } catch (Exception ex) {
            ex.printStackTrace();
            matrixDisplay.setText("加载失败: " + ex.getMessage());
        }
    }
}