package org.example.ui;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import org.example.core.AdjListGraph;
import org.example.core.BFS;
import org.example.core.DFS;
import org.example.core.kruskal;
import org.example.core.Edge;

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

    // 存储边UI的Map，Key格式统一为 "小ID-大ID" 以处理无向图
    private Map<String, EdgeUI> edges = new HashMap<>();

    public AdjListGraphUI(AdjListGraph graph) {
        this.graph = graph;
        
        // 创建根布局
        root = new BorderPane();
        root.setPrefSize(1000, 600);
        
        // 左侧：图显示区域
        graphPane = new Pane();
        graphPane.setPrefSize(800, 600);
        graphPane.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-width: 1;");
        
        // 右侧：邻接表显示区域
        VBox adjListPane = new VBox();
        adjListPane.setPrefSize(200, 600);
        adjListPane.setPadding(new Insets(15));
        adjListPane.setStyle("-fx-background-color: #ffffff; -fx-border-color: #dee2e6; -fx-border-width: 1;");
        
        Text adjListTitle = new Text("邻接表数据");
        adjListTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-fill: #2c3e50;");
        
        // 创建滚动面板
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(500); 
        scrollPane.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        
        // 内容容器
        VBox scrollContent = new VBox();
        
        adjListDisplay = new Text();
        adjListDisplay.setStyle("-fx-font-family: 'Monaco', 'Menlo', 'Consolas', monospace; -fx-font-size: 12px; -fx-fill: #34495e;");
        adjListDisplay.wrappingWidthProperty().bind(scrollPane.widthProperty().subtract(20));
        
        scrollContent.getChildren().add(adjListDisplay);
        scrollPane.setContent(scrollContent);
        
        adjListPane.getChildren().addAll(adjListTitle, scrollPane);
        
        // 算法控制面板
        VBox algorithmPane = new VBox(10);
        algorithmPane.setPadding(new Insets(15));
        algorithmPane.setStyle("-fx-background-color: #ffffff; -fx-border-color: #dee2e6; -fx-border-width: 1;");
        
        Text algorithmTitle = new Text("图算法");
        algorithmTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-fill: #2c3e50;");
        
        Label startVertexLabel = new Label("起始顶点:");
        TextField startVertexField = new TextField();
        startVertexField.setPromptText("0-4");
        
        Button bfsButton = new Button("BFS遍历");
        Button dfsButton = new Button("DFS遍历");
        Button mstButton = new Button("最小生成树");
        Button clearButton = new Button("恢复原始视图");
        
        bfsButton.setOnAction(e -> performBFS(startVertexField.getText()));
        dfsButton.setOnAction(e -> performDFS(startVertexField.getText()));
        mstButton.setOnAction(e -> performMST());
        clearButton.setOnAction(e -> clearDisplay());
        
        algorithmPane.getChildren().addAll(algorithmTitle, startVertexLabel, startVertexField, bfsButton, dfsButton, mstButton, clearButton);
        
        // 右侧整体布局
        VBox rightPane = new VBox();
        rightPane.getChildren().addAll(adjListPane, algorithmPane);
        
        root.setLeft(graphPane);
        root.setRight(rightPane);
        
        // 初始化5个顶点
        for (int i = 0; i < 5; i++) {
            addVertexUIOnly(i);
        }
        
        updateAdjListDisplay();
    }

    public BorderPane getPane() {
        return root;
    }

    /** 更新邻接表文本显示 */
    private void updateAdjListDisplay() {
        if (graph != null) {
            String info = graph.getAdjListString();
            // 确保文本被更新
            adjListDisplay.setText(info);
        }
    }

    private void addVertexUIOnly(int id) {
        if (nodes.containsKey(id)) return;

        Circle circle = new Circle(20, Color.LIGHTGREEN);
        circle.setStroke(Color.BLACK);
        circle.setStrokeWidth(2);
        Text label = new Text(String.valueOf(id));

        graphPane.getChildren().addAll(circle, label);
        nodes.put(id, circle);
        nodeLabels.put(id, label);

        updateNodePositions();
    }

    public void addVertex(int id) {
        if (nodes.containsKey(id)) return;

        graph.addVertex(); // 真正更新数据结构
        addVertexUIOnly(id); // 更新UI
        updateAdjListDisplay(); // 刷新文本
    }

    public void removeVertex(int id) {
        Circle circle = nodes.remove(id);
        Text label = nodeLabels.remove(id);
        if (circle != null) graphPane.getChildren().remove(circle);
        if (label != null) graphPane.getChildren().remove(label);

        // 使用迭代器安全删除相关边
        Iterator<Map.Entry<String, EdgeUI>> it = edges.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, EdgeUI> entry = it.next();
            String key = entry.getKey();
            String[] parts = key.split("-");
            int v1 = Integer.parseInt(parts[0]);
            int v2 = Integer.parseInt(parts[1]);
            
            if (v1 == id || v2 == id) {
                // 从图中真正删除
                graph.delEdge(v1, v2);
                // 从UI删除
                graphPane.getChildren().removeAll(entry.getValue().line, entry.getValue().label);
                it.remove();
            }
        }

        updateNodePositions();
        updateAdjListDisplay();
    }

    /** 添加或更新边 */
    public void addEdge(int from, int to, int weight) {
        if (!nodes.containsKey(from) || !nodes.containsKey(to)) return;
        if (from == to) return; // 不处理自环

        // 1. 始终更新底层数据结构 (Update Model)
        // AdjListGraph 是无向的，setEdge 会同时更新 from->to 和 to->from
        graph.setEdge(from, to, weight);

        // 2. 更新 UI (Update View)
        // 生成标准化的 Key (小-大)，保证 1-2 和 2-1 指向同一个UI对象
        int min = Math.min(from, to);
        int max = Math.max(from, to);
        String edgeKey = min + "-" + max;

        // 如果UI上已经有这条边，先移除旧的（为了更新权重文字）
        if (edges.containsKey(edgeKey)) {
            EdgeUI oldEdge = edges.get(edgeKey);
            graphPane.getChildren().removeAll(oldEdge.line, oldEdge.label);
            edges.remove(edgeKey);
        }

        Circle c1 = nodes.get(from);
        Circle c2 = nodes.get(to);

        Line line = new Line(c1.getCenterX(), c1.getCenterY(), c2.getCenterX(), c2.getCenterY());
        line.setStrokeWidth(2);
        line.setStroke(Color.GRAY);

        // 计算文字位置
        Text text = new Text(
                (c1.getCenterX() + c2.getCenterX()) / 2,
                (c1.getCenterY() + c2.getCenterY()) / 2 - 5,
                String.valueOf(weight)
        );
        text.setFill(Color.DARKRED);
        text.setStyle("-fx-font-weight: bold;");

        // 确保线条在圆圈下面
        graphPane.getChildren().add(0, line);
        graphPane.getChildren().add(text);
        
        edges.put(edgeKey, new EdgeUI(line, text));
        
        // 3. 刷新右侧邻接表文本
        updateAdjListDisplay();
    }

    public void removeEdge(int from, int to) {
        // 更新模型
        graph.delEdge(from, to);
        
        // 更新 UI (使用标准化Key)
        int min = Math.min(from, to);
        int max = Math.max(from, to);
        String key = min + "-" + max;
        
        EdgeUI edgeUI = edges.remove(key);
        if (edgeUI != null) {
            graphPane.getChildren().removeAll(edgeUI.line, edgeUI.label);
        }
        
        updateAdjListDisplay();
    }

    private void updateNodePositions() {
        int n = nodes.size();
        if (n == 0) return;

        double width = graphPane.getPrefWidth();
        double height = graphPane.getPrefHeight();
        double centerX = width / 2;
        double centerY = height * 0.4;
        double radius = Math.min(centerX, centerY) * 0.7;

        int i = 0;
        // 排序以保持位置稳定
        List<Integer> sortedKeys = nodes.keySet().stream().sorted().toList();
        Map<Integer, double[]> positions = new HashMap<>();

        for (Integer id : sortedKeys) {
            double angle = 2 * Math.PI * i / n - Math.PI / 2;
            double x = centerX + radius * Math.cos(angle);
            double y = centerY + radius * Math.sin(angle);
            
            Circle c = nodes.get(id);
            c.setCenterX(x);
            c.setCenterY(y);
            
            Text t = nodeLabels.get(id);
            t.setX(x - 5);
            t.setY(y + 5);
            
            positions.put(id, new double[]{x, y});
            i++;
        }

        // 更新所有连接线的位置
        for (Map.Entry<String, EdgeUI> entry : edges.entrySet()) {
            String[] parts = entry.getKey().split("-");
            int id1 = Integer.parseInt(parts[0]);
            int id2 = Integer.parseInt(parts[1]);
            
            double[] p1 = positions.get(id1);
            double[] p2 = positions.get(id2);
            
            if (p1 != null && p2 != null) {
                Line l = entry.getValue().line;
                l.setStartX(p1[0]); l.setStartY(p1[1]);
                l.setEndX(p2[0]);   l.setEndY(p2[1]);
                
                Text t = entry.getValue().label;
                t.setX((p1[0] + p2[0]) / 2);
                t.setY((p1[1] + p2[1]) / 2 - 5);
            }
        }
    }

    // --- 算法部分保持不变，只是调用位置调整 ---

    public void performBFS(String startVertexText) {
        resetStyles();
        try {
            int start = Integer.parseInt(startVertexText.trim());
            if (!nodes.containsKey(start)) {
                adjListDisplay.setText(graph.getAdjListString() + "\n\n错误: 顶点不存在");
                return;
            }
            BFS bfs = new BFS(graph);
            bfs.traverseFromVertex(start);
            highlightPath(bfs.getTraversalOrder(), "BFS", start);
        } catch (Exception e) {
            adjListDisplay.setText(graph.getAdjListString() + "\n\n错误: 输入无效");
        }
    }

    public void performDFS(String startVertexText) {
        resetStyles();
        try {
            int start = Integer.parseInt(startVertexText.trim());
            if (!nodes.containsKey(start)) {
                adjListDisplay.setText(graph.getAdjListString() + "\n\n错误: 顶点不存在");
                return;
            }
            DFS dfs = new DFS(graph);
            dfs.traverseFromVertex(start);
            highlightPath(dfs.getTraversalOrder(), "DFS", start);
        } catch (Exception e) {
            adjListDisplay.setText(graph.getAdjListString() + "\n\n错误: 输入无效");
        }
    }

    public void performMST() {
        resetStyles();
        kruskal k = new kruskal(graph);
        Edge[] mst = k.generateMST();
        if (mst == null) {
            adjListDisplay.setText(graph.getAdjListString() + "\n\n无法生成最小生成树");
            return;
        }
        
        StringBuilder sb = new StringBuilder(graph.getAdjListString());
        sb.append("\n\n=== 最小生成树边 ===\n");
        
        // 高亮MST边
        for (Edge e : mst) {
            int min = Math.min(e.getMfrom(), e.getMto());
            int max = Math.max(e.getMfrom(), e.getMto());
            String key = min + "-" + max;
            
            if (edges.containsKey(key)) {
                EdgeUI ui = edges.get(key);
                ui.line.setStroke(Color.GREEN);
                ui.line.setStrokeWidth(4);
            }
            sb.append(e.getMfrom()).append(" - ").append(e.getMto())
              .append(" (").append(e.getMweight()).append(")\n");
        }
        adjListDisplay.setText(sb.toString());
    }

    private void highlightPath(List<Integer> path, String algo, int start) {
        StringBuilder sb = new StringBuilder(graph.getAdjListString());
        sb.append("\n\n=== ").append(algo).append(" 结果 (Start: ").append(start).append(") ===\n");
        for (int i = 0; i < path.size(); i++) {
            sb.append(path.get(i));
            if (i < path.size() - 1) sb.append(" -> ");
            
            // 高亮节点
            Circle c = nodes.get(path.get(i));
            if (c != null) c.setFill(Color.ORANGE);
        }
        adjListDisplay.setText(sb.toString());
    }

    public void clearDisplay() {
        resetStyles();
        updateAdjListDisplay();
    }
    
    private void resetStyles() {
        for (Circle c : nodes.values()) {
            c.setFill(Color.LIGHTGREEN);
            c.setStroke(Color.BLACK);
        }
        for (EdgeUI e : edges.values()) {
            e.line.setStroke(Color.GRAY);
            e.line.setStrokeWidth(2);
        }
    }
    
    public void clearAllEdges() {
        graph.clearAllEdges();
        for (EdgeUI e : edges.values()) {
            graphPane.getChildren().removeAll(e.line, e.label);
        }
        edges.clear();
        updateAdjListDisplay();
    }
    
    public void generateRandomGraph() {
        clearAllEdges();
        graph.generateRandomGraph();
        
        // 重新从Graph对象读取边并绘制
        int n = graph.verticesNumber();
        for (int i = 0; i < n; i++) {
            Edge e = graph.firstEdge(i);
            while (e != null) {
                int from = e.getMfrom();
                int to = e.getMto();
                // 只绘制一次 (from < to)
                if (from < to) {
                    addEdge(from, to, e.getMweight());
                }
                e = graph.nextEdge(e);
            }
        }
        updateAdjListDisplay();
    }
}