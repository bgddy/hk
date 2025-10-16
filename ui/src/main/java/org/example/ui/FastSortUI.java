package org.example.ui;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.example.core.QuickSortStep;

public class FastSortUI {
    private HBox root;
    private Rectangle[] bars;
    private static final double BAR_WIDTH = 90;
    private static final double SCALE = 50;
    private static final double BASELINE = 600;

    public FastSortUI(int[] initialData) {
        root = new HBox(4);
        bars = new Rectangle[initialData.length];
        for (int i = 0; i < initialData.length; i++) {
            double height = initialData[i] * SCALE;
            Rectangle rect = new Rectangle(BAR_WIDTH, height, Color.LIGHTBLUE);
            rect.setTranslateY(BASELINE - height);
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
        for (Rectangle bar : bars) bar.setFill(Color.LIGHTBLUE);

        if (step.pivotIndex >= 0) bars[step.pivotIndex].setFill(Color.PURPLE);
        if (step.leftBound >= 0) bars[step.leftBound].setFill(Color.GREEN);
        if (step.rightBound >= 0) bars[step.rightBound].setFill(Color.ORANGE);

        for (int i = 0; i < step.arrayState.length; i++) {
            double height = step.arrayState[i] * SCALE;
            bars[i].setHeight(height);
            bars[i].setTranslateY(BASELINE - height);
        }

        if (step.swapIndex1 >= 0 && step.swapIndex2 >= 0 && step.swapIndex1 != step.swapIndex2) {
            int i = step.swapIndex1;
            int j = step.swapIndex2;
            bars[i].setFill(Color.RED);
            bars[j].setFill(Color.RED);

            double distance = (j - i) * (BAR_WIDTH + 4);
            Timeline move = new Timeline(
                    new KeyFrame(Duration.millis(400),
                            new KeyValue(bars[i].translateXProperty(), distance),
                            new KeyValue(bars[j].translateXProperty(), -distance)
                    )
            );

            move.setOnFinished(e -> {
                double tempH = bars[i].getHeight();
                bars[i].setHeight(bars[j].getHeight());
                bars[j].setHeight(tempH);
                bars[i].setTranslateX(0);
                bars[j].setTranslateX(0);
                bars[i].setTranslateY(BASELINE - bars[i].getHeight());
                bars[j].setTranslateY(BASELINE - bars[j].getHeight());
            });

            move.play();
        }
    }
}