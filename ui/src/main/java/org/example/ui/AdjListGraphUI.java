package org.example.ui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import org.example.core.AdjListGraph;
import org.example.core.BFS;
import org.example.core.DFS;
import org.example.core.Dijkstra;
import org.example.core.kruskal;
import org.example.core.Edge;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class AdjListGraphUI {

    private BorderPane root;
    private Pane graphPane;
    private Text adjListDisplay;
    private AdjListGraph graph;

    private Map<Integer, Circle> nodes = new HashMap<>();
    private Map<Integer, Text> nodeLabels = new HashMap<>();

    private static class EdgeUI {
        Line line;
        Text label;
        EdgeUI(Line line, Text label) { this.line = line; this.label = label; }
    }

    private Map<String, EdgeUI> edges = new HashMap<>();

    public AdjListGraphUI(AdjListGraph graph) {
        this.graph = graph;
        
        // 创建根布局
        root = new BorderPane();
        root.setPrefSize(1000, 600);
        
        // ================== 左侧：绘图区域 ==================
        graphPane = new Pane();
        graphPane.setPrefSize(2000, 2000); 
        graphPane.setStyle("-fx-background-color: #f8f9fa;");
        
        ScrollPane graphScrollPane = new ScrollPane(graphPane);
        graphScrollPane.setPrefSize(650, 600);
        graphScrollPane.setPannable(true);
        graphScrollPane.setStyle("-fx-background-color: transparent; -fx-border-color: #dee2e6; -fx-border-width: 1;");
        
        // ================== 右侧：仅保留文本显示 ==================
        VBox rightPane = new VBox(10);
        rightPane.setPadding(new Insets(0, 0, 0, 5));
        rightPane.setPrefWidth(340);
        
        VBox textContainer = new VBox(5);
        textContainer.setPadding(new Insets(10));
        textContainer.setStyle("-fx-background-color: #ffffff; -fx-border-color: #dee2e6; -fx-border-width: 1;");
        
        Text adjListTitle = new Text("数据与日志");
        adjListTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-fill: #2c3e50;");
        
        ScrollPane textScrollPane = new ScrollPane();
        textScrollPane.setFitToWidth(true);
        textScrollPane.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        
        adjListDisplay = new Text();
        adjListDisplay.setStyle("-fx-font-family: 'Monaco', 'Menlo', 'Consolas', monospace; -fx-font-size: 11px; -fx-fill: #34495e;");
        adjListDisplay.wrappingWidthProperty().bind(textScrollPane.widthProperty().subtract(20));
        
        textScrollPane.setContent(adjListDisplay);
        textContainer.getChildren().addAll(adjListTitle, textScrollPane);
        VBox.setVgrow(textContainer, Priority.ALWAYS); 
        VBox.setVgrow(textScrollPane, Priority.ALWAYS);
        
        rightPane.getChildren().add(textContainer);
        
        root.setCenter(graphScrollPane);
        root.setRight(rightPane);
        
        for (int i = 0; i < 5; i++) addVertexUIOnly(i);
        graphScrollPane.setHvalue(0.0); graphScrollPane.setVvalue(0.0);
        
        updateAdjListDisplay();
    }

    public BorderPane getPane() { return root; }

    // === 核心逻辑方法 (供 MainApp 调用) ===

    public void saveGraph() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("保存图结构");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Graph Files", "*.graph"));
        fileChooser.setInitialFileName("adj_graph.graph");
        File file = fileChooser.showSaveDialog(root.getScene().getWindow());
        if (file == null) return;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (Map.Entry<Integer, Circle> entry : nodes.entrySet()) {
                int id = entry.getKey(); Circle c = entry.getValue();
                writer.write(String.format("V,%d,%.2f,%.2f", id, c.getCenterX(), c.getCenterY()));
                writer.newLine();
            }
            int n = graph.verticesNumber();
            for (int i = 0; i < n; i++) {
                for (org.example.core.Edge e = graph.firstEdge(i); e != null; e = graph.nextEdge(e)) {
                    if (e.getMfrom() < e.getMto()) {
                        writer.write(String.format("E,%d,%d,%d", e.getMfrom(), e.getMto(), e.getMweight()));
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
        clearAllEdges(); nodes.clear(); nodeLabels.clear(); graphPane.getChildren().clear(); edges.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 0) continue;
                if (parts[0].equals("V")) {
                    int id = Integer.parseInt(parts[1]);
                    double x = Double.parseDouble(parts[2]);
                    double y = Double.parseDouble(parts[3]);
                    if (id >= graph.verticesNumber()) { while (graph.verticesNumber() <= id) graph.addVertex(); }
                    addVertexUIOnly(id);
                    Circle c = nodes.get(id);
                    if (c != null) { c.setCenterX(x); c.setCenterY(y); Text t = nodeLabels.get(id); if(t!=null) { t.setX(x-5); t.setY(y+5); } }
                } else if (parts[0].equals("E")) {
                    int from = Integer.parseInt(parts[1]); int to = Integer.parseInt(parts[2]); int weight = Integer.parseInt(parts[3]);
                    addEdge(from, to, weight);
                }
            }
            updateAdjListDisplay();
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    public void performBFS(String startVertexText) {
        resetStyles();
        try {
            int start = Integer.parseInt(startVertexText.trim());
            if (!nodes.containsKey(start)) { adjListDisplay.setText("错误: 顶点不存在"); return; }
            BFS bfs = new BFS(graph); bfs.traverseFromVertex(start);
            highlightPath(bfs.getTraversalOrder(), "BFS", start);
        } catch (Exception e) { adjListDisplay.setText("错误: 输入无效"); }
    }

    public void performDFS(String startVertexText) {
        resetStyles();
        try {
            int start = Integer.parseInt(startVertexText.trim());
            if (!nodes.containsKey(start)) { adjListDisplay.setText("错误: 顶点不存在"); return; }
            DFS dfs = new DFS(graph); dfs.traverseFromVertex(start);
            highlightPath(dfs.getTraversalOrder(), "DFS", start);
        } catch (Exception e) { adjListDisplay.setText("错误: 输入无效"); }
    }

    public void performMST() {
        resetStyles();
        kruskal k = new kruskal(graph); Edge[] mst = k.generateMST();
        if (mst == null) { adjListDisplay.setText("无法生成MST"); return; }
        StringBuilder sb = new StringBuilder(graph.getAdjListString()); sb.append("\n\n=== MST 边 ===\n");
        for (Edge e : mst) {
            int min = Math.min(e.getMfrom(), e.getMto()); int max = Math.max(e.getMfrom(), e.getMto());
            String key = min + "-" + max;
            if (edges.containsKey(key)) { edges.get(key).line.setStroke(Color.GREEN); edges.get(key).line.setStrokeWidth(4); }
            sb.append(e.getMfrom()).append("-").append(e.getMto()).append(" (").append(e.getMweight()).append(")\n");
        }
        adjListDisplay.setText(sb.toString());
    }

    public void performDijkstra(String startText, String endText) {
        resetStyles();
        try {
            int start = Integer.parseInt(startText.trim());
            int end = Integer.parseInt(endText.trim());
            
            if (!nodes.containsKey(start) || !nodes.containsKey(end)) {
                adjListDisplay.setText("错误: 顶点不存在");
                return;
            }
            
            Dijkstra dijkstra = new Dijkstra(graph);
            List<Integer> path = dijkstra.findShortestPath(start, end);
            
            StringBuilder sb = new StringBuilder(graph.getAdjListString());
            sb.append("\n\n").append(dijkstra.getProcessLog());
            
            if (path.isEmpty() && start != end) {
                sb.append("\n结果: 无法从 ").append(start).append( " 到达 ").append(end);
                adjListDisplay.setText(sb.toString());
            } else {
                sb.append("\n=== 最短路径结果 ===\n");
                sb.append("路径: ");
                for (int i = 0; i < path.size(); i++) {
                    sb.append(path.get(i)).append(i < path.size() - 1 ? " -> " : "");
                }
                sb.append("\n总权重: ").append(dijkstra.getShortestDistance(end));
                adjListDisplay.setText(sb.toString());
                
                animatePath(path);
            }
        } catch (NumberFormatException e) {
            adjListDisplay.setText("错误: 请输入有效的顶点编号");
        }
    }

    // --- 辅助方法 ---
    
    private void animatePath(List<Integer> path) {
        if (path.size() < 1) return;
        
        Timeline timeline = new Timeline();
        
        for (int i = 0; i < path.size(); i++) {
            final int index = i;
            final int vertexId = path.get(index);
            
            // 关键帧：点亮顶点
            KeyFrame kfVertex = new KeyFrame(Duration.millis(i * 800), e -> {
                Circle c = nodes.get(vertexId);
                if (c != null) {
                    c.setFill(Color.GOLD);
                    c.setRadius(25);
                }
            });
            timeline.getKeyFrames().add(kfVertex);
            
            // 关键帧：点亮边 (如果不是最后一个点)
            if (i < path.size() - 1) {
                final int nextVertexId = path.get(i + 1);
                KeyFrame kfEdge = new KeyFrame(Duration.millis(i * 800 + 400), e -> {
                    // 邻接表可能是无向图实现，尝试查找两种方向的边
                    int min = Math.min(vertexId, nextVertexId);
                    int max = Math.max(vertexId, nextVertexId);
                    String key = min + "-" + max;
                    EdgeUI edgeUI = edges.get(key);
                    
                    if (edgeUI != null) {
                        edgeUI.line.setStroke(Color.RED);
                        edgeUI.line.setStrokeWidth(4);
                    }
                });
                timeline.getKeyFrames().add(kfEdge);
            }
        }
        
        timeline.play();
    }

    private void updateAdjListDisplay() {
        if (graph != null) {
            adjListDisplay.setText(graph.getAdjListString());
        }
    }

    private void addVertexUIOnly(int id) {
        if (nodes.containsKey(id)) return;
        Circle circle = new Circle(20, Color.LIGHTGREEN);
        circle.setStroke(Color.BLACK);
        circle.setStrokeWidth(2);
        enableDrag(circle, id);
        Text label = new Text(String.valueOf(id));
        graphPane.getChildren().addAll(circle, label);
        nodes.put(id, circle);
        nodeLabels.put(id, label);
        updateNodePositions();
    }

    private void enableDrag(Circle circle, int id) {
        final class Delta { double x, y; }
        final Delta dragDelta = new Delta();
        circle.setOnMousePressed(e -> {
            dragDelta.x = circle.getCenterX() - e.getX();
            dragDelta.y = circle.getCenterY() - e.getY();
            circle.setCursor(javafx.scene.Cursor.MOVE);
            e.consume();
        });
        circle.setOnMouseDragged(e -> {
            double newX = e.getX() + dragDelta.x;
            double newY = e.getY() + dragDelta.y;
            newX = Math.max(20, Math.min(graphPane.getPrefWidth() - 20, newX));
            newY = Math.max(20, Math.min(graphPane.getPrefHeight() - 20, newY));
            circle.setCenterX(newX); circle.setCenterY(newY);
            Text label = nodeLabels.get(id);
            if (label != null) { label.setX(newX - 5); label.setY(newY + 5); }
            updateConnectedEdges(id);
            e.consume();
        });
        circle.setOnMouseReleased(e -> circle.setCursor(javafx.scene.Cursor.HAND));
    }
    
    private void updateConnectedEdges(int id) {
        for (Map.Entry<String, EdgeUI> entry : edges.entrySet()) {
            String[] parts = entry.getKey().split("-");
            int v1 = Integer.parseInt(parts[0]);
            int v2 = Integer.parseInt(parts[1]);
            if (v1 == id || v2 == id) {
                Circle c1 = nodes.get(v1); Circle c2 = nodes.get(v2);
                Line line = entry.getValue().line; Text label = entry.getValue().label;
                line.setStartX(c1.getCenterX()); line.setStartY(c1.getCenterY());
                line.setEndX(c2.getCenterX());   line.setEndY(c2.getCenterY());
                label.setX((c1.getCenterX() + c2.getCenterX()) / 2);
                label.setY((c1.getCenterY() + c2.getCenterY()) / 2 - 5);
            }
        }
    }

    public void addVertex(int id) {
        if (nodes.containsKey(id)) return;
        graph.addVertex();
        addVertexUIOnly(id);
        updateAdjListDisplay();
    }

    public void removeVertex(int id) {
        Circle circle = nodes.remove(id);
        Text label = nodeLabels.remove(id);
        if (circle != null) graphPane.getChildren().remove(circle);
        if (label != null) graphPane.getChildren().remove(label);
        Iterator<Map.Entry<String, EdgeUI>> it = edges.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, EdgeUI> entry = it.next();
            String[] parts = entry.getKey().split("-");
            int v1 = Integer.parseInt(parts[0]);
            int v2 = Integer.parseInt(parts[1]);
            if (v1 == id || v2 == id) {
                graph.delEdge(v1, v2);
                graphPane.getChildren().removeAll(entry.getValue().line, entry.getValue().label);
                it.remove();
            }
        }
        updateNodePositions();
        updateAdjListDisplay();
    }

    public void addEdge(int from, int to, int weight) {
        if (!nodes.containsKey(from) || !nodes.containsKey(to)) return;
        if (from == to) return;
        graph.setEdge(from, to, weight);
        int min = Math.min(from, to); int max = Math.max(from, to);
        String edgeKey = min + "-" + max;
        if (edges.containsKey(edgeKey)) {
            EdgeUI oldEdge = edges.get(edgeKey);
            graphPane.getChildren().removeAll(oldEdge.line, oldEdge.label);
            edges.remove(edgeKey);
        }
        Circle c1 = nodes.get(from); Circle c2 = nodes.get(to);
        Line line = new Line(c1.getCenterX(), c1.getCenterY(), c2.getCenterX(), c2.getCenterY());
        line.setStrokeWidth(2); line.setStroke(Color.GRAY);
        Text text = new Text((c1.getCenterX() + c2.getCenterX()) / 2, (c1.getCenterY() + c2.getCenterY()) / 2 - 5, String.valueOf(weight));
        text.setFill(Color.DARKRED); text.setStyle("-fx-font-weight: bold;");
        graphPane.getChildren().add(0, line); graphPane.getChildren().add(text);
        edges.put(edgeKey, new EdgeUI(line, text));
        updateAdjListDisplay();
    }

    public void removeEdge(int from, int to) {
        graph.delEdge(from, to);
        int min = Math.min(from, to); int max = Math.max(from, to);
        String key = min + "-" + max;
        EdgeUI edgeUI = edges.remove(key);
        if (edgeUI != null) graphPane.getChildren().removeAll(edgeUI.line, edgeUI.label);
        updateAdjListDisplay();
    }

    private void updateNodePositions() {
        int n = nodes.size();
        if (n == 0) return;
        double centerX = 300; double centerY = 250; double radius = 150;
        int i = 0;
        List<Integer> sortedKeys = nodes.keySet().stream().sorted().toList();
        for (Integer id : sortedKeys) {
            double angle = 2 * Math.PI * i / n - Math.PI / 2;
            double x = centerX + radius * Math.cos(angle);
            double y = centerY + radius * Math.sin(angle);
            Circle c = nodes.get(id);
            c.setCenterX(x); c.setCenterY(y);
            Text t = nodeLabels.get(id);
            t.setX(x - 5); t.setY(y + 5);
            i++;
        }
        for (Map.Entry<String, EdgeUI> entry : edges.entrySet()) {
             String[] parts = entry.getKey().split("-");
             int v1 = Integer.parseInt(parts[0]); int v2 = Integer.parseInt(parts[1]);
             Circle c1 = nodes.get(v1); Circle c2 = nodes.get(v2);
             if(c1!=null && c2!=null){
                 entry.getValue().line.setStartX(c1.getCenterX());
                 entry.getValue().line.setStartY(c1.getCenterY());
                 entry.getValue().line.setEndX(c2.getCenterX());
                 entry.getValue().line.setEndY(c2.getCenterY());
                 entry.getValue().label.setX((c1.getCenterX()+c2.getCenterX())/2);
                 entry.getValue().label.setY((c1.getCenterY()+c2.getCenterY())/2-5);
             }
        }
    }

    private void highlightPath(List<Integer> path, String algo, int start) {
        StringBuilder sb = new StringBuilder(graph.getAdjListString());
        sb.append("\n\n=== ").append(algo).append(" ===\n");
        for (int i = 0; i < path.size(); i++) {
            sb.append(path.get(i)).append(i < path.size()-1 ? " -> " : "");
            Circle c = nodes.get(path.get(i)); if (c != null) c.setFill(Color.ORANGE);
        }
        adjListDisplay.setText(sb.toString());
    }

    public void clearDisplay() { resetStyles(); updateAdjListDisplay(); }
    private void resetStyles() { 
        for (Circle c : nodes.values()) { 
            c.setFill(Color.LIGHTGREEN); 
            c.setStroke(Color.BLACK); 
            c.setRadius(20); 
        } 
        for (EdgeUI e : edges.values()) { 
            e.line.setStroke(Color.GRAY); 
            e.line.setStrokeWidth(2); 
        } 
    }
    public void clearAllEdges() { graph.clearAllEdges(); for (EdgeUI e : edges.values()) { graphPane.getChildren().removeAll(e.line, e.label); } edges.clear(); updateAdjListDisplay(); }
    public void generateRandomGraph() { clearAllEdges(); graph.generateRandomGraph(); int n = graph.verticesNumber(); for (int i = 0; i < n; i++) { Edge e = graph.firstEdge(i); while (e != null) { int from = e.getMfrom(); int to = e.getMto(); if (from < to) addEdge(from, to, e.getMweight()); e = graph.nextEdge(e); } } updateAdjListDisplay(); }
}