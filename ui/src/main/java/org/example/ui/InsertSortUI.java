package org.example.ui;

import javafx.animation.*;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class InsertSortUI {
    private HBox root;
    private Rectangle[] bars;
    private static final double BAR_WIDTH = 90;
    private static final double SCALE = 50;
    private static final double BASELINE = 600;

    public InsertSortUI(int[] data) {
        root = new HBox(5);
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
        SequentialTransition seq = new SequentialTransition();

        for (int stepIndex = 1; stepIndex < steps.length; stepIndex++) {
            final int[] curr = steps[stepIndex].clone();
            final int index = stepIndex;
            Timeline highlight = new Timeline(
                    new KeyFrame(Duration.ZERO, e -> highlightInsert(index)),
                    new KeyFrame(Duration.millis(500)) // 保持0.3秒
            );
            PauseTransition pause = new PauseTransition(Duration.millis(400));
            Timeline update = new Timeline(
                    new KeyFrame(Duration.millis(delayMs), e -> updateBars(curr))
            );
            seq.getChildren().addAll(highlight, pause, update);
        }

        seq.play();
    }

    private void highlightInsert(int index) {
        for (int i = 0; i < bars.length; i++) {
            if (i == index)
                bars[i].setFill(Color.GOLD); // 当前插入元素金色
            else if (i < index)
                bars[i].setFill(Color.SALMON); // 左侧有序部分红色
            else
                bars[i].setFill(Color.LIGHTGREEN); // 未排序区绿色
        }
    }

    private void updateBars(int[] curr) {
        ParallelTransition pt = new ParallelTransition();

        for (int i = 0; i < curr.length; i++) {
            double newHeight = curr[i] * SCALE;
            Rectangle bar = bars[i];

            Timeline anim = new Timeline(
                    new KeyFrame(Duration.seconds(0.5),
                            new KeyValue(bar.heightProperty(), newHeight, Interpolator.EASE_BOTH),
                            new KeyValue(bar.translateYProperty(), BASELINE - newHeight, Interpolator.EASE_BOTH)
                    )
            );
            pt.getChildren().add(anim);
        }

        pt.setOnFinished(e -> {
            for (Rectangle bar : bars)
                bar.setFill(Color.LIGHTGREEN);
        });

        pt.play();
    }
}