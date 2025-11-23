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

    public InsertSortUI(int[] data) {
        this.originalData = data.clone();
        this.root = new HBox(SPACING);
        this.bars = new Rectangle[data.length];
        
        for (int i = 0; i < data.length; i++) {
            double height = data[i] * SCALE;
            Rectangle rect = new Rectangle(BAR_WIDTH, height, Color.LIGHTGREEN);
            rect.setTranslateY(BASELINE - height);
            bars[i] = rect;
            root.getChildren().add(rect);
        }
        
        InsertSort sorter = new InsertSort();
        this.steps = sorter.sort(data);
    }

    public HBox getRoot() { return root; }

    @Override
    public void visualizeSteps(long stepDelay) {
        if (animation != null) animation.stop();
        
        this.isPlaying = true;
        this.currentStep = 0;
        
        animation = new SequentialTransition();
        boolean enableSmoothAnim = stepDelay > 50;

        int[] prevArray = originalData.clone();

        for (int i = 0; i < steps.length; i++) {
            final int stepIndex = i;
            final int[] curr = steps[i];
            final int[] prev = prevArray; // 捕获上一步状态
            
            if (enableSmoothAnim) {
                // === 慢速模式：平滑变形动画 ===
                ParallelTransition morphing = new ParallelTransition();
                
                for (int k = 0; k < curr.length; k++) {
                    double newHeight = curr[k] * SCALE;
                    Rectangle bar = bars[k];
                    
                    // 只有发生变化的柱子才会有颜色和高度动画
                    if (curr[k] != prev[k]) {
                        // 正在移动的元素标为橙色
                        Timeline t = new Timeline(
                            new KeyFrame(Duration.ZERO, e -> bar.setFill(Color.ORANGE)),
                            new KeyFrame(Duration.millis(stepDelay), 
                                new KeyValue(bar.heightProperty(), newHeight, Interpolator.EASE_BOTH),
                                new KeyValue(bar.translateYProperty(), BASELINE - newHeight, Interpolator.EASE_BOTH)
                            )
                        );
                        morphing.getChildren().add(t);
                    } else {
                        // 未变化的保持绿色（或恢复绿色）
                        Timeline t = new Timeline(new KeyFrame(Duration.ZERO, e -> bar.setFill(Color.LIGHTGREEN)));
                        morphing.getChildren().add(t);
                    }
                }
                // 记录当前步数
                morphing.setOnFinished(e -> currentStep = stepIndex + 1);
                animation.getChildren().add(morphing);
                
            } else {
                // === 竞速模式：瞬移 ===
                Timeline quickUpdate = new Timeline(new KeyFrame(Duration.millis(stepDelay), e -> {
                    updateBarsInstant(curr);
                    currentStep = stepIndex + 1;
                }));
                animation.getChildren().add(quickUpdate);
            }
            
            prevArray = curr; // 更新上一步状态
        }

        animation.setOnFinished(e -> {
            isPlaying = false;
            currentStep = steps.length;
            for(Rectangle r : bars) r.setFill(Color.LIGHTGREEN);
        });
        
        animation.play();
    }

    private void updateBarsInstant(int[] curr) {
        for (int i = 0; i < curr.length; i++) {
            double newHeight = curr[i] * SCALE;
            bars[i].setHeight(newHeight);
            bars[i].setTranslateY(BASELINE - newHeight);
            bars[i].setFill(Color.LIGHTGREEN);
        }
    }

    @Override
    public void nextStep() {
        if (currentStep < steps.length) {
            updateBarsInstant(steps[currentStep]);
            currentStep++;
        }
    }

    @Override
    public void reset() {
        if (animation != null) animation.stop();
        isPlaying = false;
        currentStep = 0;
        
        for (int i = 0; i < originalData.length; i++) {
            double height = originalData[i] * SCALE;
            bars[i].setHeight(height);
            bars[i].setTranslateY(BASELINE - height);
            bars[i].setFill(Color.LIGHTGREEN);
        }
    }

    @Override
    public int getTotalSteps() { return steps.length; }
}