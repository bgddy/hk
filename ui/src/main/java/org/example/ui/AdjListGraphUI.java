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
    
    private Timeline autoPlayTimeline;
    private Timeline currentAnimation; 
    
    private List<TraversalStep> currentSteps = new ArrayList<>();
    private String currentAlgoType = "";
    private int currentStepIndex = 0;
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
        graphScrollPane.setPannable(true); // 允许拖拽画布
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

        // 右侧数据面板配置...
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
                if (empty || item == null) { setText(null); setStyle(""); } 
                else { 
                    setText(item);
                    if (isSelected()) setStyle("-fx-background-color: #fff176; -fx-text-fill: #000; -fx-font-weight: bold;");
                    else setStyle("-fx-background-color: transparent; -fx-text-fill: #333;");
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
        
        // 初始节点
        for (int i = 0; i < 5; i++) addVertexUIOnly(i, -1, -1);
        applyCircularLayout(); // 仅初始化时使用圆环布局，后续保持自定义位置
        updateAdjListDisplay();
        setCode(new String[]{"// 等待算法运行...", "// 代码将在此显示"});

        centerContent();
        
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

    // 优化缩放：基于中心点缩放
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
                    
                    // 限制边界
                    newX = Math.max(20, Math.min(graphPane.getPrefWidth() - 20, newX));
                    newY = Math.max(20, Math.min(graphPane.getPrefHeight() - 20, newY));
                    
                    c.setCenterX(newX);
                    c.setCenterY(newY);
                    Text t = nodeLabels.get(id);
                    if (t != null) { t.setX(newX - 5); t.setY(newY + 5); }
                }
            }
            updateAllEdges(); // 仅更新线条端点，不重置节点位置
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
            "Sort all edges by weight",       
            "For each edge (u, v):",          
            "  if find(u) != find(v):",       
            "    union(u, v); add to MST",    
            "  else: ignore (cycle formed)"   
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
        String[] code = {
            "init: dist[s]=0, PQ.add(s)",      
            "while PQ not empty:",             
            "  u = PQ.poll()",                 
            "  if visited[u]: continue",       
            "  visited[u] = true",             
            "  for edge(u,v) in neighbors:",   
            "    if dist[u]+w >= dist[v]:",    
            "      dist[v] = dist[u]+w",       
            "      PQ.add(v)"                  
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
        renderStep(step); 
        currentStepIndex++;
    }

    public void resetAnimation() {
        pause();
        resetStyles();
        currentStepIndex = 0;
        currentPathEdges.clear();
        highlightCode(0);
    }
    
    private void stopAnimation() {
        if (currentAnimation != null) {
            currentAnimation.stop();
            currentAnimation = null;
        }
        pause(); // 停止自动播放
        highlightCode(-1);
    }

    private void initAnimation(List<TraversalStep> steps, String algoType) {
        stopAnimation(); 
        this.currentSteps = steps;
        this.currentAlgoType = algoType;
        resetAnimation(); 
        play(); 
    }

    private void renderStep(TraversalStep step) {
        highlightCode(step.getLineIndex());
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
        }
        else if (algoType.equals("MST") || algoType.equals("Prim")) {
            switch (step.getType()) {
                case VISIT: 
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
                    int min = Math.min(vertexId, nextVertexId);
                    int max = Math.max(vertexId, nextVertexId);
                    String key = min + "-" + max;
                    EdgeUI edgeUI = edges.get(key);
                    if (edgeUI != null) { edgeUI.line.setStroke(Color.RED); edgeUI.line.setStrokeWidth(4); }
                });
                currentAnimation.getKeyFrames().add(kfEdge);
            }
        }
        currentAnimation.play();
    }

    // === 修改：解析 DSL 时支持增量更新 ===
    public void renderFromDSL(String dslText) {
        if (dslText == null || dslText.trim().isEmpty()) return;
        
        // 注释掉清空逻辑，实现增量更新
        // this.graph = new AdjListGraph(5);
        // clearInternalGraphState(); 
        // for (int i = 0; i < 5; i++) addVertexUIOnly(i, -1, -1);
        
        String[] lines = dslText.split("\n");

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;
            
            // 解析 POS 指令
            if (line.startsWith("POS")) {
                try {
                    String[] parts = line.split(" ");
                    int id = Integer.parseInt(parts[1]);
                    double x = Double.parseDouble(parts[2]);
                    double y = Double.parseDouble(parts[3]);
                    
                    // 确保图足够大
                    while (graph.verticesNumber() <= id) graph.addVertex();
                    
                    if (!nodes.containsKey(id)) {
                        addVertexUIOnly(id, x, y);
                    } else {
                        // 更新位置
                        Circle c = nodes.get(id);
                        c.setCenterX(x); c.setCenterY(y);
                        Text t = nodeLabels.get(id);
                        if(t != null) { t.setX(x-5); t.setY(y+5); }
                        updateConnectedEdges(id);
                    }
                } catch (Exception e) { System.out.println("POS parse error: " + line); }
                continue;
            }

            // 解析边
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
                    if (!nodes.containsKey(u)) addVertexUIOnly(u, -1, -1);
                    if (!nodes.containsKey(v)) addVertexUIOnly(v, -1, -1);

                    // 检查边是否存在且权重一致
                    boolean needUpdate = true;
                    // 在 AdjListGraph 中查找该边
                    org.example.core.Edge existingEdge = null;
                    for(org.example.core.Edge e = graph.firstEdge(u); e != null; e = graph.nextEdge(e)) {
                        if (e.getMto() == v) {
                            existingEdge = e;
                            break;
                        }
                    }
                    
                    if (existingEdge != null && existingEdge.getMweight() == w) {
                        needUpdate = false;
                    }

                    if (needUpdate) {
                        addEdge(u, v, w);
                    }
                    
                } catch (Exception e) {}
            }
        }
        
        updateAllEdges();
        updateAdjListDisplay();
    }

    public void resetToDefault() {
        this.graph = new AdjListGraph(5);
        clearInternalGraphState();
        for (int i = 0; i < 5; i++) addVertexUIOnly(i, -1, -1);
        applyCircularLayout(); // 重置时恢复圆环布局
        updateAdjListDisplay();
        setCode(new String[]{"// 准备就绪"});
        centerContent();
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
        fileChooser.setTitle("保存图 (DSL)");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        fileChooser.setInitialFileName("graph_dsl.txt");
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
            // 清空当前状态后再加载，如果你希望 loadGraph 是完全替换，可以保留 clearInternalGraphState
            // 但因为我们修改了 renderFromDSL 为增量，如果希望 loadGraph 是覆盖，可以在这里调用 resetToDefault
            resetToDefault(); 
            renderFromDSL(dslContent.toString());
            System.out.println("DSL 加载成功");
        } catch (Exception ex) { 
            ex.printStackTrace(); 
            adjListDisplay.setText("加载失败: " + ex.getMessage());
        }
    }

    private void updateAdjListDisplay() { if (graph != null) adjListDisplay.setText(graph.getAdjListString()); }

    private void addVertexUIOnly(int id, double x, double y) {
        if (nodes.containsKey(id)) return;
        Circle circle = new Circle(20, Color.LIGHTGREEN);
        circle.setStroke(Color.BLACK);
        circle.setStrokeWidth(2);
        
        if (x == -1 || y == -1) {
            circle.setCenterX(1000);
            circle.setCenterY(1000);
        } else {
            circle.setCenterX(x);
            circle.setCenterY(y);
        }
        
        enableDrag(circle, id);
        Text label = new Text(String.valueOf(id));
        label.setX(circle.getCenterX() - 5);
        label.setY(circle.getCenterY() + 5);
        label.setMouseTransparent(true); 
        
        graphPane.getChildren().addAll(circle, label);
        nodes.put(id, circle);
        nodeLabels.put(id, label);
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
            
            Text label = nodeLabels.get(id);
            if(label!=null) { label.setX(newX-5); label.setY(newY+5); }
            
            updateConnectedEdges(id);
            e.consume(); 
        });
    }
    
    public void addVertex(int id) { 
        if (!nodes.containsKey(id)) { 
            graph.addVertex(); 
            double cx = graphPane.getPrefWidth()/2;
            double cy = graphPane.getPrefHeight()/2;
            addVertexUIOnly(id, cx + (Math.random()-0.5)*100, cy + (Math.random()-0.5)*100); 
            updateAdjListDisplay(); 
        } 
    }
    
    public void removeVertex(int id) {
        if (!nodes.containsKey(id)) return;
        graphPane.getChildren().removeAll(nodes.get(id), nodeLabels.get(id));
        nodes.remove(id); nodeLabels.remove(id);
        
        Iterator<Map.Entry<String, EdgeUI>> it = edges.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry<String, EdgeUI> entry = it.next();
            String[] parts = entry.getKey().split("-");
            if(Integer.parseInt(parts[0]) == id || Integer.parseInt(parts[1]) == id) {
                graphPane.getChildren().removeAll(entry.getValue().line, entry.getValue().label);
                it.remove();
            }
        }
        updateAdjListDisplay();
    }

    // === 修改：addEdge 支持更新文本 ===
    public void addEdge(int from, int to, int weight) {
        if (from==to) return;
        graph.setEdge(from, to, weight);
        
        String key = Math.min(from,to) + "-" + Math.max(from,to);
        
        if (edges.containsKey(key)) {
            // 更新现有文本
            edges.get(key).label.setText(String.valueOf(weight));
        } else {
            Line line = new Line(); line.setStrokeWidth(2); line.setStroke(Color.GRAY);
            Text text = new Text(String.valueOf(weight)); text.setFill(Color.DARKRED);
            graphPane.getChildren().add(0, line); graphPane.getChildren().add(text);
            edges.put(key, new EdgeUI(line, text));
        }
        
        updateConnectedEdges(from);
        updateAdjListDisplay();
    }

    public void removeEdge(int from, int to) {
        graph.delEdge(from, to);
        String key = Math.min(from,to) + "-" + Math.max(from,to);
        EdgeUI ui = edges.remove(key);
        if (ui != null) graphPane.getChildren().removeAll(ui.line, ui.label);
        updateAdjListDisplay();
    }

    private void updateConnectedEdges(int id) {
        for (Map.Entry<String, EdgeUI> entry : edges.entrySet()) {
            String[] parts = entry.getKey().split("-");
            int v1 = Integer.parseInt(parts[0]); int v2 = Integer.parseInt(parts[1]);
            if (v1 == id || v2 == id) {
                updateSingleEdge(entry.getValue(), v1, v2);
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
            ui.line.setStartX(c1.getCenterX());
            ui.line.setStartY(c1.getCenterY());
            ui.line.setEndX(c2.getCenterX());
            ui.line.setEndY(c2.getCenterY());
            ui.label.setX((c1.getCenterX()+c2.getCenterX())/2);
            ui.label.setY((c1.getCenterY()+c2.getCenterY())/2-5);
        }
    }

    private void applyCircularLayout() {
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
        updateAllEdges();
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

    public void clearAllEdges() { 
        graph.clearAllEdges(); 
        for(EdgeUI ui:edges.values()) graphPane.getChildren().removeAll(ui.line, ui.label); 
        edges.clear(); 
        updateAdjListDisplay(); 
    }
    
    public void generateRandomGraph() { 
        clearAllEdges(); 
        graph.generateRandomGraph(); 
        applyCircularLayout(); // 随机生成时重置布局
        int n=graph.verticesNumber(); 
        for(int i=0; i<n; i++) { 
            Edge e=graph.firstEdge(i); 
            while(e!=null) { 
                if(e.getMfrom()<e.getMto()) addEdge(e.getMfrom(), e.getMto(), e.getMweight()); 
                e=graph.nextEdge(e); 
            } 
        } 
    }
}