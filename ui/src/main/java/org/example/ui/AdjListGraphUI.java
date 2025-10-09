package org.example.ui;

import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import org.example.core.AdjListGraph;
import org.example.core.Edge;

import java.util.ArrayList;
import java.util.List;

public class AdjListGraphUI {

    private AdjListGraph graph;
    private Group root;

    private List<Circle> vertexNodes = new ArrayList<>();
    private List<Line> edgeLines = new ArrayList<>();
    private List<Text> edgeWeights = new ArrayList<>();

    private Thread currentAlgorithmThread;

    public AdjListGraphUI(AdjListGraph graph) {
        this.graph = graph;
        this.root = new Group();
        drawVertices();
    }

    public Group getRoot() {
        return root;
    }

   public void drawVertices() {
        int n = graph.vecticesNumber();
        vertexNodes.clear();
        root.getChildren().clear();

        double width = 800;
        double height = 400;
        double nodeRadius = 20;
        double minSpacing = 10;

        // 画布中心向上移动
        double centerX = width / 2;
        double centerY = height / 2 - 50; // 往上提 50px

        // 根据顶点数动态计算半径，保证间距更大
        double maxRadius = Math.min(width, height) / 2 - nodeRadius - 10;
        double radius = n > 1 ? Math.min(maxRadius, n * (nodeRadius * 6 + minSpacing) / (2 * Math.PI)) : 0;
        // nodeRadius*3 让间距更大

        for (int i = 0; i < n; i++) {
            double angle = 2 * Math.PI * i / n;
            double x = centerX + radius * Math.cos(angle);
            double y = centerY + radius * Math.sin(angle);
            Circle circle = new Circle(x, y, nodeRadius, Color.LIGHTBLUE);
            Text label = new Text(x - 5, y + 5, String.valueOf(i));
            vertexNodes.add(circle);
            root.getChildren().addAll(circle, label);
        }

        drawEdges();
    }
    public void drawEdges() {
        root.getChildren().removeAll(edgeLines);
        root.getChildren().removeAll(edgeWeights);
        edgeLines.clear();
        edgeWeights.clear();

        int n = graph.vecticesNumber();
        for (int i = 0; i < n; i++) {
            for (Edge e = graph.firstEdge(i); e != null; e = graph.nextEdge(e)) {
                int from = e.getMfrom();
                int to = e.getMto();
                if (from < to) {
                    Circle c1 = vertexNodes.get(from);
                    Circle c2 = vertexNodes.get(to);
                    Line line = new Line(c1.getCenterX(), c1.getCenterY(), c2.getCenterX(), c2.getCenterY());
                    line.setStrokeWidth(2);
                    line.setStroke(Color.GRAY);

                    Text weightText = new Text((c1.getCenterX() + c2.getCenterX()) / 2,
                            (c1.getCenterY() + c2.getCenterY()) / 2, String.valueOf(e.getMweight()));

                    edgeLines.add(line);
                    edgeWeights.add(weightText);
                    root.getChildren().addAll(line, weightText);
                }
            }
        }
    }

    public void insertEdge(int from, int to, int weight) {
        graph.setEdge(from, to, weight);
        drawVertices();
    }

    public void deleteEdge(int from, int to) {
        graph.delEdge(from, to);
        drawVertices();
    }

    public void resetHighlights() {
        Platform.runLater(() -> {
            for (Circle c : vertexNodes) c.setFill(Color.LIGHTBLUE);
            for (Line l : edgeLines) l.setStroke(Color.GRAY);
        });
    }

    public void visualizeDFS(int start) {
        if (currentAlgorithmThread != null && currentAlgorithmThread.isAlive())
            currentAlgorithmThread.interrupt();
        resetHighlights();
        currentAlgorithmThread = new Thread(() -> dfsHelper(start, new boolean[graph.vecticesNumber()]));
        currentAlgorithmThread.start();
    }

    private void dfsHelper(int v, boolean[] visited) {
        if (Thread.currentThread().isInterrupted()) return;
        visited[v] = true;
        highlightVertex(v, Color.GREEN);
        try { Thread.sleep(500); } catch (InterruptedException e) { return; }
        for (Edge e = graph.firstEdge(v); e != null; e = graph.nextEdge(e)) {
            int to = e.getMto();
            if (!visited[to]) dfsHelper(to, visited);
        }
    }

    public void visualizeBFS(int start) {
        if (currentAlgorithmThread != null && currentAlgorithmThread.isAlive())
            currentAlgorithmThread.interrupt();
        resetHighlights();
        currentAlgorithmThread = new Thread(() -> {
            boolean[] visited = new boolean[graph.vecticesNumber()];
            java.util.List<Integer> queue = new java.util.ArrayList<>();
            queue.add(start);
            visited[start] = true;
            highlightVertex(start, Color.GREEN);
            while (!queue.isEmpty()) {
                if (Thread.currentThread().isInterrupted()) return;
                int v = queue.remove(0);
                for (Edge e = graph.firstEdge(v); e != null; e = graph.nextEdge(e)) {
                    int to = e.getMto();
                    if (!visited[to]) {
                        visited[to] = true;
                        highlightVertex(to, Color.GREEN);
                        try { Thread.sleep(500); } catch (InterruptedException ex) { return; }
                        queue.add(to);
                    }
                }
            }
        });
        currentAlgorithmThread.start();
    }

    public void visualizeKruskal(Edge[] mstEdges) {
        if (currentAlgorithmThread != null && currentAlgorithmThread.isAlive())
            currentAlgorithmThread.interrupt();
        resetHighlights();
        currentAlgorithmThread = new Thread(() -> {
            for (Edge e : mstEdges) {
                if (Thread.currentThread().isInterrupted()) return;
                highlightEdge(e.getMfrom(), e.getMto(), Color.RED);
                try { Thread.sleep(500); } catch (InterruptedException ex) { return; }
            }
        });
        currentAlgorithmThread.start();
    }

    private void highlightVertex(int v, Color color) {
        Platform.runLater(() -> vertexNodes.get(v).setFill(color));
    }

    private void highlightEdge(int from, int to, Color color) {
        Platform.runLater(() -> {
            for (Line line : edgeLines) {
                Circle c1 = vertexNodes.get(from);
                Circle c2 = vertexNodes.get(to);
                if ((line.getStartX() == c1.getCenterX() && line.getStartY() == c1.getCenterY() &&
                        line.getEndX() == c2.getCenterX() && line.getEndY() == c2.getCenterY()) ||
                        (line.getStartX() == c2.getCenterX() && line.getStartY() == c2.getCenterY() &&
                                line.getEndX() == c1.getCenterX() && line.getEndY() == c1.getCenterY())) {
                    line.setStroke(color);
                    break;
                }
            }
        });
    }
}

// MatrixGraphUI 类同理，drawVertices 中也用动态半径布局，drawEdges 刷新所有边
// 只需将之前的固定半径改为根据 pane 宽高和节点数计算半径，然后调用 drawEdges() 刷新边位置