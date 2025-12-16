package org.example.ui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.example.core.FastSort;
import org.example.core.SortFrame;
import java.util.List;

public class FastSortUI extends ControllableSortUI {
    private List<SortFrame> steps;
    private final String[] pseudocode = {
        "function partition(arr, low, high)", // 0
        "  pivot = arr[high]",               // 1
        "  i = low - 1",                     // 2
        "  for j from low to high - 1",      // 3
        "    if arr[j] <= pivot",            // 4
        "      i = i + 1",                   // 5
        "      swap(arr[i], arr[j])",        // 6
        "  swap(arr[i + 1], arr[high])",     // 7
        "  return i + 1",                    // 8
        "",                                  // 9
        "function quick_sort(arr, low, high)",// 10
        "  if low < high",                   // 11
        "    pi = partition(arr, low, high)",// 12
        "    quick_sort(arr, low, pi - 1)",  // 13
        "    quick_sort(arr, pi + 1, high)"  // 14
    };

    public FastSortUI(int[] array) {
        FastSort sorter = new FastSort();
        this.steps = sorter.sort(array);

        initBars(array);
        initCodeView(pseudocode);
    }

    private void initBars(int[] array) {
        barsContainer.getChildren().clear();
        bars = new Rectangle[array.length];
        
        for (int i = 0; i < array.length; i++) {
            double height = array[i] * SCALE;
            Rectangle bar = new Rectangle(BAR_WIDTH, height);
            bar.setFill(Color.LIGHTGREEN);
            bar.setX(i * (BAR_WIDTH + SPACING));
            bar.setY(BASELINE - height);
            bars[i] = bar;

            Text text = new Text(String.valueOf(array[i]));
            text.setX(i * (BAR_WIDTH + SPACING) + BAR_WIDTH / 2 - text.getLayoutBounds().getWidth() / 2);
            text.setY(BASELINE + 15);

            barsContainer.getChildren().addAll(bar, text);
        }
    }

    @Override
    public void nextStep() {
        if (currentStep < steps.size()) {
            SortFrame frame = steps.get(currentStep);
            
            // [新增] 更新算法解释文本
            updateExplanation(frame.getDescription()); 

            int[] state = frame.getArrayState();
            int lineIndex = frame.getLineIndex();
            int i = frame.getI();     // 小于 Pivot 区域的边界 + 1 (在分区结束后是 Pivot 的最终位置)
            int j = frame.getJ();     // 扫描指针 j (或 Pivot 的原始位置)
            int pivotValue = frame.getExtra(); // Pivot 值
            int low = frame.getL();   // 子数组左边界
            int high = frame.getR();  // 子数组右边界

            // 1. 更新柱状图高度和位置
            for (int k = 0; k < state.length; k++) {
                double height = state[k] * SCALE;
                bars[k].setHeight(height);
                bars[k].setY(BASELINE - height);
            }
            
            // 2. 更新颜色
            refreshColors(); 
            for (int k = 0; k < state.length; k++) {
                if (k < low || k > high) { // 已排序/非当前子数组部分
                    bars[k].setFill(Color.GRAY);
                } else if (k == j) { // Pivot 的原始位置（在分区过程中是 high）
                    bars[k].setFill(Color.PURPLE);
                } else if (k == i && i != -1 && lineIndex < 7) { // 小于区间的边界
                    bars[k].setFill(Color.RED);
                } else if (k > i && k < j) { // 大于区间
                    bars[k].setFill(Color.ORANGE);
                } else if (k == i && lineIndex >= 7) { // Pivot 最终位置
                    bars[k].setFill(Color.GREEN);
                } else {
                    bars[k].setFill(Color.LIGHTGREEN);
                }
            }

            // 3. 高亮代码
            highlightLine(lineIndex);

            currentStep++;
        } else {
            if (currentStep == steps.size()) {
                for (Rectangle bar : bars) {
                    bar.setFill(Color.GREEN);
                }
                highlightLine(-1);
                currentStep++;
            }
        }
    }
    
    @Override
    public void visualizeSteps(long stepDelay) {
        if (animation != null) {
            animation.stop();
        }

        animation = new Timeline(new KeyFrame(Duration.millis(stepDelay), e -> nextStep()));
        animation.setCycleCount(steps.size() - currentStep + 1);
        animation.play();
        isPlaying = true;
    }

    @Override
    public void reset() {
        if (animation != null) {
            animation.stop();
        }
        currentStep = 0;
        isPlaying = false;
        
        int[] initialArray = new int[0];
        if (steps != null && !steps.isEmpty()) {
            initialArray = steps.get(0).getArrayState();
        }
        initBars(initialArray);
        initCodeView(pseudocode);
        
        // [新增] 重置解释文本
        updateExplanation("算法已重置。请点击'下一步'或'播放'开始演示。");
    }

    @Override
    public int getTotalSteps() {
        return steps.size();
    }
}