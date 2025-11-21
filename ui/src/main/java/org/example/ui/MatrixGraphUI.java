package org.example.ui;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import org.example.core.MatrixGraph;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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

    // 存储边UI的Map (Key: "from-to")
    private Map<String, EdgeUI> edges = new HashMap<>();

    public MatrixGraphUI(MatrixGraph graph) {
        this.graph = graph;
        
        // 创建根布局
        root = new BorderPane();
        // 宽度限制为 1050，确保能在 1100 的主窗口中显示完全
        root.setPrefSize(1050, 600);
        
        // 左侧：图显示区域
        graphPane = new Pane();
        graphPane.setPrefSize(650, 600);
        graphPane.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dee2e6; -fx-border-width: 1;");
        
        // 右侧上部：邻接矩阵显示区域
        // 注意：这里不再创建控制按钮，按钮由 MainApp 在右上角统一管理
        VBox matrixPane = new VBox(10);
        matrixPane.setPadding(new Insets(15));
        matrixPane.setPrefHeight(350);
        matrixPane.setStyle("-fx-background-color: #ffffff; -fx-border-color: #dee2e6; -fx-border-width: 1;");
        
        Text matrixTitle = new Text("邻接矩阵数据");
        matrixTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-fill: #2c3e50;");
        
        // 滚动面板防止矩阵过大
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(300);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: #ffffff;");
        
        matrixDisplay = new Text();
        matrixDisplay.setStyle("-fx-font-family: 'Monaco', 'Menlo', 'Consolas', monospace; -fx-font-size: 12px; -fx-fill: #34495e;");
        
        scrollPane.setContent(matrixDisplay);
        matrixPane.getChildren().addAll(matrixTitle, scrollPane);
        
        // 组合右侧面板
        VBox rightPane = new VBox(10);
        rightPane.setPrefWidth(380); // 限制宽度
        rightPane.setPadding(new Insets(0, 0, 0, 10));
        rightPane.getChildren().add(matrixPane); // 只加显示区，控制区在 MainApp
        VBox.setVgrow(matrixPane, Priority.ALWAYS);
        
        root.setLeft(graphPane);
        root.setRight(rightPane);
        
        // 初始化显示
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

        if (id >= graph.verticesNumber()) {
            while (graph.verticesNumber() <= id) {
                graph.addVertex();
            }
        }
        
        graph.setVertexExists(id, true);

        Circle circle = new Circle(20, Color.LIGHTBLUE);
        circle.setStroke(Color.BLACK);
        circle.setStrokeWidth(2);
        
        enableDrag(circle, id);
        
        Text label = new Text(String.valueOf(id));

        graphPane.getChildren().addAll(circle, label);
        nodes.put(id, circle);
        nodeLabels.put(id, label);

        updateNodePositions();
        updateMatrixDisplay();
    }
    
    /** 启用节点拖拽 */
    private void enableDrag(Circle circle, int id) {
        final class Delta { double x, y; }
        final Delta dragDelta = new Delta();
        
        circle.setOnMousePressed(e -> {
            dragDelta.x = circle.getCenterX() - e.getX();
            dragDelta.y = circle.getCenterY() - e.getY();
            circle.setCursor(javafx.scene.Cursor.MOVE);
        });
        
        circle.setOnMouseDragged(e -> {
            double newX = e.getX() + dragDelta.x;
            double newY = e.getY() + dragDelta.y;
            
            newX = Math.max(20, Math.min(graphPane.getPrefWidth() - 20, newX));
            newY = Math.max(20, Math.min(graphPane.getPrefHeight() - 20, newY));
            
            circle.setCenterX(newX);
            circle.setCenterY(newY);
            
            Text label = nodeLabels.get(id);
            if (label != null) {
                label.setX(newX - 6);
                label.setY(newY + 6);
            }
            
            updateConnectedEdges(id);
        });
        
        circle.setOnMouseReleased(e -> circle.setCursor(javafx.scene.Cursor.HAND));
    }

    private void updateConnectedEdges(int id) {
        for (Map.Entry<String, EdgeUI> entry : edges.entrySet()) {
            String[] parts = entry.getKey().split("-");
            int from = Integer.parseInt(parts[0]);
            int to = Integer.parseInt(parts[1]);
            
            if (from == id || to == id) {
                Circle c1 = nodes.get(from);
                Circle c2 = nodes.get(to);
                if(c1 != null && c2 != null) {
                    Line line = entry.getValue().line;
                    line.setStartX(c1.getCenterX()); line.setStartY(c1.getCenterY());
                    line.setEndX(c2.getCenterX());   line.setEndY(c2.getCenterY());
                    
                    Text label = entry.getValue().label;
                    label.setX((c1.getCenterX() + c2.getCenterX()) / 2);
                    label.setY((c1.getCenterY() + c2.getCenterY()) / 2 - 5);
                }
            }
        }
    }

    /** 删除顶点及相关边 */
    public void removeVertex(int id) {
        if (!nodes.containsKey(id)) return;

        Circle circle = nodes.remove(id);
        Text label = nodeLabels.remove(id);
        if (circle != null) graphPane.getChildren().remove(circle);
        if (label != null) graphPane.getChildren().remove(label);

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

        String edgeKey = from + "-" + to;
        
        if (edges.containsKey(edgeKey)) {
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

        graphPane.getChildren().add(0, line); 
        graphPane.getChildren().add(text);
        
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

    private void updateNodePositions() {
        int n = nodes.size();
        if (n == 0) return;

        double width = graphPane.getPrefWidth();
        double height = graphPane.getPrefHeight();
        double centerX = width / 2;
        double centerY = height * 0.4;
        double radius = Math.min(centerX, centerY) * 0.7;

        int i = 0;
        List<Integer> sortedKeys = nodes.keySet().stream().sorted().toList();
        
        for (Integer vertexId : sortedKeys) {
            double angle = 2 * Math.PI * i / n - Math.PI / 2;
            double x = centerX + radius * Math.cos(angle);
            double y = centerY + radius * Math.sin(angle);
            
            // 仅当是新节点时计算，loadGraph 会覆盖
            Circle circle = nodes.get(vertexId);
            circle.setCenterX(x);
            circle.setCenterY(y);
            
            Text t = nodeLabels.get(vertexId);
            t.setX(x - 6);
            t.setY(y + 6);
            
            i++;
        }
        
        for (Map.Entry<String, EdgeUI> entry : edges.entrySet()) {
            String[] parts = entry.getKey().split("-");
            int from = Integer.parseInt(parts[0]);
            int to = Integer.parseInt(parts[1]);
            
            Circle c1 = nodes.get(from);
            Circle c2 = nodes.get(to);
            
            if (c1 != null && c2 != null) {
                Line l = entry.getValue().line;
                l.setStartX(c1.getCenterX()); l.setStartY(c1.getCenterY());
                l.setEndX(c2.getCenterX());   l.setEndY(c2.getCenterY());
                
                Text t = entry.getValue().label;
                t.setX((c1.getCenterX() + c2.getCenterX()) / 2);
                t.setY((c1.getCenterY() + c2.getCenterY()) / 2 - 5);
            }
        }
    }
    
    // --- 文件操作 ---
    
    public void saveGraph() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("保存邻接矩阵图");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Graph Files", "*.graph"));
        fileChooser.setInitialFileName("matrix_graph.graph");
        File file = fileChooser.showSaveDialog(root.getScene().getWindow());

        if (file == null) return;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            // 保存存在的顶点: V,id,x,y
            for (Map.Entry<Integer, Circle> entry : nodes.entrySet()) {
                int id = entry.getKey();
                Circle c = entry.getValue();
                writer.write(String.format("V,%d,%.2f,%.2f", id, c.getCenterX(), c.getCenterY()));
                writer.newLine();
            }

            // 保存边: E,from,to,weight
            // 遍历矩阵保存所有权重非0的边
            int n = graph.verticesNumber();
            for (int i = 0; i < n; i++) {
                if (!graph.isVertexExists(i)) continue;
                for (int j = 0; j < n; j++) {
                    if (!graph.isVertexExists(j)) continue;
                    
                    int weight = graph.getEdge(i, j);
                    if (weight != 0) {
                        writer.write(String.format("E,%d,%d,%d", i, j, weight));
                        writer.newLine();
                    }
                }
            }
            System.out.println("矩阵图保存成功: " + file.getAbsolutePath());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    public void loadGraph() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("打开图文件");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Graph Files", "*.graph"));
        File file = fileChooser.showOpenDialog(root.getScene().getWindow());

        if (file == null) return;

        // 清空 UI 和 数据
        graph.clearAllEdges();
        // 重置顶点存在状态 (假设我们保留图对象，只重置状态)
        int currentMax = graph.verticesNumber();
        for(int i=0; i<currentMax; i++) graph.setVertexExists(i, false);
        
        nodes.clear();
        nodeLabels.clear();
        graphPane.getChildren().clear();
        edges.clear();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 0) continue;

                if (parts[0].equals("V")) {
                    int id = Integer.parseInt(parts[1]);
                    double x = Double.parseDouble(parts[2]);
                    double y = Double.parseDouble(parts[3]);

                    addVertex(id);
                    
                    // 恢复坐标
                    Circle c = nodes.get(id);
                    if (c != null) {
                        c.setCenterX(x); c.setCenterY(y);
                        Text t = nodeLabels.get(id);
                        if(t != null) { t.setX(x-6); t.setY(y+6); }
                    }
                } else if (parts[0].equals("E")) {
                    int from = Integer.parseInt(parts[1]);
                    int to = Integer.parseInt(parts[2]);
                    int weight = Integer.parseInt(parts[3]);
                    addEdge(from, to, weight);
                }
            }
            updateMatrixDisplay();
            System.out.println("矩阵图加载成功");
        } catch (Exception ex) {
            ex.printStackTrace();
            matrixDisplay.setText("加载失败: " + ex.getMessage());
        }
    }
}