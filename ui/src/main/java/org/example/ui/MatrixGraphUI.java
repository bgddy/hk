package org.example.ui;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import org.example.core.AdjListGraph;
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
        VBox matrixPane = new VBox(10); // 添加间距
        matrixPane.setPrefSize(400, 700);
        matrixPane.setPadding(new Insets(15));
        matrixPane.setStyle("-fx-background-color: #ffffff; -fx-border-color: #dee2e6; -fx-border-width: 1;");
        
        Text matrixTitle = new Text("邻接矩阵");
        matrixTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-fill: #2c3e50;");
        
        // --- 修复 1: 添加 ScrollPane 以防止矩阵内容超出显示范围 ---
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(600); // 设置合适的高度
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: #ffffff;");
        
        matrixDisplay = new Text();
        matrixDisplay.setStyle("-fx-font-family: 'Monaco', 'Menlo', 'Consolas', monospace; -fx-font-size: 14px; -fx-fill: #34495e;");
        
        // 将 Text 放入 ScrollPane
        scrollPane.setContent(matrixDisplay);
        
        matrixPane.getChildren().addAll(matrixTitle, scrollPane);
        
        root.setLeft(graphPane);
        root.setCenter(matrixPane);
        
        updateMatrixDisplay();
    }

    public Pane getPane() {
        return root;
    }

    /** 更新邻接矩阵显示 */
    private void updateMatrixDisplay() {
        if (graph != null) {
            String matrixString = graph.getMatrixString();
            matrixDisplay.setText(matrixString);
        }
    }

    /** 添加顶点 */
    public void addVertex(int id) {
        if (nodes.containsKey(id)) return;

        // --- 修复 2: 防止逻辑重复导致的图无限扩大 ---
        // 只有当 ID 超过当前图的容量范围时，才调用底层的 addVertex 扩展图
        // 如果 MainApp 初始化了 5 个点，这里添加 id=0 时就不应该再增加图的大小
        if (id >= graph.verticesNumber()) {
            graph.addVertex();
        }
        
        // 标记顶点存在
        graph.setVertexExists(id, true);

        // UI 绘制部分
        Circle circle = new Circle(20, Color.LIGHTBLUE);
        circle.setStroke(Color.BLACK);
        circle.setStrokeWidth(2);
        Text label = new Text(String.valueOf(id));

        graphPane.getChildren().addAll(circle, label);
        nodes.put(id, circle);
        nodeLabels.put(id, label);

        updateNodePositions();
        updateMatrixDisplay(); // 触发界面更新
    }

    /** 删除顶点及相关边 */
    public void removeVertex(int id) {
        if (!nodes.containsKey(id)) return;

        Circle circle = nodes.remove(id);
        Text label = nodeLabels.remove(id);
        if (circle != null) graphPane.getChildren().remove(circle);
        if (label != null) graphPane.getChildren().remove(label);

        // 标记顶点不存在
        graph.setVertexExists(id, false);

        // 清理相关边
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
        // 确保两个顶点都在 UI 上存在
        if (!nodes.containsKey(from) || !nodes.containsKey(to)) {
            System.out.println("顶点不存在，无法添加边: " + from + " -> " + to);
            return;
        }

        // 检查是否已经存在该边，如果存在则先删除旧的 UI
        String edgeKey = from + "-" + to;
        if (edges.containsKey(edgeKey)) {
            EdgeUI oldEdge = edges.get(edgeKey);
            graphPane.getChildren().removeAll(oldEdge.line, oldEdge.label);
            edges.remove(edgeKey);
        }

        // 更新底层数据结构
        graph.setEdge(from, to, weight);

        // 绘制边
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

        // 确保边画在顶点圆形的下面 (Line 放在 children 列表的前面)
        graphPane.getChildren().add(0, line); 
        graphPane.getChildren().add(text);
        
        edges.put(edgeKey, new EdgeUI(line, text));
        
        updateMatrixDisplay(); // 触发界面更新
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

        double centerX = paneWidth / 2;
        double centerY = paneHeight * 0.35; 

        double base = Math.min(centerX, centerY);
        double radius = base * (0.45 + 0.4 / Math.max(n, 3)); 

        int i = 0;
        Map<Integer, double[]> positions = new HashMap<>();
        
        // 对 Key 进行排序，保证顶点按顺序排列 (0, 1, 2...)，防止每次刷新位置乱跳
        java.util.List<Integer> sortedKeys = new java.util.ArrayList<>(nodes.keySet());
        java.util.Collections.sort(sortedKeys);

        for (Integer vertexId : sortedKeys) {
            Circle circle = nodes.get(vertexId);
            double angle = 2 * Math.PI * i / n - Math.PI / 2; // 从正上方开始 (-90度)
            double x = centerX + radius * Math.cos(angle);
            double y = centerY + radius * Math.sin(angle);
            
            circle.setCenterX(x);
            circle.setCenterY(y);
            positions.put(vertexId, new double[]{x, y});
            i++;
        }

        // 更新标签位置
        for (Map.Entry<Integer, Text> entry : nodeLabels.entrySet()) {
            int id = entry.getKey();
            if (positions.containsKey(id)) {
                double[] pos = positions.get(id);
                entry.getValue().setX(pos[0] - 6);
                entry.getValue().setY(pos[1] + 6);
            }
        }

        // 更新边的位置
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
    
    /** 创建遍历控制面板 (保持原样或按需使用) */
    private VBox createControlPanel() {
        // ... 代码与你之前的一样，可以保留 ...
        return new VBox(); // 占位
    }
    
    /** 将MatrixGraph转换为AdjListGraph用于遍历 */
    private AdjListGraph convertToAdjListGraph() {
        int maxVertex = 0;
        for (Integer vertex : nodes.keySet()) {
            if (vertex > maxVertex) maxVertex = vertex;
        }
        
        AdjListGraph adjListGraph = new AdjListGraph(maxVertex + 1);
        
        for (Map.Entry<String, EdgeUI> entry : edges.entrySet()) {
            String key = entry.getKey();
            String[] parts = key.split("-");
            int from = Integer.parseInt(parts[0]);
            int to = Integer.parseInt(parts[1]);
            int weight = graph.getEdge(from, to);
            adjListGraph.setEdge(from, to, weight);
        }
        return adjListGraph;
    }
}