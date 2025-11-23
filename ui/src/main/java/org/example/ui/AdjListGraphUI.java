package org.example.ui;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform; // 必须导入
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import org.example.core.*;

import java.io.*;
import java.util.*;

public class AdjListGraphUI {

    private BorderPane root;
    private Pane graphPane;
    private Text adjListDisplay;
    private ListView<String> codeListView;
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
    private Timeline currentAnimation;

    public AdjListGraphUI(AdjListGraph graph) {
        this.graph = graph;
        root = new BorderPane();
        root.setPrefSize(1100, 650);
        
        // ==================== 中间：绘图区域 ====================
        graphPane = new Pane();
        graphPane.setPrefSize(2000, 2000); // 画布很大
        graphPane.setStyle("-fx-background-color: #f8f9fa;");
        
        // 【关键修复1】移除 Group，直接放入 ScrollPane
        ScrollPane graphScrollPane = new ScrollPane(graphPane);
        graphScrollPane.setPannable(true);
        graphScrollPane.setFitToWidth(false); // 允许内容超过视口
        graphScrollPane.setFitToHeight(false);
        graphScrollPane.setStyle("-fx-background-color: transparent; -fx-border-color: #dee2e6; -fx-border-width: 1;");
        
        // 缩放按钮
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

        // ==================== 右侧：垂直分割面板 ====================
        VBox dataContainer = new VBox(5);
        dataContainer.setPadding(new Insets(10));
        dataContainer.setStyle("-fx-background-color: #ffffff;");
        
        Text adjListTitle = new Text("数据与日志");
        adjListTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-fill: #2c3e50;");
        
        ScrollPane logScrollPane = new ScrollPane();
        logScrollPane.setFitToWidth(true);
        logScrollPane.setStyle("-fx-background-color: transparent; -fx-background: #ffffff;");
        
        adjListDisplay = new Text();
        adjListDisplay.setStyle("-fx-font-family: 'Monaco', 'Menlo', 'Consolas', monospace; -fx-font-size: 11px; -fx-fill: #34495e;");
        adjListDisplay.wrappingWidthProperty().bind(logScrollPane.widthProperty().subtract(20));
        logScrollPane.setContent(adjListDisplay);
        
        dataContainer.getChildren().addAll(adjListTitle, logScrollPane);
        VBox.setVgrow(logScrollPane, Priority.ALWAYS);

        VBox codeContainer = new VBox(5);
        codeContainer.setPadding(new Insets(10));
        codeContainer.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-width: 1 0 0 0;");

        Text codeTitle = new Text("算法伪代码追踪");
        codeTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-fill: #1565c0;");

        codeListView = new ListView<>();
        codeListView.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-size: 13px;");
        
        codeListView.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (isSelected()) {
                        setStyle("-fx-background-color: #fff176; -fx-text-fill: #000; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-background-color: transparent; -fx-text-fill: #333;");
                    }
                }
            }
        });
        VBox.setVgrow(codeListView, Priority.ALWAYS);
        codeContainer.getChildren().addAll(codeTitle, codeListView);

        SplitPane rightSplitPane = new SplitPane();
        rightSplitPane.setOrientation(Orientation.VERTICAL);
        rightSplitPane.getItems().addAll(dataContainer, codeContainer);
        rightSplitPane.setDividerPositions(0.55);
        rightSplitPane.setPrefWidth(360);

        root.setCenter(centerStack);
        root.setRight(rightSplitPane);
        
        // 初始化图
        for (int i = 0; i < 5; i++) addVertexUIOnly(i);
        updateAdjListDisplay();
        setCode(new String[]{"// 等待算法运行...", "// 代码将在此显示"});

        // 【关键修复2】延迟执行滚动条居中，确保布局计算完成后再跳转
        Platform.runLater(() -> {
            graphScrollPane.setHvalue(0.5);
            graphScrollPane.setVvalue(0.5);
        });
    }
    
    // ... [中间的方法保持不变] ... 
    // 为了节省篇幅，我保留了所有逻辑方法，请确保 resetStyles 在类里面

    private void setCode(String[] lines) {
        codeListView.getItems().clear();
        codeListView.getItems().addAll(lines);
    }

    private void highlightCode(int lineIndex) {
        Platform.runLater(() -> {
            if (lineIndex >= 0 && lineIndex < codeListView.getItems().size()) {
                codeListView.getSelectionModel().select(lineIndex);
                codeListView.scrollTo(lineIndex);
            } else {
                codeListView.getSelectionModel().clearSelection();
            }
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
            updateNodePositions();
        }
    }

    public BorderPane getPane() { return root; }

    public void performBFS(String startVertexText) {
        stopAnimation();
        resetStyles();
        String[] bfsCode = {
            "Q.enqueue(start); visited[start]=true",
            "while Q is not empty:",
            "  u = Q.dequeue()",
            "  for each neighbor v of u:",
            "    if v is not visited:",
            "      visited[v]=true; Q.enqueue(v)"
        };
        setCode(bfsCode);
        try {
            int start = Integer.parseInt(startVertexText.trim());
            if (!nodes.containsKey(start)) { adjListDisplay.setText("错误: 顶点不存在"); return; }
            BFS bfs = new BFS(graph); 
            bfs.traverseFromVertex(start);
            adjListDisplay.setText(graph.getAdjListString() + "\n\n" + bfs.getTraversalResult());
            animateSteps(bfs.getSteps(), "BFS");
        } catch (Exception e) { adjListDisplay.setText("错误: " + e.getMessage()); }
    }

    public void performDFS(String startVertexText) {
        stopAnimation();
        resetStyles();
        String[] dfsCode = {
            "function DFS(u):",
            "  visited[u] = true",
            "  process(u)",
            "  for each neighbor v of u:",
            "    if v is not visited:",
            "      DFS(v)",
            "  // backtrack"
        };
        setCode(dfsCode);
        try {
            int start = Integer.parseInt(startVertexText.trim());
            if (!nodes.containsKey(start)) { adjListDisplay.setText("错误: 顶点不存在"); return; }
            DFS dfs = new DFS(graph); 
            dfs.traverseFromVertex(start);
            adjListDisplay.setText(graph.getAdjListString() + "\n\n" + dfs.getTraversalResult());
            animateSteps(dfs.getSteps(), "DFS");
        } catch (Exception e) { adjListDisplay.setText("错误: " + e.getMessage()); }
    }

    private void stopAnimation() {
        if (currentAnimation != null) {
            currentAnimation.stop();
            currentAnimation = null;
        }
        highlightCode(-1);
    }

    private void animateSteps(List<TraversalStep> steps, String algoType) {
        if (steps == null || steps.isEmpty()) return;
        currentAnimation = new Timeline();
        double delayPerStep = 1000;
        currentAnimation.getKeyFrames().add(new KeyFrame(Duration.ZERO, e -> highlightCode(0)));
        for (int i = 0; i < steps.size(); i++) {
            TraversalStep step = steps.get(i);
            double time = (i + 1) * delayPerStep;
            KeyFrame kf = new KeyFrame(Duration.millis(time), e -> {
                highlightCode(step.getLineIndex());
                switch (step.getType()) {
                    case VISIT: highlightNode(step.getVertexId(), Color.ORANGE); break;
                    case VISIT_EDGE: highlightEdge(step.getEdge(), Color.GREEN); break;
                    case BACKTRACK: highlightNode(step.getVertexId(), Color.MEDIUMPURPLE); break;
                }
                if (algoType.equals("BFS")) {
                    switch (step.getType()) {
                        case VISIT: highlightCode(4); break;
                        case VISIT_EDGE: highlightCode(5); break;
                        default: highlightCode(3);
                    }
                } else if (algoType.equals("DFS")) {
                    switch (step.getType()) {
                        case VISIT: highlightCode(1); break;
                        case VISIT_EDGE: highlightCode(5); break;
                        case BACKTRACK: highlightCode(6); break;
                    }
                }
            });
            currentAnimation.getKeyFrames().add(kf);
        }
        currentAnimation.setOnFinished(e -> highlightCode(-1));
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
                    while (graph.verticesNumber() <= Math.max(u, v)) graph.addVertex(); 
                    addVertexUIOnly(u); addVertexUIOnly(v);
                    edgesToAdd.add(new int[]{u, v, w});
                } catch (Exception e) {}
            }
        }
        for (int[] edge : edgesToAdd) addEdge(edge[0], edge[1], edge[2]);
        updateNodePositions();
        updateAdjListDisplay();
    }

    public void resetToDefault() {
        this.graph = new AdjListGraph(5);
        clearInternalGraphState();
        for (int i = 0; i < 5; i++) addVertexUIOnly(i);
        updateNodePositions();
        updateAdjListDisplay();
        setCode(new String[]{"// 准备就绪"});
        // 重置时也居中一下
        Platform.runLater(() -> {
            ScrollPane sp = (ScrollPane) ((StackPane)root.getCenter()).getChildren().get(0);
            sp.setHvalue(0.5); sp.setVvalue(0.5);
        });
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
        fileChooser.setTitle("保存图");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Graph Files", "*.graph"));
        File file = fileChooser.showSaveDialog(root.getScene().getWindow());
        if (file == null) return;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (Map.Entry<Integer, Circle> entry : nodes.entrySet()) {
                writer.write(String.format("V,%d,%.2f,%.2f\n", entry.getKey(), entry.getValue().getCenterX(), entry.getValue().getCenterY()));
            }
            int n = graph.verticesNumber();
            for (int i = 0; i < n; i++) {
                for (org.example.core.Edge e = graph.firstEdge(i); e != null; e = graph.nextEdge(e)) {
                    if (e.getMfrom() < e.getMto()) writer.write(String.format("E,%d,%d,%d\n", e.getMfrom(), e.getMto(), e.getMweight()));
                }
            }
        } catch (IOException ex) { ex.printStackTrace(); }
    }

    public void loadGraph() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("打开图");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Graph Files", "*.graph"));
        File file = fileChooser.showOpenDialog(root.getScene().getWindow());
        if (file == null) return;
        clearInternalGraphState();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts[0].equals("V")) {
                    int id = Integer.parseInt(parts[1]);
                    if (id >= graph.verticesNumber()) while(graph.verticesNumber() <= id) graph.addVertex();
                    addVertexUIOnly(id);
                    Circle c = nodes.get(id);
                    if (c!=null) { c.setCenterX(Double.parseDouble(parts[2])); c.setCenterY(Double.parseDouble(parts[3])); updateNodePositions();}
                } else if (parts[0].equals("E")) {
                    addEdge(Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
                }
            }
            updateAdjListDisplay();
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    public void performMST() {
        stopAnimation();
        resetStyles();
        setCode(new String[]{"// Kruskal MST", "Sort edges", "For each edge:", "  If not connected -> Union & Add"});
        kruskal k = new kruskal(graph); Edge[] mst = k.generateMST();
        if (mst == null) { adjListDisplay.setText("无法生成MST"); return; }
        for (Edge e : mst) {
            int min = Math.min(e.getMfrom(), e.getMto());
            int max = Math.max(e.getMfrom(), e.getMto());
            EdgeUI ui = edges.get(min + "-" + max);
            if (ui != null) { ui.line.setStroke(Color.GREEN); ui.line.setStrokeWidth(4); }
        }
        highlightCode(5); 
    }

    public void performDijkstra(String startText, String endText) {
        stopAnimation();
        resetStyles();
        setCode(new String[]{"// Dijkstra", "dist[start]=0", "while PQ not empty:", "  u = PQ.poll()", "  relax neighbors"});
        try {
            int start = Integer.parseInt(startText.trim());
            int end = Integer.parseInt(endText.trim());
            if (!nodes.containsKey(start) || !nodes.containsKey(end)) return;
            Dijkstra dijkstra = new Dijkstra(graph);
            List<Integer> path = dijkstra.findShortestPath(start, end);
            adjListDisplay.setText(dijkstra.getProcessLog());
            animatePath(path);
        } catch (Exception e) {}
    }

    private void animatePath(List<Integer> path) {
        if (path.size() < 1) return;
        currentAnimation = new Timeline();
        for (int i = 0; i < path.size(); i++) {
            int v = path.get(i);
            currentAnimation.getKeyFrames().add(new KeyFrame(Duration.millis(i * 800), e -> {
                if(nodes.get(v)!=null) nodes.get(v).setFill(Color.GOLD);
            }));
        }
        currentAnimation.play();
    }

    private void updateAdjListDisplay() { if (graph != null) adjListDisplay.setText(graph.getAdjListString()); }

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
            e.consume();
        });
        circle.setOnMouseDragged(e -> {
            double newX = Math.max(20, Math.min(graphPane.getPrefWidth()-20, e.getX() + dragDelta.x));
            double newY = Math.max(20, Math.min(graphPane.getPrefHeight()-20, e.getY() + dragDelta.y));
            circle.setCenterX(newX); circle.setCenterY(newY);
            updateNodePositions();
            e.consume();
        });
    }
    
    private void updateConnectedEdges(int id) { updateNodePositions(); } 

    public void addVertex(int id) { if (!nodes.containsKey(id)) { graph.addVertex(); addVertexUIOnly(id); updateAdjListDisplay(); } }
    
    public void removeVertex(int id) {
        if (!nodes.containsKey(id)) return;
        graphPane.getChildren().removeAll(nodes.get(id), nodeLabels.get(id));
        nodes.remove(id); nodeLabels.remove(id);
        updateNodePositions(); 
        updateAdjListDisplay();
    }

    public void addEdge(int from, int to, int weight) {
        if (from==to) return;
        graph.setEdge(from, to, weight);
        String key = Math.min(from,to) + "-" + Math.max(from,to);
        if (!edges.containsKey(key)) {
            Line line = new Line(); line.setStrokeWidth(2); line.setStroke(Color.GRAY);
            Text text = new Text(String.valueOf(weight)); text.setFill(Color.DARKRED);
            graphPane.getChildren().add(0, line); graphPane.getChildren().add(text);
            edges.put(key, new EdgeUI(line, text));
        }
        updateNodePositions();
        updateAdjListDisplay();
    }

    public void removeEdge(int from, int to) {
        graph.delEdge(from, to);
        String key = Math.min(from,to) + "-" + Math.max(from,to);
        EdgeUI ui = edges.remove(key);
        if (ui != null) graphPane.getChildren().removeAll(ui.line, ui.label);
        updateAdjListDisplay();
    }

    private void updateNodePositions() {
        int n = nodes.size();
        if (n == 0) return;
        double baseRadius = 150;
        double radius = baseRadius * currentScale; 
        // 中心点固定在 2000x2000 画布的中心
        double centerX = 1000; 
        double centerY = 1000;
        
        int i = 0;
        List<Integer> sortedKeys = nodes.keySet().stream().sorted().toList();
        for (Integer id : sortedKeys) {
            double angle = 2 * Math.PI * i / n - Math.PI / 2;
            double x = centerX + radius * Math.cos(angle);
            double y = centerY + radius * Math.sin(angle);
            Circle c = nodes.get(id);
            if (c != null) { c.setCenterX(x); c.setCenterY(y); }
            Text t = nodeLabels.get(id);
            if(t!=null) { t.setX(x-5); t.setY(y+5); }
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

    public void clearDisplay() { resetStyles(); updateAdjListDisplay(); highlightCode(-1); }

    // 【关键方法】确保 resetStyles 存在于类中
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

    public void clearAllEdges() { graph.clearAllEdges(); for(EdgeUI ui:edges.values()) graphPane.getChildren().removeAll(ui.line, ui.label); edges.clear(); updateAdjListDisplay(); }
    public void generateRandomGraph() { clearAllEdges(); graph.generateRandomGraph(); int n=graph.verticesNumber(); for(int i=0; i<n; i++) { Edge e=graph.firstEdge(i); while(e!=null) { if(e.getMfrom()<e.getMto()) addEdge(e.getMfrom(), e.getMto(), e.getMweight()); e=graph.nextEdge(e); } } }
}