package org.example.ui;

import javafx.application.Platform;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class SelectionSortUI {

    private HBox root;
    private int[] data;

    public SelectionSortUI(int[] data) {
        this.data = data;
        root = new HBox(5);
        initBars(data);
    }

    private void initBars(int[] data) {
        root.getChildren().clear();
        for (int value : data) {
            Rectangle rect = new Rectangle(20, value * 5, Color.BLUE);
            root.getChildren().add(rect);
        }
    }

    public HBox getRoot() {
        return root;
    }

    // 可视化每一步
    public void visualizeSteps(int[][] steps, long delayMs) {
        new Thread(() -> {
            try {
                for (int[] step : steps) {
                    Platform.runLater(() -> initBars(step));
                    Thread.sleep(delayMs);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}