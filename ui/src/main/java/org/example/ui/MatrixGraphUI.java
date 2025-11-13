package org.example.ui;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import org.example.core.AdjListGraph;
import org.example.core.BFS;
import org.example.core.DFS;
import org.example.core.MatrixGraph;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MatrixGraphUI {

    private BorderPane root;
    private Pane graphPane;
    private Text matrixDisplay;
    private MatrixGraph graph;

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
        
        // 创建根布局
        root = new BorderPane();
        root.setPrefSize(1200, 700);
        
        // 左侧：图显示区域
        graphPane = new Pane();
        graphPane.setPrefSize(600, 700);
        graphPane.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-width: 1;");
        
        // 中间：邻接矩阵显示区域
        VBox matrixPane = new VBox();
        matrixPane.setPrefSize(400, 700);
        matrixPane.setPadding(new Insets(15));
        matrixPane.setStyle("-fx-background-color: #ffffff; -fx-border-color: #dee2e6; -fx-border-width: 1;");
        
        Text matrixTitle = new Text("邻接矩阵");
        matrixTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-fill: #2c3e50;");
        
        matrixDisplay = new Text();
        matrixDisplay.setStyle("-fx-font-family: 'Monaco', 'Menlo', 'Consolas', monospace; -fx-font-size: 14px; -fx-fill: #34495e;");
        
        matrixPane.getChildren().addAll(matrixTitle, matrixDisplay);
        
        root.setLeft(graphPane);
        root.setCenter(matrixPane);
        
        updateMatrixDisplay();
    }

    public Pane getPane() {
        return root;
    }

    /** 更新邻接矩阵显示 */
    private void updateMatrixDisplay() {
        String matrixString = graph.getMatrixString();
        matrixDisplay.setText(matrixString);
    }

    /** 添加顶点 */
    public void addVertex(int id) {
        if (nodes.containsKey(id)) return;

        // 调用图的addVertex方法动态添加顶点
        graph.addVertex();
        
        // 标记顶点存在
        graph.setVertexExists(id, true);

        Circle circle = new Circle(20, Color.LIGHTBLUE);
        circle.setStroke(Color.BLACK);
        circle.setStrokeWidth(2);
        Text label = new Text(String.valueOf(id));

        graphPane.getChildren().addAll(circle, label);
        nodes.put(id, circle);
        nodeLabels.put(id, label);

        updateNodePositions();
        updateMatrixDisplay();
    }

    /** 删除顶点及相关边 */
    public void removeVertex(int id) {
        Circle circle = nodes.remove(id);
        Text label = nodeLabels.remove(id);
        if (circle != null) graphPane.getChildren().remove(circle);
        if (label != null) graphPane.getChildren().remove(label);

        // 标记顶点不存在
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

        graph.setEdge(from, to, weight);

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
        
        updateMatrixDisplay();
    }

    /** 删除边 */
    public void removeEdge(int from, int to) {
        graph.delEdge(from, to);
        String key = from + "-" + to;
        EdgeUI edgeUI = edges.remove(key);
        if (edgeUI != null) {
            graphPane.getChildren().removeAll(edgeUI.line, edgeUI.label);
        }
        updateMatrixDisplay();
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
    
    /** 创建遍历控制面板 */
    private VBox createControlPanel() {
        VBox controlPane = new VBox();
        controlPane.setPrefSize(200, 700);
        controlPane.setPadding(new Insets(15));
        controlPane.setSpacing(10);
        controlPane.setStyle("-fx-background-color: #f0f8ff; -fx-border-color: #dee2e6; -fx-border-width: 1;");
        
        Text controlTitle = new Text("遍历控制");
        controlTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-fill: #2c3e50;");
        
        // 起始顶点选择
        Label startLabel = new Label("起始顶点:");
        startLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        TextField startVertexField = new TextField();
        startVertexField.setPromptText("输入顶点编号");
        startVertexField.setStyle("-fx-font-size: 14px;");
        
        // 遍历结果显示
        TextArea resultArea = new TextArea();
        resultArea.setPrefHeight(200);
        resultArea.setEditable(false);
        resultArea.setStyle("-fx-font-family: 'Monaco', 'Menlo', 'Consolas', monospace; -fx-font-size: 12px;");
        resultArea.setPromptText("遍历结果将显示在这里...");
        
        // BFS按钮
        Button bfsButton = new Button("BFS遍历");
        bfsButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px; -fx-pref-width: 150;");
        bfsButton.setOnAction(e -> {
            try {
                int startVertex = Integer.parseInt(startVertexField.getText().trim());
                if (nodes.containsKey(startVertex)) {
                    AdjListGraph adjListGraph = convertToAdjListGraph();
                    BFS bfs = new BFS(adjListGraph);
                    bfs.traverseFromVertex(startVertex);
                    resultArea.setText("BFS遍历结果:\n" + bfs.getTraversalResult());
                } else {
                    resultArea.setText("错误: 顶点 " + startVertex + " 不存在！");
                }
            } catch (NumberFormatException ex) {
                resultArea.setText("错误: 请输入有效的顶点编号！");
            }
        });
        
        // DFS按钮
        Button dfsButton = new Button("DFS遍历");
        dfsButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 14px; -fx-pref-width: 150;");
        dfsButton.setOnAction(e -> {
            try {
                int startVertex = Integer.parseInt(startVertexField.getText().trim());
                if (nodes.containsKey(startVertex)) {
                    AdjListGraph adjListGraph = convertToAdjListGraph();
                    DFS dfs = new DFS(adjListGraph);
                    dfs.traverseFromVertex(startVertex);
                    resultArea.setText("DFS遍历结果:\n" + dfs.getTraversalResult());
                } else {
                    resultArea.setText("错误: 顶点 " + startVertex + " 不存在！");
                }
            } catch (NumberFormatException ex) {
                resultArea.setText("错误: 请输入有效的顶点编号！");
            }
        });
        
        // 完整遍历按钮
        Button fullTraverseButton = new Button("完整遍历");
        fullTraverseButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-size: 14px; -fx-pref-width: 150;");
        fullTraverseButton.setOnAction(e -> {
            AdjListGraph adjListGraph = convertToAdjListGraph();
            BFS bfs = new BFS(adjListGraph);
            DFS dfs = new DFS(adjListGraph);
            
            bfs.traverseGraph();
            dfs.traverseGraph();
            
            StringBuilder result = new StringBuilder();
            result.append("=== 完整遍历结果 ===\n");
            result.append("BFS遍历顺序: ").append(bfs.getTraversalOrder()).append("\n");
            result.append("DFS遍历顺序: ").append(dfs.getTraversalOrder()).append("\n\n");
            result.append("BFS: ").append(bfs.getTraversalResult()).append("\n");
            result.append("DFS: ").append(dfs.getTraversalResult());
            
            resultArea.setText(result.toString());
        });
        
        // 清空结果按钮
        Button clearButton = new Button("清空结果");
        clearButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 14px; -fx-pref-width: 150;");
        clearButton.setOnAction(e -> resultArea.clear());
        
        controlPane.getChildren().addAll(
            controlTitle, startLabel, startVertexField, 
            bfsButton, dfsButton, fullTraverseButton, clearButton,
            new Label("遍历结果:"), resultArea
        );
        
        return controlPane;
    }
    
    /** 将MatrixGraph转换为AdjListGraph用于遍历 */
    private AdjListGraph convertToAdjListGraph() {
        int maxVertex = 0;
        for (Integer vertex : nodes.keySet()) {
            if (vertex > maxVertex) maxVertex = vertex;
        }
        
        AdjListGraph adjListGraph = new AdjListGraph(maxVertex + 1);
        
        // 添加所有存在的顶点
        for (Integer vertex : nodes.keySet()) {
            // AdjListGraph会自动处理顶点存在性
        }
        
        // 添加所有边
        for (Map.Entry<String, EdgeUI> entry : edges.entrySet()) {
            String key = entry.getKey();
            String[] parts = key.split("-");
            int from = Integer.parseInt(parts[0]);
            int to = Integer.parseInt(parts[1]);
            // 从MatrixGraph获取权重
            int weight = graph.getEdge(from, to);
            adjListGraph.setEdge(from, to, weight);
        }
        
        return adjListGraph;
    }
}
