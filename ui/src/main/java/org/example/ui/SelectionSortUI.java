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

    public SelectionSortUI(int[] data) {
        root = new HBox(5);
        initBars(data);
    }

    private void initBars(int[] data) {
        root.getChildren().clear();
        bars = new Rectangle[data.length];
        for (int i = 0; i < data.length; i++) {
            Rectangle rect = new Rectangle(20, data[i] * 5, Color.BLUE);
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

                    // 高亮当前轮起始位置和最小值
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
        // 默认全部蓝色
        for (Rectangle bar : bars) bar.setFill(Color.BLUE);

        // 高亮当前轮和最小值
        bars[i].setFill(Color.GREEN);
        bars[minIdx].setFill(Color.RED);

        if (i != minIdx) {
            Rectangle r1 = bars[i];
            Rectangle r2 = bars[minIdx];

            double distance = r2.getLayoutX() - r1.getLayoutX();

            Timeline timeline = new Timeline(
                    new KeyFrame(Duration.millis(800),
                            new KeyValue(r1.translateXProperty(), distance),
                            new KeyValue(r2.translateXProperty(), -distance)
                    )
            );

            timeline.setOnFinished(e -> {
                // 交换 bars 数组顺序
                bars[i] = r2;
                bars[minIdx] = r1;

                // 重置平移
                r1.setTranslateX(0);
                r2.setTranslateX(0);

                // 更新条形高度到数组当前值
                for (int k = 0; k < array.length; k++) {
                    bars[k].setHeight(array[k] * 5);
                }
            });

            timeline.play();
        } else {
            // 如果当前轮最小值就是当前œ位置，也更新高度
            for (int k = 0; k < array.length; k++) {
                bars[k].setHeight(array[k] * 5);
            }
        }
    }
}