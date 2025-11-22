package org.example.ui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.KeyValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import org.example.core.*;

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
    
    private double currentScale = 1.0;

    private Map<Integer, Circle> nodes = new HashMap<>();
    private Map<Integer, Text> nodeLabels = new HashMap<>();

    private static class EdgeUI {
        Line line;
        Text label;
        EdgeUI(Line line, Text label) { this.line = line; this.label = label; }
    }

    private Map<String, EdgeUI> edges = new HashMap<>();
    
    // 动画对象
    private Timeline currentAnimation;

    public AdjListGraphUI(AdjListGraph graph) {
        this.graph = graph;
        root = new BorderPane();
        root.setPrefSize(1000, 600);
        
        graphPane = new Pane();
        graphPane.setPrefSize(2000, 2000); 
        graphPane.setStyle("-fx-background-color: #f8f9fa;");
        
        Group scrollContent = new Group(graphPane);
        ScrollPane graphScrollPane = new ScrollPane(scrollContent);
        graphScrollPane.setPrefSize(650, 600);
        graphScrollPane.setPannable(true);
        graphScrollPane.setStyle("-fx-background-color: transparent; -fx-border-color: #dee2e6; -fx-border-width: 1;");
        
        // 缩放按钮
        Button zoomInBtn = createZoomButton("放大", 1.2);
        Button zoomOutBtn = createZoomButton("缩小", 0.8);
        VBox zoomControls = new VBox(10, zoomInBtn, zoomOutBtn);
        zoomControls.setAlignment(Pos.CENTER);
        zoomControls.setPadding(new Insets(20));
        zoomControls.setPickOnBounds(false);
        // 【关键修改】设置最大尺寸为首选尺寸，防止 VBox 被 StackPane 拉伸填满整个区域
        zoomControls.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        
        StackPane centerStack = new StackPane();
        centerStack.getChildren().addAll(graphScrollPane, zoomControls);
        
        // 将按钮固定在右上角
        StackPane.setAlignment(zoomControls, Pos.TOP_RIGHT);

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
        
        root.setCenter(centerStack);
        root.setRight(rightPane);
        
        for (int i = 0; i < 5; i++) addVertexUIOnly(i);
        graphScrollPane.setHvalue(0.5); graphScrollPane.setVvalue(0.5);
        
        updateAdjListDisplay();
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
            updateNodePositions();
        }
    }

    public BorderPane getPane() { return root; }

    public void performBFS(String startVertexText) {
        stopAnimation();
        resetStyles();
        try {
            int start = Integer.parseInt(startVertexText.trim());
            if (!nodes.containsKey(start)) { adjListDisplay.setText("错误: 顶点不存在"); return; }
            
            BFS bfs = new BFS(graph); 
            bfs.traverseFromVertex(start);
            
            adjListDisplay.setText(graph.getAdjListString() + "\n\n" + bfs.getTraversalResult());
            animateSteps(bfs.getSteps(), "BFS 动态扩散");
            
        } catch (Exception e) { 
            adjListDisplay.setText("错误: " + e.getMessage()); 
            e.printStackTrace();
        }
    }

    public void performDFS(String startVertexText) {
        stopAnimation();
        resetStyles();
        try {
            int start = Integer.parseInt(startVertexText.trim());
            if (!nodes.containsKey(start)) { adjListDisplay.setText("错误: 顶点不存在"); return; }
            
            DFS dfs = new DFS(graph); 
            dfs.traverseFromVertex(start);
            
            adjListDisplay.setText(graph.getAdjListString() + "\n\n" + dfs.getTraversalResult());
            animateSteps(dfs.getSteps(), "DFS 回溯演示");
            
        } catch (Exception e) { 
            adjListDisplay.setText("错误: " + e.getMessage());
            e.printStackTrace(); 
        }
    }

    private void stopAnimation() {
        if (currentAnimation != null) {
            currentAnimation.stop();
            currentAnimation = null;
        }
    }

    // 核心动画逻辑: 处理 TraversalStep
    private void animateSteps(List<TraversalStep> steps, String title) {
        if (steps == null || steps.isEmpty()) return;
        
        currentAnimation = new Timeline();
        double delayPerStep = 800; // 动画间隔
        
        for (int i = 0; i < steps.size(); i++) {
            TraversalStep step = steps.get(i);
            double time = (i + 1) * delayPerStep;
            
            KeyFrame kf = new KeyFrame(Duration.millis(time), e -> {
                switch (step.getType()) {
                    case VISIT:
                        // 访问节点：变为橙色，并有脉冲效果
                        highlightNode(step.getVertexId(), Color.ORANGE);
                        break;
                    case VISIT_EDGE:
                        // 访问边：变为绿色 (扩散/探索)
                        highlightEdge(step.getEdge(), Color.GREEN);
                        break;
                    case BACKTRACK:
                        // 回溯：变为浅紫色/灰色，表示回退
                        highlightNode(step.getVertexId(), Color.MEDIUMPURPLE);
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
            // 脉冲动画效果
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
            ui.line.setStrokeWidth(4);
        }
    }

    public void renderFromDSL(String dslText) {
        if (dslText == null || dslText.trim().isEmpty()) return;
        
        this.graph = new AdjListGraph(5);
        clearInternalGraphState(); 
        for (int i = 0; i < 5; i++) addVertexUIOnly(i);

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
                    
                    while (graph.verticesNumber() <= Math.max(u, v)) { 
                        graph.addVertex(); 
                    }
                    addVertexUIOnly(u); addVertexUIOnly(v);
                    edgesToAdd.add(new int[]{u, v, w});
                } catch (Exception e) { System.out.println("DSL 解析错误: " + line); }
            }
        }
        for (int[] edge : edgesToAdd) { addEdge(edge[0], edge[1], edge[2]); }
        updateNodePositions();
        updateAdjListDisplay();
        adjListDisplay.setText(adjListDisplay.getText() + "\n\n[DSL 渲染完成]");
    }

    public void resetToDefault() {
        this.graph = new AdjListGraph(5);
        clearInternalGraphState();
        for (int i = 0; i < 5; i++) { addVertexUIOnly(i); }
        updateNodePositions();
        updateAdjListDisplay();
        adjListDisplay.setText(adjListDisplay.getText() + "\n\n[已恢复初始设置]");
    }

    private void clearInternalGraphState() {
        stopAnimation();
        resetStyles();
        graph.clearAllEdges(); 
        nodes.clear();
        nodeLabels.clear();
        graphPane.getChildren().clear();
        edges.clear();
    }

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

    public void performMST() {
        stopAnimation();
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
        stopAnimation();
        resetStyles();
        try {
            int start = Integer.parseInt(startText.trim());
            int end = Integer.parseInt(endText.trim());
            if (!nodes.containsKey(start) || !nodes.containsKey(end)) { adjListDisplay.setText("错误: 顶点不存在"); return; }
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
                for (int i = 0; i < path.size(); i++) { sb.append(path.get(i)).append(i < path.size() - 1 ? " -> " : ""); }
                sb.append("\n总权重: ").append(dijkstra.getShortestDistance(end));
                adjListDisplay.setText(sb.toString());
                animatePath(path);
            }
        } catch (NumberFormatException e) { adjListDisplay.setText("错误: 请输入有效的顶点编号"); }
    }

    private void animatePath(List<Integer> path) {
        if (path.size() < 1) return;
        currentAnimation = new Timeline();
        for (int i = 0; i < path.size(); i++) {
            final int index = i; final int vertexId = path.get(index);
            KeyFrame kfVertex = new KeyFrame(Duration.millis(i * 800), e -> {
                Circle c = nodes.get(vertexId);
                if (c != null) { c.setFill(Color.GOLD); c.setRadius(25); }
            });
            currentAnimation.getKeyFrames().add(kfVertex);
            if (i < path.size() - 1) {
                final int nextVertexId = path.get(i + 1);
                KeyFrame kfEdge = new KeyFrame(Duration.millis(i * 800 + 400), e -> {
                    int min = Math.min(vertexId, nextVertexId); int max = Math.max(vertexId, nextVertexId);
                    String key = min + "-" + max; EdgeUI edgeUI = edges.get(key);
                    if (edgeUI != null) { edgeUI.line.setStroke(Color.RED); edgeUI.line.setStrokeWidth(4); }
                });
                currentAnimation.getKeyFrames().add(kfEdge);
            }
        }
        currentAnimation.play();
    }

    private void updateAdjListDisplay() { if (graph != null) { adjListDisplay.setText(graph.getAdjListString()); } }

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
            int v1 = Integer.parseInt(parts[0]); int v2 = Integer.parseInt(parts[1]);
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
            int v1 = Integer.parseInt(parts[0]); int v2 = Integer.parseInt(parts[1]);
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
        if (!nodes.containsKey(from)) { if (from >= graph.verticesNumber()) { while(graph.verticesNumber() <= from) graph.addVertex(); } addVertexUIOnly(from); }
        if (!nodes.containsKey(to)) { if (to >= graph.verticesNumber()) { while(graph.verticesNumber() <= to) graph.addVertex(); } addVertexUIOnly(to); }
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
        double baseRadius = 150;
        double radius = baseRadius * currentScale; 
        double requiredSize = Math.max(2000, radius * 2 + 400);
        graphPane.setPrefSize(requiredSize, requiredSize);
        double centerX = requiredSize / 2; 
        double centerY = requiredSize / 2;
        
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