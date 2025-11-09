package org.example.ui;

import javafx.animation.*;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.example.core.InsertSort;

public class InsertSortUI extends ControllableSortUI {

    private int[] originalData;
    private int[][] steps;
    private SequentialTransition animation;
    private long stepDelay = 500;

    public InsertSortUI(int[] data) {
        this.originalData = data.clone();
        this.root = new HBox(SPACING);
        this.bars = new Rectangle[data.length];
        
        // 初始化柱状图
        for (int i = 0; i < data.length; i++) {
            double height = data[i] * SCALE;
            Rectangle rect = new Rectangle(BAR_WIDTH, height, Color.LIGHTGREEN);
            rect.setTranslateY(BASELINE - height);
            bars[i] = rect;
            root.getChildren().add(rect);
        }
        
        // 生成排序步骤
        InsertSort sorter = new InsertSort();
        this.steps = sorter.sort(data);
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
        
        for (int stepIndex = 1; stepIndex < steps.length; stepIndex++) {
            final int stepIndexFinal = stepIndex;
            final int[] curr = steps[stepIndex].clone();
            
            Timeline highlight = new Timeline(
                    new KeyFrame(Duration.ZERO, e -> {
                        highlightInsert(stepIndexFinal);
                        currentStep = stepIndexFinal;
                    }),
                    new KeyFrame(Duration.millis(500))
            );
            PauseTransition pause = new PauseTransition(Duration.millis(stepDelay));
            Timeline update = new Timeline(
                    new KeyFrame(Duration.millis(stepDelay), e -> updateBars(curr))
            );
            animation.getChildren().addAll(highlight, pause, update);
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
            if (currentStep == 0) {
                // 第一步特殊处理
                highlightInsert(1);
                updateBars(steps[1]);
            } else if (currentStep < steps.length - 1) {
                highlightInsert(currentStep + 1);
                updateBars(steps[currentStep + 1]);
            }
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
            bars[i].setFill(Color.LIGHTGREEN);
        }
    }

    @Override
    public int getTotalSteps() {
        return steps.length;
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
