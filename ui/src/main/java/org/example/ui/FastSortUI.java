package org.example.ui;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.example.core.FastSort;
import org.example.core.QuickSortStep;

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
            // 关键修改：将每一帧的 KeyFrame 持续时间严格设为 stepDelay
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
            // 单步调试时，强制使用无动画模式或短动画，避免卡顿
            updateBars(steps[currentStep]);
            currentStep++;
        }
    }

    @Override
    public void reset() {
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
            bars[i].setTranslateX(0); // 确保X轴偏移归零
        }
    }

    @Override
    public int getTotalSteps() {
        return steps.length;
    }

    private void updateBars(QuickSortStep step) {
        // 1. 重置所有柱子颜色
        for (Rectangle bar : bars) bar.setFill(Color.LIGHTBLUE);

        // 2. 高亮关键元素
        if (step.pivotIndex >= 0 && step.pivotIndex < bars.length) 
            bars[step.pivotIndex].setFill(Color.PURPLE);
        
        if (step.leftBound >= 0 && step.leftBound < bars.length) 
            bars[step.leftBound].setFill(Color.GREEN);
        
        if (step.rightBound >= 0 && step.rightBound < bars.length) 
            bars[step.rightBound].setFill(Color.ORANGE);

        // 3. 核心逻辑修复：根据速度决定是否播放位移动画
        boolean enableAnimation = this.stepDelay >= 200; // 只有慢速模式才启用位移

        // 先更新除了交换涉及的柱子以外的所有柱子高度（防止数据不同步）
        for (int i = 0; i < step.arrayState.length; i++) {
            // 如果正在进行交换动画，跳过这两个柱子的高度直接设置，交给动画处理
            if (enableAnimation && (i == step.swapIndex1 || i == step.swapIndex2)) continue;
            
            double height = step.arrayState[i] * SCALE;
            bars[i].setHeight(height);
            bars[i].setTranslateY(BASELINE - height);
            bars[i].setTranslateX(0); // 极其重要：防止之前的动画偏移残留
        }

        // 4. 处理交换
        if (step.swapIndex1 >= 0 && step.swapIndex2 >= 0 && step.swapIndex1 != step.swapIndex2) {
            int i = step.swapIndex1;
            int j = step.swapIndex2;
            
            bars[i].setFill(Color.RED);
            bars[j].setFill(Color.RED);

            if (enableAnimation) {
                // --- 慢速模式：漂亮的位伊动画 ---
                // 使用当前高度，而不是 step.arrayState，因为动画还没完成交换
                double distance = (j - i) * (BAR_WIDTH + SPACING);
                
                // 动画时长必须小于 stepDelay，留一点缓冲
                double animDuration = Math.min(400, this.stepDelay * 0.9);

                Timeline move = new Timeline(
                        new KeyFrame(Duration.millis(animDuration),
                                new KeyValue(bars[i].translateXProperty(), distance),
                                new KeyValue(bars[j].translateXProperty(), -distance)
                        )
                );

                move.setOnFinished(e -> {
                    // 动画结束后，物理交换高度并归位
                    double h1 = step.arrayState[i] * SCALE;
                    double h2 = step.arrayState[j] * SCALE;
                    
                    bars[i].setHeight(h1); bars[i].setTranslateY(BASELINE - h1);
                    bars[j].setHeight(h2); bars[j].setTranslateY(BASELINE - h2);
                    
                    bars[i].setTranslateX(0);
                    bars[j].setTranslateX(0);
                });
                move.play();
            } else {
                // --- 竞速模式：直接瞬移 ---
                // 直接设置最终高度，没有任何 timeline 开销
                double h1 = step.arrayState[i] * SCALE;
                double h2 = step.arrayState[j] * SCALE;
                
                bars[i].setHeight(h1); bars[i].setTranslateY(BASELINE - h1);
                bars[j].setHeight(h2); bars[j].setTranslateY(BASELINE - h2);
                bars[i].setTranslateX(0);
                bars[j].setTranslateX(0);
            }
        }
    }
}