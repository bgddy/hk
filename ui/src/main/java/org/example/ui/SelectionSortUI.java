package org.example.ui;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.example.core.SelectionSort;

public class SelectionSortUI {

    private HBox root;
    private Rectangle[] bars;
    private static final double BAR_WIDTH = 90;
    private static final double SCALE = 50;
    private static final double BASELINE = 600; // 底部基线高度

    public SelectionSortUI(int[] data) {
        root = new HBox(20);
        initBars(data);
    }

    private void initBars(int[] data) {
        root.getChildren().clear();
        bars = new Rectangle[data.length];
        for (int i = 0; i < data.length; i++) {
            double height = data[i] * SCALE;
            Rectangle rect = new Rectangle(BAR_WIDTH, height, Color.LIGHTGREEN);
            rect.setTranslateY(BASELINE - height);
            bars[i] = rect;
            root.getChildren().add(rect);
        }
    }

    public HBox getRoot() {
        return root;
    }

    public void visualizeSteps(int[][] steps, long delayMs) {
        new Thread(() -> {
            try {
                for (int stepIndex = 0; stepIndex < steps.length; stepIndex++) {
                    final int[] step = steps[stepIndex];
                    final int currentIndex = stepIndex;

                    int minIndex = findMinIndex(step, currentIndex);
                    Platform.runLater(() -> animateStep(currentIndex, minIndex, step));
                    Thread.sleep(delayMs);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private int findMinIndex(int[] array, int start) {
        int minIdx = start;
        for (int i = start + 1; i < array.length; i++) {
            if (array[i] < array[minIdx]) minIdx = i;
        }
        return minIdx;
    }

    private void animateStep(int i, int minIdx, int[] array) {
        for (Rectangle bar : bars) bar.setFill(Color.LIGHTGREEN);

        bars[i].setFill(Color.PURPLE);
        bars[minIdx].setFill(Color.RED);

        if (i != minIdx) {
            Rectangle r1 = bars[i];
            Rectangle r2 = bars[minIdx];
            double distance = (minIdx - i) * (BAR_WIDTH + 5);

            Timeline timeline = new Timeline(
                    new KeyFrame(Duration.seconds(10),
                            new KeyValue(r1.translateXProperty(), distance),
                            new KeyValue(r2.translateXProperty(), -distance)
                    )
            );

            timeline.setOnFinished(e -> {
                bars[i] = r2;
                bars[minIdx] = r1;
                r1.setTranslateX(0);
                r2.setTranslateX(0);
                for (int k = 0; k < array.length; k++) {
                    double height = array[k] * SCALE;
                    bars[k].setHeight(height);
                    bars[k].setTranslateY(BASELINE - height);
                }
            });
            timeline.play();
        } else {
            for (int k = 0; k < array.length; k++) {
                double height = array[k] * SCALE;
                bars[k].setHeight(height);
                bars[k].setTranslateY(BASELINE - height);
            }
        }
    }
}