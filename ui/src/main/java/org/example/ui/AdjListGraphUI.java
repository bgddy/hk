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

    private Map<String, EdgeUI> edges = new HashMap<>();

    public AdjListGraphUI(AdjListGraph graph) {
        this.graph = graph;
        
        // 创建根布局
        root = new BorderPane();
        root.setPrefSize(1000, 600);
        
        // 左侧：图显示区域（更大的空间）
        graphPane = new Pane();
        graphPane.setPrefSize(800, 600);
        graphPane.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-width: 1;");
        
        // 右侧：邻接表显示区域（更小的空间）
        VBox adjListPane = new VBox();
        adjListPane.setPrefSize(200, 600);
        adjListPane.setPadding(new Insets(15));
        adjListPane.setStyle("-fx-background-color: #ffffff; -fx-border-color: #dee2e6; -fx-border-width: 1;");
        
        Text adjListTitle = new Text("邻接表");
        adjListTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-fill: #2c3e50;");
        
        // 创建滚动面板来容纳邻接表显示
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(500); // 设置固定高度
        scrollPane.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        
        // 创建内容容器
        VBox scrollContent = new VBox();
        scrollContent.setStyle("-fx-background-color: transparent;");
        
        adjListDisplay = new Text();
        adjListDisplay.setStyle("-fx-font-family: 'Monaco', 'Menlo', 'Consolas', monospace; -fx-font-size: 12px; -fx-fill: #34495e;");
        adjListDisplay.wrappingWidthProperty().bind(scrollPane.widthProperty().subtract(20)); // 文本自动换行
        
        scrollContent.getChildren().add(adjListDisplay);
        scrollPane.setContent(scrollContent);
        
        // 将邻接表显示添加到布局中
        adjListPane.getChildren().addAll(adjListTitle, scrollPane);
        
        // 添加算法按钮和起始顶点选择
        VBox algorithmPane = new VBox(10);
        algorithmPane.setPadding(new Insets(15));
        algorithmPane.setStyle("-fx-background-color: #ffffff; -fx-border-color: #dee2e6; -fx-border-width: 1;");
        
        Text algorithmTitle = new Text("图算法");
        algorithmTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-fill: #2c3e50;");
        
        // 起始顶点选择
        Label startVertexLabel = new Label("起始顶点:");
        startVertexLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        TextField startVertexField = new TextField();
        startVertexField.setPromptText("输入顶点编号 (0-4)");
        startVertexField.setPrefWidth(180);  // 扩大宽度
        startVertexField.setPrefHeight(35);  // 扩大高度
        
        Button bfsButton = new Button("BFS遍历");
        Button dfsButton = new Button("DFS遍历");
        Button mstButton = new Button("最小生成树");
        Button clearButton = new Button("清空显示");
        
        // 设置按钮样式
        bfsButton.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white; -fx-font-weight: bold;");
        dfsButton.setStyle("-fx-background-color: #2196f3; -fx-text-fill: white; -fx-font-weight: bold;");
        mstButton.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white; -fx-font-weight: bold;");
        clearButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold;");
        
        // 按钮事件
        bfsButton.setOnAction(e -> performBFS(startVertexField.getText()));
        dfsButton.setOnAction(e -> performDFS(startVertexField.getText()));
        mstButton.setOnAction(e -> performMST());
        clearButton.setOnAction(e -> clearDisplay());
        
        algorithmPane.getChildren().addAll(algorithmTitle, startVertexLabel, startVertexField, bfsButton, dfsButton, mstButton, clearButton);
        
        // 为算法面板添加滚动条
        ScrollPane algorithmScrollPane = new ScrollPane();
        algorithmScrollPane.setContent(algorithmPane);
        algorithmScrollPane.setFitToWidth(true);
        algorithmScrollPane.setPrefHeight(200); // 设置固定高度
        algorithmScrollPane.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        algorithmScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        algorithmScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        
        // 创建右侧整体布局
        VBox rightPane = new VBox();
        rightPane.getChildren().addAll(adjListPane, algorithmScrollPane);
        
        root.setLeft(graphPane);
        root.setRight(rightPane);
        
        // 自动添加固定顶点（0-4） - 只添加UI显示，不重复调用图的addVertex
        for (int i = 0; i < 5; i++) {
            addVertexUIOnly(i);
        }
        
        updateAdjListDisplay();
    }

    public BorderPane getPane() {
        return root;
    }

    /** 更新邻接表显示 */
    private void updateAdjListDisplay() {
        String adjListString = graph.getAdjListString();
        adjListDisplay.setText(adjListString);
    }

    /** 只添加UI显示，不调用图的addVertex方法 */
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
        updateAdjListDisplay();
    }

    /** 添加顶点 */
    public void addVertex(int id) {
        if (nodes.containsKey(id)) return;

        // 调用图的addVertex方法动态添加顶点
        graph.addVertex();

        Circle circle = new Circle(20, Color.LIGHTGREEN);
        circle.setStroke(Color.BLACK);
        circle.setStrokeWidth(2);
        Text label = new Text(String.valueOf(id));

        graphPane.getChildren().addAll(circle, label);
        nodes.put(id, circle);
        nodeLabels.put(id, label);

        updateNodePositions();
        updateAdjListDisplay();
    }

    /** 删除顶点及相关边 */
    public void removeVertex(int id) {
        Circle circle = nodes.remove(id);
        Text label = nodeLabels.remove(id);
        if (circle != null) graphPane.getChildren().remove(circle);
        if (label != null) graphPane.getChildren().remove(label);

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
        updateAdjListDisplay();
    }

    /** 添加边 */
    public void addEdge(int from, int to, int weight) {
        if (!nodes.containsKey(from) || !nodes.containsKey(to)) return;

        // 检查是否已经存在该边，如果存在则先删除旧的
        String edgeKey = from + "-" + to;
        if (edges.containsKey(edgeKey)) {
            // 删除旧的边UI元素
            EdgeUI oldEdge = edges.get(edgeKey);
            graphPane.getChildren().removeAll(oldEdge.line, oldEdge.label);
            edges.remove(edgeKey);
        }

        // 只在图数据结构中设置边一次
        if (!graph.isEdge(new Edge(from, to, weight))) {
            graph.setEdge(from, to, weight);
        }

        Circle c1 = nodes.get(from);
        Circle c2 = nodes.get(to);

        Line line = new Line(c1.getCenterX(), c1.getCenterY(), c2.getCenterX(), c2.getCenterY());
        line.setStrokeWidth(2);
        line.setStroke(Color.GRAY);

        Text text = new Text(
                (c1.getCenterX() + c2.getCenterX()) / 2,
                (c1.getCenterY() + c2.getCenterY()) / 2 - 5,
                String.valueOf(weight)
        );
        text.setFill(Color.DARKRED);

        graphPane.getChildren().addAll(line, text);
        edges.put(edgeKey, new EdgeUI(line, text));
        
        updateAdjListDisplay();
    }

    /** 删除边 */
    public void removeEdge(int from, int to) {
        graph.delEdge(from, to);
        String key = from + "-" + to;
        EdgeUI edgeUI = edges.remove(key);
        if (edgeUI != null) {
            graphPane.getChildren().removeAll(edgeUI.line, edgeUI.label);
        }
        updateAdjListDisplay();
    }

    /** 改进版圆形布局：圆心上移 + 半径略减 */
    private void updateNodePositions() {
        int n = nodes.size();
        if (n == 0) return;

        double paneWidth = graphPane.getPrefWidth();
        double paneHeight = graphPane.getPrefHeight();

        // ✅ 圆心上移得更明显
        double centerX = paneWidth / 2;
        double centerY = paneHeight * 0.35;  // 🔹原0.45 → 改为0.35（整体上提）

        // ✅ 半径再缩小一点点，避免顶点挤到边界
        double base = Math.min(centerX, centerY);
        double radius = base * (0.45 + 0.4 / Math.max(n, 3));  // 🔹整体略缩小

        int i = 0;
        Map<Integer, double[]> positions = new HashMap<>();

        for (Map.Entry<Integer, Circle> entry : nodes.entrySet()) {
            double angle = 2 * Math.PI * i / n;
            double x = centerX + radius * Math.cos(angle);
            double y = centerY + radius * Math.sin(angle);
            entry.getValue().setCenterX(x);
            entry.getValue().setCenterY(y);
            positions.put(entry.getKey(), new double[]{x, y});
            i++;
        }

        // 更新标签
        for (Map.Entry<Integer, Text> entry : nodeLabels.entrySet()) {
            int id = entry.getKey();
            if (positions.containsKey(id)) {
                double[] pos = positions.get(id);
                entry.getValue().setX(pos[0] - 6);
                entry.getValue().setY(pos[1] + 6);
            }
        }

        // 更新边
        for (Map.Entry<String, EdgeUI> entry : edges.entrySet()) {
            String key = entry.getKey();
            String[] parts = key.split("-");
            int from = Integer.parseInt(parts[0]);
            int to = Integer.parseInt(parts[1]);
            Circle c1 = nodes.get(from);
            Circle c2 = nodes.get(to);
            if (c1 == null || c2 == null) continue;

            Line line = entry.getValue().line;
            line.setStartX(c1.getCenterX());
            line.setStartY(c1.getCenterY());
            line.setEndX(c2.getCenterX());
            line.setEndY(c2.getCenterY());

            Text text = entry.getValue().label;
            text.setX((c1.getCenterX() + c2.getCenterX()) / 2);
            text.setY((c1.getCenterY() + c2.getCenterY()) / 2 - 5);
        }
    }

    /** 执行BFS遍历（从指定顶点开始） */
    public void performBFS(String startVertexText) {
        if (nodes.isEmpty()) return;
        
        // 重置所有节点颜色
        resetNodeColors();
        
        try {
            int startVertex = 0; // 默认从顶点0开始
            if (startVertexText != null && !startVertexText.trim().isEmpty()) {
                startVertex = Integer.parseInt(startVertexText.trim());
            }
            
            if (!nodes.containsKey(startVertex)) {
                // 在邻接表显示区域添加错误信息
                String currentText = adjListDisplay.getText();
                adjListDisplay.setText(currentText + "\n\n错误: 顶点 " + startVertex + " 不存在！");
                return;
            }
            
            BFS bfs = new BFS(graph);
            System.out.println("BFS遍历结果 (从顶点 " + startVertex + " 开始):");
            bfs.traverseFromVertex(startVertex);
            System.out.println();
            
            // 在UI中高亮显示遍历过程（动画版）
            highlightBFSAnimation(bfs, startVertex);
        } catch (NumberFormatException e) {
            // 在邻接表显示区域添加错误信息
            String currentText = adjListDisplay.getText();
            adjListDisplay.setText(currentText + "\n\n错误: 请输入有效的顶点编号！");
        }
    }

    /** 执行DFS遍历（从指定顶点开始） */
    public void performDFS(String startVertexText) {
        if (nodes.isEmpty()) return;
        
        // 重置所有节点颜色
        resetNodeColors();
        
        try {
            int startVertex = 0; // 默认从顶点0开始
            if (startVertexText != null && !startVertexText.trim().isEmpty()) {
                startVertex = Integer.parseInt(startVertexText.trim());
            }
            
            if (!nodes.containsKey(startVertex)) {
                // 在邻接表显示区域添加错误信息
                String currentText = adjListDisplay.getText();
                adjListDisplay.setText(currentText + "\n\n错误: 顶点 " + startVertex + " 不存在！");
                return;
            }
            
            DFS dfs = new DFS(graph);
            System.out.println("DFS遍历结果 (从顶点 " + startVertex + " 开始):");
            dfs.traverseFromVertex(startVertex);
            System.out.println();
            
            // 在UI中高亮显示遍历过程（动画版）
            highlightDFSAnimation(dfs, startVertex);
        } catch (NumberFormatException e) {
            // 在邻接表显示区域添加错误信息
            String currentText = adjListDisplay.getText();
            adjListDisplay.setText(currentText + "\n\n错误: 请输入有效的顶点编号！");
        }
    }

    /** 执行最小生成树算法 */
    public void performMST() {
        if (nodes.isEmpty()) return;
        
        // 重置所有节点和边颜色
        resetNodeColors();
        resetEdgeColors();
        
        kruskal kruskalAlgo = new kruskal(graph);
        Edge[] mstEdges = kruskalAlgo.generateMST();
        
        if (mstEdges != null) {
            System.out.println("最小生成树边:");
            StringBuilder mstInfo = new StringBuilder("最小生成树边:\n");
            for (Edge edge : mstEdges) {
                System.out.println(edge.getMfrom() + " - " + edge.getMto() + " (权重: " + edge.getMweight() + ")");
                mstInfo.append(edge.getMfrom()).append(" - ").append(edge.getMto())
                       .append(" (权重: ").append(edge.getMweight()).append(")\n");
            }
            
            // 高亮显示最小生成树的边（动画版）
            highlightMSTAnimation(mstEdges, mstInfo.toString());
        } else {
            // 在邻接表显示区域添加错误信息
            String currentText = adjListDisplay.getText();
            adjListDisplay.setText(currentText + "\n\n最小生成树不存在！");
        }
    }

    /** 重置所有节点颜色 */
    private void resetNodeColors() {
        for (Circle circle : nodes.values()) {
            circle.setFill(Color.LIGHTGREEN);
            circle.setStroke(Color.BLACK);
        }
    }

    /** 重置所有边颜色 */
    private void resetEdgeColors() {
        for (EdgeUI edgeUI : edges.values()) {
            edgeUI.line.setStroke(Color.GRAY);
        }
    }

    /** BFS遍历动画效果 */
    private void highlightBFSAnimation(BFS bfs, int startVertex) {
        List<Integer> traversalOrder = bfs.getTraversalOrder();
        String traversalResult = bfs.getTraversalResult();
        
        // 创建动画时间线
        javafx.animation.Timeline timeline = new javafx.animation.Timeline();
        
        for (int i = 0; i < traversalOrder.size(); i++) {
            final int index = i;
            final int vertex = traversalOrder.get(i);
            
            javafx.animation.KeyFrame keyFrame = new javafx.animation.KeyFrame(
                javafx.util.Duration.millis(i * 800), // 每个节点间隔800毫秒
                e -> {
                    // 高亮当前节点
                    Circle circle = nodes.get(vertex);
                    if (circle != null) {
                        circle.setFill(Color.ORANGE);
                        circle.setStroke(Color.DARKORANGE);
                    }
                    
                    // 更新邻接表显示
                    String currentText = adjListDisplay.getText();
                    String newText = currentText + "\n\n从顶点 " + startVertex + " 开始的BFS遍历:\n" + 
                                   traversalResult + "\n当前访问: " + vertex;
                    adjListDisplay.setText(newText);
                }
            );
            timeline.getKeyFrames().add(keyFrame);
        }
        
        // 添加最终帧，重置显示
        javafx.animation.KeyFrame finalFrame = new javafx.animation.KeyFrame(
            javafx.util.Duration.millis(traversalOrder.size() * 800 + 1000),
            e -> {
                String currentText = adjListDisplay.getText();
                adjListDisplay.setText(currentText + "\n\nBFS遍历完成！");
            }
        );
        timeline.getKeyFrames().add(finalFrame);
        
        timeline.play();
    }

    /** DFS遍历动画效果 */
    private void highlightDFSAnimation(DFS dfs, int startVertex) {
        List<Integer> traversalOrder = dfs.getTraversalOrder();
        String traversalResult = dfs.getTraversalResult();
        
        // 创建动画时间线
        javafx.animation.Timeline timeline = new javafx.animation.Timeline();
        
        for (int i = 0; i < traversalOrder.size(); i++) {
            final int index = i;
            final int vertex = traversalOrder.get(i);
            
            javafx.animation.KeyFrame keyFrame = new javafx.animation.KeyFrame(
                javafx.util.Duration.millis(i * 800), // 每个节点间隔800毫秒
                e -> {
                    // 高亮当前节点
                    Circle circle = nodes.get(vertex);
                    if (circle != null) {
                        circle.setFill(Color.LIGHTBLUE);
                        circle.setStroke(Color.DARKBLUE);
                    }
                    
                    // 更新邻接表显示
                    String currentText = adjListDisplay.getText();
                    String newText = currentText + "\n\n从顶点 " + startVertex + " 开始的DFS遍历:\n" + 
                                   traversalResult + "\n当前访问: " + vertex;
                    adjListDisplay.setText(newText);
                }
            );
            timeline.getKeyFrames().add(keyFrame);
        }
        
        // 添加最终帧，重置显示
        javafx.animation.KeyFrame finalFrame = new javafx.animation.KeyFrame(
            javafx.util.Duration.millis(traversalOrder.size() * 800 + 1000),
            e -> {
                String currentText = adjListDisplay.getText();
                adjListDisplay.setText(currentText + "\n\nDFS遍历完成！");
            }
        );
        timeline.getKeyFrames().add(finalFrame);
        
        timeline.play();
    }

    /** 最小生成树动画效果 */
    private void highlightMSTAnimation(Edge[] mstEdges, String mstInfo) {
        // 创建动画时间线
        javafx.animation.Timeline timeline = new javafx.animation.Timeline();
        
        for (int i = 0; i < mstEdges.length; i++) {
            final int index = i;
            final Edge edge = mstEdges[i];
            
            javafx.animation.KeyFrame keyFrame = new javafx.animation.KeyFrame(
                javafx.util.Duration.millis(i * 1000), // 每条边间隔1秒
                e -> {
                    // 高亮当前边
                    String edgeKey = edge.getMfrom() + "-" + edge.getMto();
                    EdgeUI edgeUI = edges.get(edgeKey);
                    if (edgeUI != null) {
                        edgeUI.line.setStroke(Color.GREEN);
                        edgeUI.line.setStrokeWidth(3);
                    }
                    
                    // 高亮相关节点
                    Circle fromCircle = nodes.get(edge.getMfrom());
                    Circle toCircle = nodes.get(edge.getMto());
                    if (fromCircle != null) {
                        fromCircle.setFill(Color.LIGHTGREEN);
                        fromCircle.setStroke(Color.DARKGREEN);
                    }
                    if (toCircle != null) {
                        toCircle.setFill(Color.LIGHTGREEN);
                        toCircle.setStroke(Color.DARKGREEN);
                    }
                    
                    // 更新邻接表显示
                    String currentText = adjListDisplay.getText();
                    String newText = currentText + "\n\n最小生成树构建中...\n" + 
                                   mstInfo + "\n当前添加边: " + edge.getMfrom() + " - " + edge.getMto() + " (权重: " + edge.getMweight() + ")";
                    adjListDisplay.setText(newText);
                }
            );
            timeline.getKeyFrames().add(keyFrame);
        }
        
        // 添加最终帧，显示完成信息
        javafx.animation.KeyFrame finalFrame = new javafx.animation.KeyFrame(
            javafx.util.Duration.millis(mstEdges.length * 1000 + 1000),
            e -> {
                String currentText = adjListDisplay.getText();
                adjListDisplay.setText(currentText + "\n\n最小生成树构建完成！");
            }
        );
        timeline.getKeyFrames().add(finalFrame);
        
        timeline.play();
    }
    
    /** 清空邻接表显示区域 */
    public void clearDisplay() {
        // 重置所有节点颜色
        resetNodeColors();
        
        // 重置所有边颜色
        resetEdgeColors();
        
        // 只显示原始的邻接表，清除所有遍历过程信息
        updateAdjListDisplay();
    }
    
    /** 清空所有边 */
    public void clearAllEdges() {
        // 清空图数据结构中的所有边
        graph.clearAllEdges();
        
        // 清空UI中的所有边
        for (EdgeUI edgeUI : edges.values()) {
            graphPane.getChildren().removeAll(edgeUI.line, edgeUI.label);
        }
        edges.clear();
        
        // 更新显示
        updateAdjListDisplay();
    }
    
    /** 随机生成图 */
    public void generateRandomGraph() {
        // 随机生成图
        graph.generateRandomGraph();
        
        // 清空UI中的所有边
        for (EdgeUI edgeUI : edges.values()) {
            graphPane.getChildren().removeAll(edgeUI.line, edgeUI.label);
        }
        edges.clear();
        
        // 重新添加所有边到UI
        int n = graph.verticesNumber();
        for (int i = 0; i < n; i++) {
            for (Edge e = graph.firstEdge(i); e != null; e = graph.nextEdge(e)) {
                int from = e.getMfrom();
                int to = e.getMto();
                int weight = e.getMweight();
                
                // 只添加一次边（避免重复）
                String edgeKey = from + "-" + to;
                if (!edges.containsKey(edgeKey) && from < to) {
                    Circle c1 = nodes.get(from);
                    Circle c2 = nodes.get(to);
                    
                    if (c1 != null && c2 != null) {
                        Line line = new Line(c1.getCenterX(), c1.getCenterY(), c2.getCenterX(), c2.getCenterY());
                        line.setStrokeWidth(2);
                        line.setStroke(Color.GRAY);
                        
                        Text text = new Text(
                            (c1.getCenterX() + c2.getCenterX()) / 2,
                            (c1.getCenterY() + c2.getCenterY()) / 2 - 5,
                            String.valueOf(weight)
                        );
                        text.setFill(Color.DARKRED);
                        
                        graphPane.getChildren().addAll(line, text);
                        edges.put(edgeKey, new EdgeUI(line, text));
                    }
                }
            }
        }
        
        // 更新显示
        updateAdjListDisplay();
    }
}
