package org.example.ui;

import javafx.animation.KeyFrame;
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
    private ScrollPane graphScrollPane;
    private Text matrixDisplay;
    private MatrixGraph graph;
    
    private double currentScale = 1.0;

    private Map<Integer, Circle> nodes = new HashMap<>();
    private Map<Integer, Text> nodeLabels = new HashMap<>();

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
        
        // 左侧：绘图区域
        graphPane = new Pane();
        graphPane.setPrefSize(2000, 2000); 
        graphPane.setStyle("-fx-background-color: #f8f9fa;");
        
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

        // 右侧：数据与日志
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
        
        updateMatrixDisplay();
        centerContent(); 
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
            updateNodePositions();
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
                    int v; int w = 1;
                    if (rightPart.contains(":")) {
                        String[] vw = rightPart.split(":");
                        v = Integer.parseInt(vw[0].trim());
                        w = Integer.parseInt(vw[1].trim());
                    } else { v = Integer.parseInt(rightPart); }
                    while (graph.verticesNumber() <= Math.max(u, v)) { graph.addVertex(); }
                    addVertex(u); addVertex(v);
                    edgesToAdd.add(new int[]{u, v, w});
                } catch (Exception e) { System.out.println("DSL 解析错误: " + line); }
            }
        }
        for (int[] edge : edgesToAdd) { addEdge(edge[0], edge[1], edge[2]); }
        updateNodePositions();
        updateMatrixDisplay();
        matrixDisplay.setText(matrixDisplay.getText() + "\n\n[DSL 渲染完成]");
    }

    public void resetToDefault() {
        clearInternalGraphState();
        for (int i = 0; i < 5; i++) { addVertex(i); }
        updateNodePositions();
        updateMatrixDisplay();
        matrixDisplay.setText(matrixDisplay.getText() + "\n\n[已恢复初始设置]");
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
                for (int i = 0; i < path.size(); i++) { sb.append(path.get(i)).append(i < path.size() - 1 ? " -> " : ""); }
                sb.append("\n总权重: ").append(dijkstra.getShortestDistance(end));
                animatePath(path);
            }
            matrixDisplay.setText(sb.toString());
        } catch (NumberFormatException e) { matrixDisplay.setText(graph.getMatrixString() + "\n\n错误: 请输入有效的顶点编号"); }
    }
    
    private void animatePath(List<Integer> path) {
        if (path.size() < 1) return;
        Timeline timeline = new Timeline();
        for (int i = 0; i < path.size(); i++) {
            final int index = i;
            final int vertexId = path.get(index);
            KeyFrame kfVertex = new KeyFrame(Duration.millis(i * 800), e -> {
                Circle c = nodes.get(vertexId);
                if (c != null) { c.setFill(Color.GOLD); c.setRadius(25); }
            });
            timeline.getKeyFrames().add(kfVertex);
            if (i < path.size() - 1) {
                final int nextVertexId = path.get(i + 1);
                KeyFrame kfEdge = new KeyFrame(Duration.millis(i * 800 + 400), e -> {
                    String key = vertexId + "-" + nextVertexId;
                    EdgeUI edgeUI = edges.get(key);
                    if (edgeUI != null) { edgeUI.line.setStroke(Color.RED); edgeUI.line.setStrokeWidth(4); } else {
                        String revKey = nextVertexId + "-" + vertexId;
                        EdgeUI revEdgeUI = edges.get(revKey);
                        if (revEdgeUI != null) { revEdgeUI.line.setStroke(Color.RED); revEdgeUI.line.setStrokeWidth(4); }
                    }
                });
                timeline.getKeyFrames().add(kfEdge);
            }
        }
        timeline.play();
    }

    private void resetStyles() {
        for (Circle c : nodes.values()) { c.setFill(Color.LIGHTBLUE); c.setStroke(Color.BLACK); c.setRadius(20); }
        for (EdgeUI e : edges.values()) { e.line.setStroke(Color.GRAY); e.line.setStrokeWidth(2); }
    }

    private void updateMatrixDisplay() {
        if (graph != null) { matrixDisplay.setText(graph.getMatrixString()); }
    }

    public void addVertex(int id) {
        if (nodes.containsKey(id)) return;
        if (id >= graph.verticesNumber()) { while (graph.verticesNumber() <= id) { graph.addVertex(); } }
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
            newX = Math.max(20, Math.min(graphPane.getPrefWidth() - 20, newX));
            newY = Math.max(20, Math.min(graphPane.getPrefHeight() - 20, newY));
            circle.setCenterX(newX); circle.setCenterY(newY);
            Text label = nodeLabels.get(id);
            if (label != null) { label.setX(newX - 6); label.setY(newY + 6); }
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
                Circle c1 = nodes.get(from); Circle c2 = nodes.get(to);
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
        Circle c1 = nodes.get(from); Circle c2 = nodes.get(to);
        Line line = new Line(c1.getCenterX(), c1.getCenterY(), c2.getCenterX(), c2.getCenterY());
        line.setStrokeWidth(2); line.setStroke(Color.GRAY);
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
        if (edgeUI != null) { graphPane.getChildren().removeAll(edgeUI.line, edgeUI.label); }
        updateMatrixDisplay();
    }

    private void updateNodePositions() {
        int n = nodes.size();
        if (n == 0) return;
        double baseRadius = Math.max(200, n * 20);
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
                circle.setCenterX(x); circle.setCenterY(y);
                Text t = nodeLabels.get(vertexId);
                if (t != null) { t.setX(x - 6); t.setY(y + 6); }
            }
            i++;
        }
        for (Map.Entry<String, EdgeUI> entry : edges.entrySet()) {
            String[] parts = entry.getKey().split("-");
            int from = Integer.parseInt(parts[0]);
            int to = Integer.parseInt(parts[1]);
            Circle c1 = nodes.get(from); Circle c2 = nodes.get(to);
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
    
    // 【修改】保存为 DSL 格式 (.txt)
    public void saveGraph() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("保存邻接矩阵图 (DSL)");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        fileChooser.setInitialFileName("matrix_graph.txt");
        File file = fileChooser.showSaveDialog(root.getScene().getWindow());
        
        if (file == null) return;
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            int n = graph.verticesNumber();
            // 遍历矩阵，只保存 i < j 且 weight > 0 的边
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
    
    // 【修改】加载 DSL 格式 (.txt)
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
            renderFromDSL(dslContent.toString());
            System.out.println("DSL 加载成功");
        } catch (Exception ex) { 
            ex.printStackTrace(); 
            matrixDisplay.setText("加载失败: " + ex.getMessage());
        }
    }
}