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

public class SelectionSortUI extends ControllableSortUI {

    private int[] originalData;
    private int[][] steps;
    private Timeline currentAnimation;
    private long stepDelay = 500;

    public SelectionSortUI(int[] data) {
        this.originalData = data.clone();
        this.root = new HBox(SPACING);
        initBars(data);
        
        // 生成排序步骤
        SelectionSort sorter = new SelectionSort();
        this.steps = sorter.sort(data);
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

    @Override
    public void visualizeSteps(long stepDelay) {
        this.stepDelay = stepDelay;
        isPlaying = true;
        currentStep = 0;
        
        new Thread(() -> {
            try {
                while (currentStep < steps.length && isPlaying) {
                    final int stepIndex = currentStep;
                    final int[] step = steps[stepIndex];
                    int minIndex = findMinIndex(step, stepIndex);
                    
                    Platform.runLater(() -> animateStep(stepIndex, minIndex, step));
                    Thread.sleep(stepDelay);
                    
                    currentStep++;
                }
                isPlaying = false;
            } catch (InterruptedException e) {
                e.printStackTrace();
                isPlaying = false;
            }
        }).start();
    }

    @Override
    public void nextStep() {
        if (currentStep < steps.length) {
            int[] step = steps[currentStep];
            int minIndex = findMinIndex(step, currentStep);
            animateStep(currentStep, minIndex, step);
            currentStep++;
        }
    }

    @Override
    public void reset() {
        // 停止当前动画
        if (currentAnimation != null) {
            currentAnimation.stop();
        }
        isPlaying = false;
        currentStep = 0;
        
        // 重置到初始状态
        initBars(originalData);
    }

    @Override
    public int getTotalSteps() {
        return steps.length;
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
            double distance = (minIdx - i) * (BAR_WIDTH + SPACING);

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
