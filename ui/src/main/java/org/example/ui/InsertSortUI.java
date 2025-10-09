package org.example.ui;

import javafx.animation.KeyFrame;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class InsertSortUI {
    private HBox root;
    private Rectangle[] bars;
    private static final double BAR_WIDTH = 40;
    private static final double SCALE = 10;
    private static final double BASELINE = 300;

    public InsertSortUI(int[] data) {
        root = new HBox(5);
        bars = new Rectangle[data.length];
        for (int i = 0; i < data.length; i++) {
            double height = data[i] * SCALE;
            Rectangle rect = new Rectangle(BAR_WIDTH, height, Color.LIGHTBLUE);
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
            Timeline t = new Timeline(new KeyFrame(Duration.millis(delayMs), e -> updateBars(curr)));
            seq.getChildren().add(t);
        }

        seq.play();
    }

    private void updateBars(int[] curr) {
        for (Rectangle bar : bars) bar.setFill(Color.LIGHTBLUE);
        for (int i = 0; i < curr.length; i++) {
            double height = curr[i] * SCALE;
            bars[i].setHeight(height);
            bars[i].setTranslateY(BASELINE - height);
        }
    }
}