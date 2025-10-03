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
    private static final int BAR_WIDTH = 40;

    public InsertSortUI(int[] data) {
        root = new HBox(5);
        bars = new Rectangle[data.length];
        for (int i = 0; i < data.length; i++) {
            Rectangle rect = new Rectangle(BAR_WIDTH, data[i] * 10, Color.BLUE);
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
            final int[] prev = steps[stepIndex - 1].clone(); // 上一步
            final int[] curr = steps[stepIndex].clone();     // 当前步

            Timeline t = new Timeline(new KeyFrame(Duration.millis(delayMs), e -> animateInsert(prev, curr)));
            seq.getChildren().add(t);
        }

        seq.play();
    }

    private void animateInsert(int[] prev, int[] curr) {
        int n = prev.length;

        // 找出被插入的元素位置
        int movedIndex = -1;
        for (int i = 0; i < n; i++) {
            if (prev[i] != curr[i]) {
                movedIndex = i;
                break;
            }
        }

        // 默认全蓝
        for (Rectangle bar : bars) bar.setFill(Color.BLUE);

        // 更新每个柱子的高度
        for (int i = 0; i < n; i++) {
            bars[i].setHeight(curr[i] * 10);
        }

        // 如果找到了被移动的元素，标红
        if (movedIndex != -1) {
            bars[movedIndex].setFill(Color.RED);
        }
    }
}