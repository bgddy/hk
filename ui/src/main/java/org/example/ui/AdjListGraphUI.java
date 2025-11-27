package org.example.ui;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
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
    private ScrollPane graphScrollPane;
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
    
    // === 动画控制相关变量 ===
    private Timeline autoPlayTimeline;
    // [修复] 恢复这个变量声明，供 animatePath 和 stopAnimation 使用
    private Timeline currentAnimation; 
    
    private List<TraversalStep> currentSteps = new ArrayList<>();
    private String currentAlgoType = "";
    private int currentStepIndex = 0;
    // 用于 Dijkstra/Prim 等算法记录当前路径状态
    private Map<Integer, String> currentPathEdges = new HashMap<>(); 

    public AdjListGraphUI(AdjListGraph graph) {
        this.graph = graph;
        root = new BorderPane();
        root.setPrefSize(1100, 650);
        
        // 中间：绘图区域
        graphPane = new Pane();
        graphPane.setPrefSize(2000, 2000); 
        graphPane.setStyle("-fx-background-color: #f8f9fa;");
        
        graphScrollPane = new ScrollPane(graphPane);
        graphScrollPane.setPannable(true);
        graphScrollPane.setFitToWidth(false); 
        graphScrollPane.setFitToHeight(false);
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

        // 右侧：垂直分割面板
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
        
        for (int i = 0; i < 5; i++) addVertexUIOnly(i);
        updateAdjListDisplay();
        setCode(new String[]{"// 等待算法运行...", "// 代码将在此显示"});

        centerContent();
        
        // 初始化自动播放 Timeline
        autoPlayTimeline = new Timeline(new KeyFrame(Duration.millis(800), e -> nextStep()));
        autoPlayTimeline.setCycleCount(Timeline.INDEFINITE);
    }
    
    public void centerContent() {
        Platform.runLater(() -> {
            graphScrollPane.setHvalue(0.5);
            graphScrollPane.setVvalue(0.5);
        });
    }

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

    // === 算法执行入口 ===

    public void performBFS(String startVertexText) {
        stopAnimation(); // 停止之前的动画
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
            // 初始化动画数据并开始播放
            initAnimation(bfs.getSteps(), "BFS");
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
            initAnimation(dfs.getSteps(), "DFS");
        } catch (Exception e) { adjListDisplay.setText("错误: " + e.getMessage()); }
    }

    public void performMST() { // Kruskal
        stopAnimation();
        resetStyles();
        String[] mstCode = {
            "// Kruskal MST Algorithm",
            "Sort all edges by weight",       // line 1
            "For each edge (u, v):",          // line 2
            "  if find(u) != find(v):",       // line 3 (add)
            "    union(u, v); add to MST",    // line 3 (add)
            "  else: ignore (cycle formed)"   // line 4 (reject)
        };
        setCode(mstCode);
        
        kruskal k = new kruskal(graph); 
        Edge[] mst = k.generateMST();
        
        if (mst == null) { 
            adjListDisplay.setText("无法生成MST (图可能不连通)"); 
            return; 
        }
        
        initAnimation(k.getSteps(), "MST");
        adjListDisplay.setText(graph.getAdjListString() + "\n\n[Kruskal] 最小生成树已生成。");
    }

    public void performPrim() {
        stopAnimation();
        resetStyles();
        String[] primCode = {
            "// Prim MST Algorithm",
            "1. PQ.poll() min node u",
            "2. Add edge (parent[u], u) to MST",
            "3. For each neighbor v of u:",
            "4.   If weight < dist[v]: update dist",
            "5.   Else: ignore"
        };
        setCode(primCode);
        
        prim p = new prim(graph);
        Edge[] mst = p.generateMST();
        
        initAnimation(p.getSteps(), "Prim");
        adjListDisplay.setText(graph.getAdjListString() + "\n\n[Prim] 最小生成树已生成。");
    }

    public void performDijkstra(String startText, String endText) {
        stopAnimation();
        resetStyles();
        
        // Use placeholder code for result view
        setCode(new String[]{
            "// Dijkstra Shortest Path Result", 
            "1. Run Algorithm (Background)", 
            "2. Trace Back Path", 
            "3. Visualize Result"
        });
        
        try {
            int start = Integer.parseInt(startText.trim());
            int end = Integer.parseInt(endText.trim());
            
            if (!nodes.containsKey(start) || !nodes.containsKey(end)) {
                 adjListDisplay.setText("Error: Vertex not found.");
                 return;
            }

            Dijkstra dijkstra = new Dijkstra(graph);
            List<Integer> path = dijkstra.findShortestPath(start, end);
            
            StringBuilder log = new StringBuilder();
            log.append("Path Search: ").append(start).append(" -> ").append(end).append("\n");
            
            int dist = dijkstra.getShortestDistance(end);
            if (dist == Integer.MAX_VALUE) {
                log.append("Result: Unreachable");
                adjListDisplay.setText(log.toString());
            } else {
                log.append("Total Distance: ").append(dist).append("\n");
                log.append("Path: ").append(path);
                adjListDisplay.setText(log.toString());
                
                // Show path animation
                animatePath(path);
            }
            
        } catch (NumberFormatException e) {
            adjListDisplay.setText("Invalid Input");
        } catch (Exception e) {
            adjListDisplay.setText("Error: " + e.getMessage());
        }
    }

    public void performDijkstraAll(String startText) {
        stopAnimation();
        resetStyles();
        // Updated code lines to match Dijkstra.java step indices (0-8)
        String[] code = {
            "init: dist[s]=0, PQ.add(s)",      // 0
            "while PQ not empty:",             // 1
            "  u = PQ.poll()",                 // 2
            "  if visited[u]: continue",       // 3
            "  visited[u] = true",             // 4
            "  for edge(u,v) in neighbors:",   // 5
            "    if dist[u]+w >= dist[v]:",    // 6 (fail check)
            "      dist[v] = dist[u]+w",       // 7 (success)
            "      PQ.add(v)"                  // 8
        };
        setCode(code);
        
        try {
            int start = Integer.parseInt(startText.trim());
            if (!nodes.containsKey(start)) {
                adjListDisplay.setText("错误: 顶点不存在");
                return;
            }
            
            Dijkstra dijkstra = new Dijkstra(graph);
            dijkstra.findShortestPath(start, -1); 
            
            adjListDisplay.setText(dijkstra.getAllPathsResult(start));
            initAnimation(dijkstra.getSteps(), "Dijkstra");
            
        } catch (NumberFormatException e) {
            adjListDisplay.setText("请输入有效的起点ID");
        }
    }

    // ================= 动画控制逻辑 =================

    public void play() {
        if (currentSteps == null || currentSteps.isEmpty()) return;
        autoPlayTimeline.play();
    }

    public void pause() {
        if (autoPlayTimeline != null) autoPlayTimeline.pause();
    }

    public void nextStep() {
        if (currentSteps == null || currentStepIndex >= currentSteps.size()) {
            pause();
            highlightCode(-1);
            return;
        }
        
        TraversalStep step = currentSteps.get(currentStepIndex);
        renderStep(step); // 执行当前步骤的视觉渲染
        
        currentStepIndex++;
    }

    public void resetAnimation() {
        pause();
        resetStyles();
        currentStepIndex = 0;
        currentPathEdges.clear();
        highlightCode(0);
    }
    
    // [修复] 停止动画方法：同时处理旧的 timeline 和新的 autoPlay
    private void stopAnimation() {
        if (currentAnimation != null) {
            currentAnimation.stop();
            currentAnimation = null;
        }
        pause(); // 停止自动播放
        highlightCode(-1);
    }

    // 初始化动画数据，并自动开始播放
    private void initAnimation(List<TraversalStep> steps, String algoType) {
        stopAnimation(); // 确保之前的停止
        this.currentSteps = steps;
        this.currentAlgoType = algoType;
        resetAnimation(); // 重置状态
        play(); // 自动开始
    }

    // 渲染单个步骤
    private void renderStep(TraversalStep step) {
        highlightCode(step.getLineIndex());
        
        // 计算当前边的唯一 Key
        String stepEdgeKey = "";
        if (step.getEdge() != null) {
            int u = step.getEdge().getMfrom();
            int v = step.getEdge().getMto();
            stepEdgeKey = Math.min(u, v) + "-" + Math.max(u, v);
        }

        String algoType = this.currentAlgoType;

        if (algoType.equals("BFS") || algoType.equals("DFS")) {
            switch (step.getType()) {
                case VISIT: highlightNode(step.getVertexId(), Color.ORANGE); break;
                case VISIT_EDGE: highlightEdge(step.getEdge(), Color.GREEN); break;
                case BACKTRACK: highlightNode(step.getVertexId(), Color.MEDIUMPURPLE); break;
                case RELAX_SUCCESS: highlightEdge(step.getEdge(), Color.RED); break; 
            }
        } 
        else if (algoType.equals("Dijkstra")) {
            switch (step.getType()) {
                case VISIT: 
                    if(step.getVertexId() != -1) highlightNode(step.getVertexId(), Color.ORANGE);
                    break;
                    
                case VISIT_EDGE:
                    // [关键] 只有当这条边 不是 当前已确认的红边时，才允许变色
                    if (!currentPathEdges.containsValue(stepEdgeKey)) {
                        if (step.getLineIndex() == 5) { // 检查中
                            highlightEdge(step.getEdge(), Color.CORNFLOWERBLUE);
                        } else if (step.getLineIndex() == 6) { // 松弛失败/未更新
                            highlightEdge(step.getEdge(), Color.LIGHTGRAY);
                        }
                    }
                    break;
                    
                case RELAX_SUCCESS:
                    Edge newEdge = step.getEdge();
                    int targetNode = newEdge.getMto();
                    
                    // 1. 变灰旧的路径边
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
                    
                    // 2. 变红新边
                    highlightEdge(newEdge, Color.RED);
                    currentPathEdges.put(targetNode, stepEdgeKey);
                    highlightNode(targetNode, Color.LIGHTGREEN);
                    break;
            }
        }
        // === Kruskal 和 Prim ===
        else if (algoType.equals("MST") || algoType.equals("Prim")) {
            switch (step.getType()) {
                case VISIT: 
                    // Prim 需要高亮当前节点
                    if(step.getVertexId() != -1) highlightNode(step.getVertexId(), Color.ORANGE);
                    break;
                case CHECK_EDGE: 
                    highlightEdge(step.getEdge(), Color.GOLD); 
                    break;
                case ADD_EDGE:
                    highlightEdge(step.getEdge(), Color.GREEN);
                    highlightNode(step.getEdge().getMfrom(), Color.LIGHTGREEN);
                    highlightNode(step.getEdge().getMto(), Color.LIGHTGREEN);
                    break;
                case REJECT_EDGE: 
                    highlightEdge(step.getEdge(), Color.LIGHTGRAY); 
                    break;
            }
        }
    }
    
    private void highlightNode(int id, Color color) {
        Circle c = nodes.get(id);
        if (c != null) {
            c.setFill(color);
            // 节点脉冲动画
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
            // 如果是灰色，恢复细线；如果是高亮色，加粗
            if (color.equals(Color.LIGHTGRAY) || color.equals(Color.GRAY)) {
                ui.line.setStrokeWidth(2);
            } else {
                ui.line.setStrokeWidth(4);
            }
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
        centerContent();
    }

    private void clearInternalGraphState() {
        stopAnimation(); // 确保停止
        resetStyles();
        graph.clearAllEdges(); 
        nodes.clear();
        nodeLabels.clear();
        graphPane.getChildren().clear();
        edges.clear();
    }

    public void saveGraph() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("保存图 (DSL)");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        fileChooser.setInitialFileName("graph_dsl.txt");
        File file = fileChooser.showSaveDialog(root.getScene().getWindow());
        
        if (file == null) return;
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            int n = graph.verticesNumber();
            for (int i = 0; i < n; i++) {
                for (org.example.core.Edge e = graph.firstEdge(i); e != null; e = graph.nextEdge(e)) {
                    if (e.getMfrom() < e.getMto()) {
                        writer.write(String.format("%d -> %d : %d\n", e.getMfrom(), e.getMto(), e.getMweight()));
                    }
                }
            }
            System.out.println("DSL 保存成功");
        } catch (IOException ex) { 
            ex.printStackTrace(); 
            adjListDisplay.setText("保存失败: " + ex.getMessage());
        }
    }

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
            renderFromDSL(dslContent.toString());
            System.out.println("DSL 加载成功");
        } catch (Exception ex) { 
            ex.printStackTrace(); 
            adjListDisplay.setText("加载失败: " + ex.getMessage());
        }
    }

    // 保留此方法用于备用或简单路径动画
    private void animatePath(List<Integer> path) {
        if (path.size() < 1) return;
        currentAnimation = new Timeline();
        for (int i = 0; i < path.size(); i++) {
            final int index = i;
            final int vertexId = path.get(index);
            KeyFrame kfVertex = new KeyFrame(Duration.millis(i * 800), e -> {
                Circle c = nodes.get(vertexId);
                if (c != null) { c.setFill(Color.GOLD); c.setRadius(25); }
                // highlightCode removed
            });
            currentAnimation.getKeyFrames().add(kfVertex);
            if (i < path.size() - 1) {
                final int nextVertexId = path.get(i + 1);
                KeyFrame kfEdge = new KeyFrame(Duration.millis(i * 800 + 400), e -> {
                    int min = Math.min(vertexId, nextVertexId);
                    int max = Math.max(vertexId, nextVertexId);
                    String key = min + "-" + max;
                    EdgeUI edgeUI = edges.get(key);
                    if (edgeUI != null) { edgeUI.line.setStroke(Color.RED); edgeUI.line.setStrokeWidth(4); }
                    // highlightCode removed
                });
                currentAnimation.getKeyFrames().add(kfEdge);
            }
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