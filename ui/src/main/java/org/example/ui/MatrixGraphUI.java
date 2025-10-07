package org.example.ui;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import org.example.core.MatrixGraph;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MatrixGraphUI {

    private Pane pane;
    private MatrixGraph graph;

    // 节点和节点标签
    private Map<Integer, Circle> nodes = new HashMap<>();
    private Map<Integer, Text> nodeLabels = new HashMap<>();

    // EdgeUI 包装 Line + 权重 Text
    private static class EdgeUI {
        Line line;
        Text label;
        EdgeUI(Line line, Text label) { this.line = line; this.label = label; }
    }
    private Map<String, EdgeUI> edges = new HashMap<>();

    public MatrixGraphUI(MatrixGraph graph) {
        this.graph = graph;
        this.pane = new Pane();
        this.pane.setPrefSize(800, 600);
    }

    public Pane getPane() {
        return pane;
    }

    /** 添加顶点，自动圆形排列 */
    public void addVertex(int id) {
        if (nodes.containsKey(id)) return;

        Circle circle = new Circle(20, Color.LIGHTBLUE);
        circle.setStroke(Color.BLACK);
        circle.setStrokeWidth(2);
        Text label = new Text(String.valueOf(id));

        pane.getChildren().addAll(circle, label);
        nodes.put(id, circle);
        nodeLabels.put(id, label);

        updateNodePositions();
    }

    /** 删除顶点及相关边 */
    public void removeVertex(int id) {
        Circle circle = nodes.remove(id);
        Text label = nodeLabels.remove(id);
        if (circle != null) pane.getChildren().remove(circle);
        if (label != null) pane.getChildren().remove(label);

        // 删除相关边
        Iterator<Map.Entry<String, EdgeUI>> it = edges.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, EdgeUI> entry = it.next();
            String key = entry.getKey();
            String[] parts = key.split("-");
            int from = Integer.parseInt(parts[0]);
            int to = Integer.parseInt(parts[1]);
            if (from == id || to == id) {
                graph.delEdge(from, to);
                pane.getChildren().removeAll(entry.getValue().line, entry.getValue().label);
                it.remove();
            }
        }

        updateNodePositions();
    }

    /** 添加边 */
    public void addEdge(int from, int to, int weight) {
        if (!nodes.containsKey(from) || !nodes.containsKey(to)) return;

        graph.setEdge(from, to, weight);

        Circle c1 = nodes.get(from);
        Circle c2 = nodes.get(to);

        Line line = new Line(c1.getCenterX(), c1.getCenterY(), c2.getCenterX(), c2.getCenterY());
        line.setStrokeWidth(2);
        line.setStroke(Color.GRAY);

        Text text = new Text((c1.getCenterX() + c2.getCenterX()) / 2,
                (c1.getCenterY() + c2.getCenterY()) / 2,
                String.valueOf(weight));

        pane.getChildren().addAll(line, text);
        edges.put(from + "-" + to, new EdgeUI(line, text));
    }

    /** 删除边 */
    public void removeEdge(int from, int to) {
        graph.delEdge(from, to);
        String key = from + "-" + to;
        EdgeUI edgeUI = edges.remove(key);
        if (edgeUI != null) {
            pane.getChildren().removeAll(edgeUI.line, edgeUI.label);
        }
    }

    /** 重新排列节点，圆形布局，更新边坐标和权重 */
    private void updateNodePositions() {
        int n = nodes.size();
        if (n == 0) return;

        double centerX = pane.getPrefWidth() / 2;
        double centerY = pane.getPrefHeight() / 2;
        double radius = Math.min(centerX, centerY) - 50;

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

        // 更新节点标签位置
        for (Map.Entry<Integer, Text> entry : nodeLabels.entrySet()) {
            int id = entry.getKey();
            if (positions.containsKey(id)) {
                double[] pos = positions.get(id);
                entry.getValue().setX(pos[0] - 5);
                entry.getValue().setY(pos[1] + 5);
            }
        }

        // 更新所有边和权重位置
        for (Map.Entry<String, EdgeUI> entry : edges.entrySet()) {
            String key = entry.getKey();
            String[] parts = key.split("-");
            int from = Integer.parseInt(parts[0]);
            int to = Integer.parseInt(parts[1]);
            Circle c1 = nodes.get(from);
            Circle c2 = nodes.get(to);
            Line line = entry.getValue().line;
            line.setStartX(c1.getCenterX());
            line.setStartY(c1.getCenterY());
            line.setEndX(c2.getCenterX());
            line.setEndY(c2.getCenterY());

            Text text = entry.getValue().label;
            text.setX((c1.getCenterX() + c2.getCenterX()) / 2);
            text.setY((c1.getCenterY() + c2.getCenterY()) / 2);
        }
    }
}