package org.example.ui;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.example.core.QuickSortStep;

public class FastSortUI {
    private HBox root;
    private Rectangle[] bars;
    private static final int BAR_WIDTH = 40;

    public FastSortUI(int[] initialData) {
        root = new HBox(4);
        bars = new Rectangle[initialData.length];
        for (int i = 0; i < initialData.length; i++) {
            Rectangle rect = new Rectangle(BAR_WIDTH, Math.max(initialData[i] * 10, 20), Color.BLUE);
            bars[i] = rect;
            root.getChildren().add(rect);
        }
    }

    public HBox getRoot() {
        return root;
    }

    public void visualizeSteps(QuickSortStep[] steps, long stepDelay) {
        SequentialTransition seq = new SequentialTransition();

        for (QuickSortStep step : steps) {
            Timeline t = new Timeline(new KeyFrame(Duration.millis(stepDelay), e -> updateBars(step)));
            seq.getChildren().add(t);
        }

        seq.play();
    }

    private void updateBars(QuickSortStep step) {
        // 重置颜色
        for (int i = 0; i < bars.length; i++) bars[i].setFill(Color.BLUE);

        // 高亮 pivot
        if (step.pivotIndex >= 0) bars[step.pivotIndex].setFill(Color.PURPLE);
        // 高亮左右边界
        if (step.leftBound >= 0) bars[step.leftBound].setFill(Color.GREEN);
        if (step.rightBound >= 0) bars[step.rightBound].setFill(Color.ORANGE);

        // 交换动画
        if (step.swapIndex1 >= 0 && step.swapIndex2 >= 0 && step.swapIndex1 != step.swapIndex2) {
            int i = step.swapIndex1;
            int j = step.swapIndex2;
            bars[i].setFill(Color.RED);
            bars[j].setFill(Color.RED);

            double x1 = bars[i].getTranslateX();
            double x2 = bars[j].getTranslateX();
            double distance = (j - i) * (BAR_WIDTH + 4); // 横向距离

            Timeline move = new Timeline(
                    new KeyFrame(Duration.millis(400),
                            new KeyValue(bars[i].translateXProperty(), distance),
                            new KeyValue(bars[j].translateXProperty(), -distance)
                    )
            );

            move.setOnFinished(e -> {
                // 交换高度
                double h = bars[i].getHeight();
                bars[i].setHeight(bars[j].getHeight());
                bars[j].setHeight(h);

                // 重置平移
                bars[i].setTranslateX(0);
                bars[j].setTranslateX(0);
            });

            move.play();
        }
    }
}