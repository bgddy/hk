package org.example.ui;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import org.example.core.AdjListGraph;
import org.example.core.Edge;

import java.util.HashMap;
import java.util.Map;

public class AdjListGraphUI {
    private Pane root;
    private AdjListGraph graph;

    private Map<Integer, Circle> vertexNodes = new HashMap<>();
    private Map<Integer, Label> vertexLabels = new HashMap<>();

    public AdjListGraphUI(AdjListGraph graph) {
        this.graph = graph;
        root = new Pane();
        root.setPrefSize(700, 700);
    }

    public Pane getRoot() {
        return root;
    }

    // 布局顶点（圆形）
    public void layoutVertices() {
        root.getChildren().removeIf(node -> node instanceof Circle || node instanceof Label);

        int n = graph.vecticesNumber();
        double centerX = 350;
        double centerY = 350;
        double radius = 250;

        vertexNodes.clear();
        vertexLabels.clear();

        for (int i = 0; i < n; i++) {
            double angle = 2 * Math.PI * i / n;
            double x = centerX + radius * Math.cos(angle);
            double y = centerY + radius * Math.sin(angle);

            Circle circle = new Circle(x, y, 20, Color.LIGHTBLUE);
            Label label = new Label(String.valueOf(i));
            label.setLayoutX(x - 6);
            label.setLayoutY(y - 10);

            vertexNodes.put(i, circle);
            vertexLabels.put(i, label);

            root.getChildren().addAll(circle, label);
        }
    }

    // 绘制边 + 权重 + 自环
    public void drawEdges() {
        root.getChildren().removeIf(node -> node instanceof Line || node instanceof Polygon || node instanceof Label || node instanceof Arc);

        int n = graph.vecticesNumber();
        for (int from = 0; from < n; from++) {
            Edge edge = graph.firstEdge(from);
            while (edge.getMto() != 0) {
                int to = edge.getMto();
                Circle start = vertexNodes.get(edge.getMfrom());
                Circle end = vertexNodes.get(edge.getMto());
                if (start != null && end != null) {
                    if (from == to) { // 自环
                        Arc arc = new Arc(start.getCenterX(), start.getCenterY() - 25, 20, 20, 0, 270);
                        arc.setType(ArcType.OPEN);
                        arc.setStroke(Color.GRAY);
                        arc.setFill(Color.TRANSPARENT);
                        arc.setStrokeWidth(2);
                        root.getChildren().add(arc);

                        Label weightLabel = new Label(String.valueOf(edge.getMweight()));
                        weightLabel.setLayoutX(start.getCenterX() + 20);
                        weightLabel.setLayoutY(start.getCenterY() - 50);
                        root.getChildren().add(weightLabel);

                    } else { // 普通边
                        Line line = new Line(start.getCenterX(), start.getCenterY(),
                                end.getCenterX(), end.getCenterY());
                        line.setStroke(Color.GRAY);
                        line.setStrokeWidth(2);
                        root.getChildren().add(0, line);

                        // 箭头
                        double ex = end.getCenterX();
                        double ey = end.getCenterY();
                        double sx = start.getCenterX();
                        double sy = start.getCenterY();
                        double dx = ex - sx;
                        double dy = ey - sy;
                        double len = Math.sqrt(dx*dx + dy*dy);
                        double arrowSize = 10;
                        double ux = dx / len;
                        double uy = dy / len;
                        double px = ex - ux * 20;
                        double py = ey - uy * 20;
                        Polygon arrow = new Polygon();
                        arrow.getPoints().addAll(
                                px, py,
                                px - uy * arrowSize - ux * arrowSize, py + ux * arrowSize - uy * arrowSize,
                                px + uy * arrowSize - ux * arrowSize, py - ux * arrowSize - uy * arrowSize
                        );
                        arrow.setFill(Color.GRAY);
                        root.getChildren().add(arrow);

                        // 边权重
                        Label weightLabel = new Label(String.valueOf(edge.getMweight()));
                        weightLabel.setLayoutX((sx+ex)/2);
                        weightLabel.setLayoutY((sy+ey)/2 - 10);
                        root.getChildren().add(weightLabel);
                    }
                }

                edge = graph.nextEdge(edge);
            }
        }
    }

    public void refresh() {
        Platform.runLater(() -> {
            layoutVertices();
            drawEdges();
        });
    }
}