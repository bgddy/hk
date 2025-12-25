package org.example.ui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.example.core.SortFrame;
import org.example.core.SelectionSort;
import java.util.List;

public class SelectionSortUI extends ControllableSortUI {
    private List<SortFrame> steps;
    private final String[] pseudocode = {
        "for i from 0 to n-1",           // 0
        "  min_idx = i",                 // 1
        "  for j from i+1 to n",         // 2
        "    if arr[j] < arr[min_idx]",  // 3
        "      min_idx = j",             // 4
        "  swap(arr[i], arr[min_idx])"   // 5
    };

    public SelectionSortUI(int[] array) {
        SelectionSort sorter = new SelectionSort();
        this.steps = sorter.sort(array);
        
        initBars(array);
        initCodeView(pseudocode);
    }

    private void initBars(int[] array) {
        barsContainer.getChildren().clear();
        bars = new Rectangle[array.length];
        labels = new Text[array.length];
        
        for (int i = 0; i < array.length; i++) {
            double height = array[i] * SCALE;
            Rectangle bar = new Rectangle(BAR_WIDTH, height);
            bar.setFill(Color.LIGHTGREEN);
            bar.setX(i * (BAR_WIDTH + SPACING));
            bar.setY(BASELINE - height);
            bars[i] = bar;

            Text text = new Text(String.valueOf(array[i]));
            text.setX(i * (BAR_WIDTH + SPACING) + BAR_WIDTH / 2 - text.getLayoutBounds().getWidth() / 2);
            text.setY(BASELINE - height - 5);
            labels[i] = text;

            barsContainer.getChildren().addAll(bar, text);
        }
    }

    @Override
    public void nextStep() {
        if (currentStep < steps.size()) {
            SortFrame frame = steps.get(currentStep);
            updateExplanation(frame.getDescription()); 

            int[] state = frame.getArrayState();
            int lineIndex = frame.getLineIndex();
            int i = frame.getI();
            int j = frame.getJ();
            int minIdx = frame.getExtra();

            // [修复] 同步颜色位置
            if (lineIndex == 5) {
                swapPermutation(i, minIdx);
            }

            // 1. 更新高度和标签
            for (int k = 0; k < state.length; k++) {
                double height = state[k] * SCALE;
                bars[k].setHeight(height);
                bars[k].setY(BASELINE - height);
                
                // Update label
                labels[k].setText(String.valueOf(state[k]));
                labels[k].setX(k * (BAR_WIDTH + SPACING) + BAR_WIDTH / 2 - labels[k].getLayoutBounds().getWidth() / 2);
                labels[k].setY(BASELINE - height - 5);
            }
            
            // 2. 更新颜色 (基于 permutation)
            refreshColors(); 
            for (int k = 0; k < state.length; k++) {
                // 如果是稳定模式，refreshColors 已经上好了颜色，这里只在非稳定模式或操作高亮时覆盖
                if (k < i) { // 已排序
                    if (!stabilityMode) bars[k].setFill(Color.GRAY);
                } else if (k == minIdx) { // 当前最小值
                    bars[k].setFill(Color.RED);
                } else if (k == i) { // 起始位置
                    bars[k].setFill(Color.YELLOW);
                } else if (k == j && j != -1) { // 比较中
                    bars[k].setFill(Color.ORANGE);
                } else {
                    // 未排序部分保持 refreshColors 的结果 (在稳定模式下显示红/蓝)
                    if (!stabilityMode && k > i) bars[k].setFill(Color.LIGHTGREEN);
                }
            }

            highlightLine(lineIndex);
            currentStep++;
        } else {
            if (currentStep == steps.size()) {
                // [修复] 稳定模式下，不要把结果涂成绿色，保留颜色以观察顺序
                if (!stabilityMode) {
                    for (Rectangle bar : bars) {
                        bar.setFill(Color.GREEN);
                    }
                }
                highlightLine(-1);
                currentStep++;
            }
        }
    }
    
    @Override
    public void visualizeSteps(long stepDelay) {
        if (animation != null) animation.stop();
        animation = new Timeline(new KeyFrame(Duration.millis(stepDelay), e -> nextStep()));
        animation.setCycleCount(steps.size() - currentStep + 1);
        animation.setOnFinished(e -> isPlaying = false);
        animation.play();
        isPlaying = true;
    }

    @Override
    public void reset() {
        if (animation != null) animation.stop();
        currentStep = 0;
        isPlaying = false;
        
        int[] initialArray = steps.isEmpty() ? new int[0] : steps.get(0).getArrayState();
        initBars(initialArray);
        // [修复] 重置时重新初始化颜色映射
        if (stabilityMode) {
            initPermutation(initialArray);
            refreshColors();
        }
        initCodeView(pseudocode);
        updateExplanation("算法已重置。");
    }

    @Override
    public int getTotalSteps() { return steps.size(); }
}