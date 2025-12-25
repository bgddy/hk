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
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
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
    
    // 分离结构显示和日志显示
    private VBox adjListVisualContainer; 
    private Text logDisplay;             
    private StringBuilder logHistory = new StringBuilder(); 

    private ListView<String> codeListView;
    private AdjListGraph graph;
    
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
    
    private Timeline autoPlayTimeline;
    private Timeline currentAnimation; 
    
    // 用于追踪并管理节点上的特效动画（如割点闪烁），以便统一停止
    private List<Timeline> activeNodeAnimations = new ArrayList<>();
    
    private List<TraversalStep> currentSteps = new ArrayList<>();
    private String currentAlgoType = "";
    private int currentStepIndex = 0;

    public AdjListGraphUI(AdjListGraph graph) {
        this.graph = graph;
        root = new BorderPane();
        root.setPrefSize(1200, 700); 
        
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
        
        graphScrollPane = new ScrollPane(graphPane);
        graphScrollPane.setPannable(true); 
        graphScrollPane.setFitToWidth(false); 
        graphScrollPane.setFitToHeight(false);
        graphScrollPane.setStyle("-fx-background-color: transparent; -fx-border-color: #dee2e6; -fx-border-width: 1;");
        
        Button zoomInBtn = createZoomButton("放大", 1.2);
        Button zoomOutBtn = createZoomButton("缩小", 0.8);
        Button layoutBtn = createZoomButton("重排", 1.0); 
        layoutBtn.setOnAction(e -> applyCircularLayout());
        
        Button randomBtn = createZoomButton("随机", 1.0); 
        randomBtn.setOnAction(e -> generateRandomGraph());

        VBox zoomControls = new VBox(10, zoomInBtn, zoomOutBtn, layoutBtn, randomBtn);
        zoomControls.setAlignment(Pos.CENTER);
        zoomControls.setPadding(new Insets(20));
        zoomControls.setPickOnBounds(false);
        zoomControls.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        
        StackPane centerStack = new StackPane();
        centerStack.getChildren().addAll(graphScrollPane, zoomControls);
        StackPane.setAlignment(zoomControls, Pos.TOP_RIGHT);

        // --- 右侧：数据与日志面板 ---
        VBox dataContainer = new VBox(0); 
        dataContainer.setStyle("-fx-background-color: #ffffff;");
        
        // 标题栏
        Label adjListTitle = new Label(" 邻接表结构 (Adjacency List)");
        adjListTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-padding: 10; -fx-background-color: #ecf0f1; -fx-border-color: #bdc3c7; -fx-border-width: 0 0 1 0;");
        adjListTitle.setMaxWidth(Double.MAX_VALUE);
        
        // 图形化容器
        adjListVisualContainer = new VBox(8); 
        adjListVisualContainer.setPadding(new Insets(15));
        adjListVisualContainer.setStyle("-fx-background-color: #ffffff;");

        // 日志区域
        Label logTitle = new Label(" 运行日志");
        logTitle.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #555; -fx-padding: 8; -fx-background-color: #f0f0f0; -fx-border-color: #e0e0e0; -fx-border-width: 1 0 1 0;");
        logTitle.setMaxWidth(Double.MAX_VALUE);
        
        logDisplay = new Text();
        logDisplay.setStyle("-fx-font-family: 'Segoe UI', 'Microsoft YaHei', sans-serif; -fx-font-size: 11px; -fx-fill: #34495e;");
        
        ScrollPane visualScrollPane = new ScrollPane(adjListVisualContainer);
        visualScrollPane.setFitToWidth(true);
        visualScrollPane.setPrefHeight(300); 
        visualScrollPane.setStyle("-fx-background-color: transparent; -fx-background: #ffffff; -fx-border-color: transparent;");

        ScrollPane logScrollPane = new ScrollPane(logDisplay);
        logScrollPane.setFitToWidth(true);
        logScrollPane.setStyle("-fx-background-color: transparent; -fx-background: #ffffff; -fx-border-color: transparent;");
        logDisplay.wrappingWidthProperty().bind(logScrollPane.widthProperty().subtract(20));

        dataContainer.getChildren().addAll(adjListTitle, visualScrollPane, logTitle, logScrollPane);
        VBox.setVgrow(logScrollPane, Priority.ALWAYS); 

        // --- 伪代码面板 ---
        VBox codeContainer = new VBox(0);
        Label codeTitle = new Label(" 算法追踪");
        codeTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1565c0; -fx-padding: 10; -fx-background-color: #e3f2fd; -fx-border-color: #90caf9; -fx-border-width: 1 0 1 0;");
        codeTitle.setMaxWidth(Double.MAX_VALUE);

        codeListView = new ListView<>();
        codeListView.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-size: 13px; -fx-border-width: 0;");
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
        rightSplitPane.setDividerPositions(0.65); 
        rightSplitPane.setPrefWidth(400); 

        root.setCenter(centerStack);
        root.setRight(rightSplitPane);
        
        // --- 初始状态 ---
        // 1. 创建节点
        for (int i = 0; i < 5; i++) addVertexUIOnly(i, -1, -1);
        
        
        // 3. 应用布局并同步视觉元素
        applyCircularLayout(); 
        syncGraphVisuals(); 
        updateAdjListDisplay(); 
        
        setCode(new String[]{"// 等待算法运行...", "// 代码将在此显示"});
        centerContent();
        
        autoPlayTimeline = new Timeline(new KeyFrame(Duration.millis(800), e -> nextStep()));
        autoPlayTimeline.setCycleCount(Timeline.INDEFINITE);
    }

    // === 同步后端图数据到前端UI显示 ===
    private void syncGraphVisuals() {
        for(EdgeUI ui : edges.values()) {
            graphPane.getChildren().removeAll(ui.line, ui.label);
        }
        edges.clear();
        
        int n = graph.verticesNumber();
        for (int i = 0; i < n; i++) {
            if (!graph.isVertexExists(i)) continue;
            for (org.example.core.Edge e = graph.firstEdge(i); e != null; e = graph.nextEdge(e)) {
                int u = e.getMfrom();
                int v = e.getMto();
                if (u < v && graph.isVertexExists(v)) {
                    createEdgeUI(u, v, e.getMweight());
                }
            }
        }
        for(EdgeUI ui : edges.values()) {
            ui.line.toBack();
        }
    }

    private void createEdgeUI(int u, int v, int weight) {
        String key = Math.min(u, v) + "-" + Math.max(u, v);
        if (edges.containsKey(key)) return;

        Line line = new Line(); 
        line.setStroke(Color.GRAY); 
        line.setStrokeWidth(2);
        
        Text t = new Text(String.valueOf(weight));
        t.setFont(Font.font("Arial", 11));
        
        graphPane.getChildren().addAll(line, t);
        edges.put(key, new EdgeUI(line, t));
        
        updateSingleEdgePosition(u, v, line, t);
    }

    private void updateSingleEdgePosition(int u, int v, Line line, Text label) {
        Circle c1 = nodes.get(u);
        Circle c2 = nodes.get(v);
        if (c1 != null && c2 != null) {
            line.setStartX(c1.getCenterX()); 
            line.setStartY(c1.getCenterY());
            line.setEndX(c2.getCenterX()); 
            line.setEndY(c2.getCenterY());
            
            double midX = (c1.getCenterX() + c2.getCenterX()) / 2;
            double midY = (c1.getCenterY() + c2.getCenterY()) / 2;
            label.setX(midX); 
            label.setY(midY - 5);
        }
    }

    private void renderAdjList() {
        adjListVisualContainer.getChildren().clear();
        int n = graph.verticesNumber();

        for (int i = 0; i < n; i++) {
            if (!graph.isVertexExists(i)) continue;

            HBox row = new HBox(0); 
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPrefHeight(34); 

            StackPane indexNode = new StackPane();
            indexNode.setPrefSize(40, 30);
            Rectangle indexBg = new Rectangle(40, 30);
            indexBg.setFill(Color.web("#ecf0f1")); 
            indexBg.setStroke(Color.web("#bdc3c7")); 
            indexBg.setArcWidth(4); indexBg.setArcHeight(4);
            Text indexText = new Text(String.valueOf(i));
            indexText.setFont(Font.font("Arial", FontWeight.BOLD, 14));
            indexText.setFill(Color.web("#2c3e50"));
            indexNode.getChildren().addAll(indexBg, indexText);
            
            Pane startPointer = createSimpleArrow(20); 
            row.getChildren().addAll(indexNode, startPointer);

            org.example.core.Edge e = graph.firstEdge(i);
            boolean hasNext = (e != null);
            
            while (e != null) {
                if (graph.isVertexExists(e.getMto())) {
                    HBox nodeBody = createListNode(e.getMto(), e.getMweight());
                    row.getChildren().add(nodeBody);

                    e = graph.nextEdge(e);
                    boolean nextValid = false;
                    org.example.core.Edge temp = e;
                    while(temp != null) {
                        if(graph.isVertexExists(temp.getMto())) {
                            nextValid = true;
                            break;
                        }
                        temp = graph.nextEdge(temp);
                    }
                    if (nextValid) {
                        row.getChildren().add(createSimpleArrow(25));
                    } else {
                        row.getChildren().add(createSimpleArrow(15)); 
                        row.getChildren().add(createNullTerminator());
                        e = null; 
                    }
                } else {
                    e = graph.nextEdge(e);
                }
            }

            if (!hasNext) {
                row.getChildren().add(createNullTerminator());
            }
            adjListVisualContainer.getChildren().add(row);
        }
    }

    private HBox createListNode(int targetId, int weight) {
        HBox node = new HBox(0);
        node.setAlignment(Pos.CENTER);
        node.setStyle("-fx-border-color: #3498db; -fx-border-width: 1; -fx-border-radius: 4; -fx-background-radius: 4; -fx-background-color: white;");

        StackPane idPart = new StackPane();
        idPart.setPrefSize(30, 28);
        idPart.setStyle("-fx-background-color: #e8f4f8; -fx-background-radius: 3 0 0 3;");
        Text idText = new Text(String.valueOf(targetId));
        idText.setFont(Font.font("Consolas", FontWeight.BOLD, 13));
        idPart.getChildren().add(idText);

        Line divider = new Line(0, 0, 0, 28);
        divider.setStroke(Color.web("#3498db"));

        StackPane weightPart = new StackPane();
        weightPart.setPrefSize(35, 28); 
        Text wText = new Text("w:" + weight);
        wText.setFont(Font.font("Arial", 10));
        wText.setFill(Color.web("#7f8c8d"));
        weightPart.getChildren().add(wText);

        node.getChildren().addAll(idPart, divider, weightPart);
        return node;
    }

    private Pane createSimpleArrow(double width) {
        Pane p = new Pane();
        p.setPrefSize(width, 30);
        double y = 15; 
        Line line = new Line(0, y, width - 4, y);
        line.setStroke(Color.web("#95a5a6"));
        line.setStrokeWidth(1.5);
        Polygon arrowHead = new Polygon();
        arrowHead.getPoints().addAll(width, y, width - 6, y - 3, width - 6, y + 3);
        arrowHead.setFill(Color.web("#95a5a6"));
        p.getChildren().addAll(line, arrowHead);
        return p;
    }

    private StackPane createNullTerminator() {
        StackPane sp = new StackPane();
        sp.setPrefSize(20, 30);
        Rectangle r = new Rectangle(14, 14);
        r.setFill(Color.web("#ecf0f1"));
        r.setStroke(Color.web("#bdc3c7"));
        r.setStrokeWidth(1);
        Line slash = new Line(0, 14, 14, 0);
        slash.setStroke(Color.web("#e74c3c"));
        slash.setStrokeWidth(1.5);
        Pane icon = new Pane(r, slash);
        icon.setMaxSize(14, 14);
        sp.getChildren().add(icon);
        return sp;
    }

    private void updateLog(String msg) {
        if (msg == null || msg.isEmpty()) return;
        String time = java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
        logHistory.insert(0, String.format("[%s] %s\n", time, msg));
        if (logHistory.length() > 3000) logHistory.setLength(3000);
        logDisplay.setText(logHistory.toString());
    }

    private void updateAdjListDisplay() { 
        if (graph != null) {
            renderAdjList(); 
        }
    }

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

    // [核心修复] A* 算法调用：加入了坐标记录的调试日志
    public void performAStar(String startText, String endText) {
        stopAnimation();
        resetStyles();
        setCode(new String[]{
            "// A* Algorithm",
            "f(n) = g(n) + h(n)",
            "1. OpenSet: priority queue",
            "2. while OpenSet not empty:",
            "3.   current = OpenSet.poll()",
            "4.   if current == target: return",
            "5.   for neighbor of current:",
            "6.     update f, g scores"
        });

        try {
            int start = Integer.parseInt(startText.trim());
            int end = Integer.parseInt(endText.trim());
            
            if (!nodes.containsKey(start) || !nodes.containsKey(end)) {
                updateLog("错误: 起点或终点不存在。");
                return;
            }

            // 1. 收集当前所有节点的最新坐标
            Map<Integer, double[]> nodePositions = new HashMap<>();
            for (Map.Entry<Integer, Circle> entry : nodes.entrySet()) {
                nodePositions.put(entry.getKey(), new double[]{
                    entry.getValue().getCenterX(), 
                    entry.getValue().getCenterY()
                });
            }
            
            // [新增] 调试日志：确认程序读到了最新的坐标
            double[] sPos = nodePositions.get(start);
            double[] ePos = nodePositions.get(end);
            updateLog(String.format("坐标检测: 起点(%.0f,%.0f) 终点(%.0f,%.0f)", sPos[0], sPos[1], ePos[0], ePos[1]));

            AStar astar = new AStar();
            List<TraversalStep> steps = astar.search(graph, start, end, nodePositions);
            
            updateLog("A* 开始: " + start + " -> " + end);
            initAnimation(steps, "AStar");
            
        } catch (NumberFormatException e) {
            updateLog("输入错误: 请输入数字ID");
        }
    }

    // [核心修复] 获取图DSL时包含坐标 (POS 指令)
    public String getGraphDSL() {
        StringBuilder sb = new StringBuilder();
        // 1. 保存节点及位置
        for (Integer id : nodes.keySet()) {
            sb.append("NODE ").append(id).append("\n"); 
            // [新增] 记录坐标 POS id x y
            Circle c = nodes.get(id);
            if (c != null) {
                sb.append("POS ").append(id).append(" ")
                  .append(String.format("%.2f", c.getCenterX())).append(" ")
                  .append(String.format("%.2f", c.getCenterY())).append("\n");
            }
        }
        int n = graph.verticesNumber();
        for (int i = 0; i < n; i++) {
            if (!graph.isVertexExists(i)) continue;
            for (org.example.core.Edge e = graph.firstEdge(i); e != null; e = graph.nextEdge(e)) {
                if (graph.isVertexExists(e.getMto()) && e.getMfrom() < e.getMto()) {
                    sb.append(e.getMfrom()).append(" -> ").append(e.getMto())
                      .append(" : ").append(e.getMweight()).append("\n");
                }
            }
        }
        return sb.toString();
    }

    public void renderFromDSL(String dslText) {
        if (dslText == null || dslText.trim().isEmpty()) return;

        String[] lines = dslText.split("\n");
        boolean hasPos = false;

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
            if (line.startsWith("DEL")) {
                try {
                    String clean = line.replace("DEL", "").trim();
                    String[] parts = clean.split("->");
                    removeEdge(Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[1].trim()));
                } catch (Exception e) {}
                continue;
            }
            if (line.startsWith("POS")) {
                hasPos = true;
                try {
                    String[] parts = line.split(" ");
                    int id = Integer.parseInt(parts[1]);
                    double x = Double.parseDouble(parts[2]);
                    double y = Double.parseDouble(parts[3]);
                    while (graph.verticesNumber() <= id) graph.addVertex();
                    if (!nodes.containsKey(id)) addVertexUIOnly(id, x, y);
                    else {
                        Circle c = nodes.get(id); c.setCenterX(x); c.setCenterY(y);
                        Text t = nodeLabels.get(id); if(t != null) { t.setX(x-5); t.setY(y+5); }
                    }
                } catch (Exception e) { }
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
                    
                    while (graph.verticesNumber() <= Math.max(u, v)) graph.addVertex(); 
                    if (!nodes.containsKey(u)) addVertexUIOnly(u, -1, -1);
                    if (!nodes.containsKey(v)) addVertexUIOnly(v, -1, -1);

                    addEdge(u, v, w);
                } catch (Exception e) {}
            }
        }
        
        if (!hasPos && !dslText.contains("POS")) {
             applyCircularLayout();
        } else {
             updateAllEdges();
        }
        
        syncGraphVisuals(); 
        updateAdjListDisplay();
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
        
        if (factor != 1.0) {
            btn.setOnAction(e -> zoom(factor));
        }
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
                    if (t != null) { t.setX(newX - 5); t.setY(newY + 5); }
                }
            }
            updateAllEdges(); 
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
            if (!nodes.containsKey(start)) { updateLog("错误: 顶点不存在"); return; }
            BFS bfs = new BFS(graph); 
            bfs.traverseFromVertex(start);
            updateLog("BFS 遍历: " + bfs.getTraversalResult());
            initAnimation(bfs.getSteps(), "BFS");
        } catch (Exception e) { updateLog("错误: " + e.getMessage()); }
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
            "      DFS(v)"
        };
        setCode(dfsCode);
        try {
            int start = Integer.parseInt(startVertexText.trim());
            if (!nodes.containsKey(start)) { updateLog("错误: 顶点不存在"); return; }
            DFS dfs = new DFS(graph); 
            dfs.traverseFromVertex(start);
            updateLog("DFS 遍历: " + dfs.getTraversalResult());
            initAnimation(dfs.getSteps(), "DFS");
        } catch (Exception e) { updateLog("错误: " + e.getMessage()); }
    }

    public void performMST() { 
        stopAnimation();
        resetStyles();
        String[] mstCode = { "// Kruskal MST", "Sort edges", "Union-Find" };
        setCode(mstCode);
        kruskal k = new kruskal(graph); 
        Edge[] mst = k.generateMST();
        if (mst == null) { updateLog("无法生成MST (不连通)"); return; }
        initAnimation(k.getSteps(), "MST");
        updateLog("Kruskal MST 生成完毕");
    }

    public void performPrim() {
        stopAnimation();
        resetStyles();
        String[] primCode = { "// Prim MST", "Priority Queue", "Expand Tree" };
        setCode(primCode);
        prim p = new prim(graph);
        Edge[] mst = p.generateMST();
        initAnimation(p.getSteps(), "Prim");
        updateLog("Prim MST 生成完毕");
    }

    public void performDijkstra(String startText, String endText) {
        stopAnimation();
        resetStyles();
        setCode(new String[]{ "// Dijkstra", "Relax edges", "Update distances" });
        try {
            int start = Integer.parseInt(startText.trim());
            int end = Integer.parseInt(endText.trim());
            if (!nodes.containsKey(start) || !nodes.containsKey(end)) { updateLog("Error: Vertex not found."); return; }

            Dijkstra dijkstra = new Dijkstra(graph);
            List<Integer> path = dijkstra.findShortestPath(start, end);
            int dist = dijkstra.getShortestDistance(end);
            
            if (dist == Integer.MAX_VALUE) updateLog("不可达: " + start + " -> " + end);
            else {
                updateLog("最短路径: " + path + ", 总重: " + dist);
                animatePath(path);
            }
        } catch (Exception e) { updateLog("错误: " + e.getMessage()); }
    }

    public void performDijkstraAll(String startText) {
        stopAnimation();
        resetStyles();
        setCode(new String[]{ "// Dijkstra All", "Calculate dist to all nodes" });
        try {
            int start = Integer.parseInt(startText.trim());
            if (!nodes.containsKey(start)) { updateLog("错误: 顶点不存在"); return; }
            Dijkstra dijkstra = new Dijkstra(graph);
            dijkstra.findShortestPath(start, -1); 
            updateLog(dijkstra.getAllPathsResult(start));
            initAnimation(dijkstra.getSteps(), "Dijkstra");
        } catch (NumberFormatException e) { updateLog("请输入有效的起点ID"); }
    }

    public void performArticulationPoints() {
        stopAnimation(); 
        resetStyles();   
        
        ArticulationPointFinder finder = new ArticulationPointFinder(graph);
        Set<Integer> cutVertices = finder.find();
        
        updateLog("=== 割点 (关键节点) 分析 ===");
        if (cutVertices.isEmpty()) {
            updateLog("分析结果: 当前图结构非常稳固，没有单一割点。");
        } else {
            updateLog("⚠️ 警告: 发现 " + cutVertices.size() + " 个关键割点: " + cutVertices);
        }

        setCode(new String[]{
            "// Articulation Point Algorithm",
            "1. DFS(u, p): update low[u], dfn[u]",
            "2. for child v:",
            "     if low[v] >= dfn[u]:",
            "       u is Articulation Point",
            "3. Root is AP if children > 1"
        });

        for (Integer id : nodes.keySet()) {
            Circle c = nodes.get(id);
            if (c == null) continue;
            if (cutVertices.contains(id)) {
                c.setFill(Color.RED);
                c.setRadius(28); 
                c.setStroke(Color.DARKRED);
                c.setStrokeWidth(4);
                
                Timeline blink = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(c.fillProperty(), Color.RED)),
                    new KeyFrame(Duration.millis(500), new KeyValue(c.fillProperty(), Color.ORANGE)),
                    new KeyFrame(Duration.millis(1000), new KeyValue(c.fillProperty(), Color.RED))
                );
                blink.setCycleCount(Timeline.INDEFINITE); 
                blink.play();
                activeNodeAnimations.add(blink);
            } else {
                c.setFill(Color.LIGHTGRAY);
                c.setStroke(Color.GRAY);
            }
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
        highlightCode(0);
    }
    
    private void stopAnimation() {
        if (currentAnimation != null) {
            currentAnimation.stop();
            currentAnimation = null;
        }
        for(Timeline t : activeNodeAnimations) {
            t.stop();
        }
        activeNodeAnimations.clear();
        pause(); 
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
        if (step.getDescription() != null && !step.getDescription().isEmpty()) {
            updateLog(step.getDescription());
        }

        if (step.getType() == TraversalStep.Type.VISIT) {
             if(step.getVertexId() != -1) highlightNode(step.getVertexId(), Color.ORANGE);
        } else if (step.getType() == TraversalStep.Type.VISIT_EDGE || step.getType() == TraversalStep.Type.CHECK_EDGE) {
             highlightEdge(step.getEdge(), Color.YELLOW);
        } else if (step.getType() == TraversalStep.Type.ADD_EDGE || step.getType() == TraversalStep.Type.RELAX_SUCCESS) {
             highlightEdge(step.getEdge(), Color.RED);
             if (step.getEdge() != null) highlightNode(step.getEdge().getMto(), Color.LIGHTGREEN);
        } else if (step.getType() == TraversalStep.Type.PATH) {
             if (step.getOpenListSnapshot() != null) animatePath(step.getOpenListSnapshot());
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
    
    private void highlightEdge(org.example.core.Edge edge, Color color) {
        if (edge == null) return;
        int u = edge.getMfrom();
        int v = edge.getMto();
        String key = Math.min(u, v) + "-" + Math.max(u, v);
        EdgeUI ui = edges.get(key);
        if (ui != null) {
            ui.line.setStroke(color);
            ui.line.setStrokeWidth(4);
            ui.line.toFront(); 
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

    public void resetToDefault() {
        this.graph = new AdjListGraph(5);
        clearInternalGraphState();
        for (int i = 0; i < 5; i++) addVertexUIOnly(i, -1, -1);
        

        
        applyCircularLayout();
        syncGraphVisuals(); 
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
        logHistory.setLength(0);
    }

    public void saveGraph() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("保存图 (DSL)");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        fileChooser.setInitialFileName("graph_dsl.txt");
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
                while ((line = reader.readLine()) != null) dslContent.append(line).append("\n");
            }
            resetToDefault(); 
            renderFromDSL(dslContent.toString());
            updateLog("DSL 加载成功");
        } catch (Exception ex) { 
            ex.printStackTrace(); 
            updateLog("加载失败: " + ex.getMessage());
        }
    }

    private void addVertexUIOnly(int id, double x, double y) {
        if (nodes.containsKey(id)) return;
        if (id < graph.verticesNumber()) graph.setVertexExists(id, true);
        
        Circle circle = new Circle(20, Color.LIGHTGREEN);
        circle.setStroke(Color.BLACK);
        circle.setStrokeWidth(2);
        
        if (x == -1 || y == -1) {
            double cx = graphPane.getPrefWidth() / 2;
            double cy = graphPane.getPrefHeight() / 2;
            circle.setCenterX(cx + (Math.random() - 0.5) * 200);
            circle.setCenterY(cy + (Math.random() - 0.5) * 200);
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
        final class InteractionState { double startX, startY; boolean isDragging = false; }
        final InteractionState state = new InteractionState();
        circle.setOnMousePressed(e -> {
            state.startX = circle.getCenterX() - e.getX();
            state.startY = circle.getCenterY() - e.getY();
            state.isDragging = false; 
            e.consume(); 
        });
        circle.setOnMouseDragged(e -> {
            state.isDragging = true; 
            double newX = Math.max(20, Math.min(graphPane.getPrefWidth()-20, e.getX() + state.startX));
            double newY = Math.max(20, Math.min(graphPane.getPrefHeight()-20, e.getY() + state.startY));
            circle.setCenterX(newX); circle.setCenterY(newY);
            Text label = nodeLabels.get(id);
            if(label!=null) { label.setX(newX-5); label.setY(newY+5); }
            updateConnectedEdges(id);
            e.consume(); 
        });
        circle.setOnMouseClicked(e -> {
            if (!state.isDragging) handleNodeClick(id);
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
        graph.setVertexExists(id, false);
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
        updateAdjListDisplay();
    }

    public void addEdge(int from, int to, int weight) {
        if (from==to) return;
        graph.setEdge(from, to, weight); 
        createEdgeUI(from, to, weight);  
        updateAdjListDisplay();
    }
    
    public void removeEdge(int from, int to) { 
        graph.delEdge(from, to); 
        String key = Math.min(from,to) + "-" + Math.max(from,to);
        EdgeUI ui = edges.remove(key);
        if(ui != null) {
            graphPane.getChildren().removeAll(ui.line, ui.label);
        }
        updateAdjListDisplay(); 
    }
    
    private void updateConnectedEdges(int id) { updateAllEdges(); } 
    
    private void updateAllEdges() {
        for(Map.Entry<String, EdgeUI> e : edges.entrySet()) {
            String[] parts = e.getKey().split("-");
            int u = Integer.parseInt(parts[0]), v = Integer.parseInt(parts[1]);
            updateSingleEdgePosition(u, v, e.getValue().line, e.getValue().label);
        }
    }

    private void applyCircularLayout() {
        int n = nodes.size();
        if (n == 0) return;
        
        double centerX = graphPane.getPrefWidth() / 2;
        double centerY = graphPane.getPrefHeight() / 2;
        double radius = 250 * currentScale; 
        
        int i = 0;
        for (Integer id : nodes.keySet()) {
            double angle = 2 * Math.PI * i / n;
            double x = centerX + radius * Math.cos(angle);
            double y = centerY + radius * Math.sin(angle);
            
            Circle c = nodes.get(id);
            if (c != null) {
                c.setCenterX(x);
                c.setCenterY(y);
                Text t = nodeLabels.get(id);
                if (t != null) { t.setX(x - 5); t.setY(y + 5); }
            }
            i++;
        }
        updateAllEdges();
    }

    public void clearAllEdges() { 
        graph.clearAllEdges(); 
        syncGraphVisuals(); 
        updateAdjListDisplay(); 
    }
    
    public void generateRandomGraph() { 
        stopAnimation(); 
        resetStyles();   
        
        graph.generateRandomGraph(); 
        applyCircularLayout();       
        syncGraphVisuals();          
        updateAdjListDisplay();      
    }
    
    private void resetStyles() { 
        for (Circle c : nodes.values()) { 
            c.setFill(Color.LIGHTGREEN); 
            c.setStroke(Color.BLACK); 
            c.setRadius(20); 
            c.setStrokeWidth(2); 
            c.setOpacity(1.0);
        }
        for (EdgeUI ui : edges.values()) {
            ui.line.setStroke(Color.GRAY);
            ui.line.setStrokeWidth(2);
        }
    }
}