package org.example.ui;

import javafx.animation.*;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.example.core.SelectionSort;

public class SelectionSortUI extends ControllableSortUI {

    private int[] originalData;
    private int[][] steps;
    private SequentialTransition animation;

    public SelectionSortUI(int[] data) {
        this.originalData = data.clone();
        this.root = new HBox(SPACING);
        initBars(data);
        
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

    public HBox getRoot() { return root; }

    @Override
    public void visualizeSteps(long stepDelay) {
        if (animation != null) animation.stop();
        
        this.isPlaying = true;
        this.currentStep = 0;
        
        animation = new SequentialTransition();
        
        // 只有在慢速模式下（延迟大于50ms）才启用华丽的交换动画
        boolean enableSwapAnim = stepDelay > 50;

        // 初始状态
        int[] prevArray = originalData.clone();

        for (int i = 0; i < steps.length; i++) {
            final int stepIndex = i;
            final int[] currentArray = steps[i];
            
            // 1. 寻找发生了交换的两个索引
            int swapIdx1 = -1, swapIdx2 = -1;
            if (enableSwapAnim) {
                for (int k = 0; k < currentArray.length; k++) {
                    if (currentArray[k] != prevArray[k]) {
                        if (swapIdx1 == -1) swapIdx1 = k;
                        else if (swapIdx2 == -1) swapIdx2 = k;
                    }
                }
            }
            
            // 更新 prevArray 为当前状态，供下一轮对比
            prevArray = currentArray.clone();

            if (enableSwapAnim && swapIdx1 != -1 && swapIdx2 != -1) {
                // === 慢速模式：创建物理交换动画 ===
                final int u = swapIdx1;
                final int v = swapIdx2;
                
                // 计算物理距离
                double dist = (v - u) * (BAR_WIDTH + SPACING);
                
                // 创建并行动画：两个柱子互换位置
                Timeline swapAnim = new Timeline(
                    new KeyFrame(Duration.ZERO, e -> {
                        // 开始前先高亮
                        bars[u].setFill(Color.RED);
                        bars[v].setFill(Color.PURPLE);
                    }),
                    new KeyFrame(Duration.millis(stepDelay), 
                        new KeyValue(bars[u].translateXProperty(), dist),
                        new KeyValue(bars[v].translateXProperty(), -dist)
                    )
                );
                
                // 动画结束后，逻辑上更新高度并归位
                Timeline cleanup = new Timeline(new KeyFrame(Duration.ONE, e -> {
                    updateBarsInstant(currentArray); // 瞬移更新高度
                    // 归位X轴（因为高度已经交换了）
                    bars[u].setTranslateX(0);
                    bars[v].setTranslateX(0);
                }));
                
                animation.getChildren().addAll(swapAnim, cleanup);
                
            } else {
                // === 竞速模式 或 无交换步骤：直接更新 ===
                Timeline simpleUpdate = new Timeline(new KeyFrame(Duration.millis(stepDelay), e -> {
                    updateBarsInstant(currentArray);
                    currentStep = stepIndex + 1;
                }));
                animation.getChildren().add(simpleUpdate);
            }
        }

        animation.setOnFinished(e -> {
            isPlaying = false;
            currentStep = steps.length;
            for(Rectangle r : bars) r.setFill(Color.LIGHTGREEN); // 完成后变绿
        });
        
        animation.play();
    }

    // 瞬间更新所有柱子高度
    private void updateBarsInstant(int[] arr) {
        for (int k = 0; k < bars.length; k++) {
            double height = arr[k] * SCALE;
            bars[k].setHeight(height);
            bars[k].setTranslateY(BASELINE - height);
            bars[k].setTranslateX(0);
            bars[k].setFill(Color.LIGHTGREEN);
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
        initBars(originalData);
    }

    @Override
    public int getTotalSteps() { return steps.length; }
}