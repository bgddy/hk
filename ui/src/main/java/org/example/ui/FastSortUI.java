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
import org.example.core.FastSort;

public class FastSortUI extends ControllableSortUI {

    private int[] originalData;
    private QuickSortStep[] steps;
    private SequentialTransition animation;
    private long stepDelay = 500;

    public FastSortUI(int[] initialData) {
        this.originalData = initialData.clone();
        this.root = new HBox(SPACING);
        this.bars = new Rectangle[initialData.length];
        
        for (int i = 0; i < initialData.length; i++) {
            double height = initialData[i] * SCALE;
            Rectangle rect = new Rectangle(BAR_WIDTH, height, Color.LIGHTBLUE);
            rect.setTranslateY(BASELINE - height);
            bars[i] = rect;
            root.getChildren().add(rect);
        }
        
        // 生成排序步骤
        FastSort sorter = new FastSort();
        this.steps = sorter.sort(initialData);
    }

    public HBox getRoot() {
        return root;
    }

    @Override
    public void visualizeSteps(long stepDelay) {
        this.stepDelay = stepDelay;
        isPlaying = true;
        currentStep = 0;
        
        // 创建新的动画序列
        animation = new SequentialTransition();
        
        for (int stepIndex = 0; stepIndex < steps.length; stepIndex++) {
            final int stepIndexFinal = stepIndex;
            Timeline t = new Timeline(new KeyFrame(Duration.millis(this.stepDelay), e -> {
                updateBars(steps[stepIndexFinal]);
                currentStep = stepIndexFinal + 1;
            }));
            animation.getChildren().add(t);
        }

        animation.setOnFinished(e -> {
            isPlaying = false;
            currentStep = steps.length;
        });
        
        animation.play();
    }

    @Override
    public void nextStep() {
        if (currentStep < steps.length) {
            updateBars(steps[currentStep]);
            currentStep++;
        }
    }

    @Override
    public void reset() {
        // 停止当前动画
        if (animation != null) {
            animation.stop();
        }
        isPlaying = false;
        currentStep = 0;
        
        // 重置到初始状态
        for (int i = 0; i < originalData.length; i++) {
            double height = originalData[i] * SCALE;
            bars[i].setHeight(height);
            bars[i].setTranslateY(BASELINE - height);
            bars[i].setFill(Color.LIGHTBLUE);
            bars[i].setTranslateX(0);
        }
    }

    @Override
    public int getTotalSteps() {
        return steps.length;
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

            double distance = (j - i) * (BAR_WIDTH + SPACING);
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
