package org.example.ui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.layout.Pane;
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
        "for i from 0 to n-1",
        "  min_idx = i",
        "  for j from i+1 to n",
        "    if arr[j] < arr[min_idx]",
        "      min_idx = j",
        "  swap(arr[i], arr[min_idx])"
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
            int i = frame.getI();
            int j = frame.getJ();
            int minIdx = frame.getExtra();

            // 1. 更新柱状图高度和位置
            for (int k = 0; k < state.length; k++) {
                double height = state[k] * SCALE;
                bars[k].setHeight(height);
                bars[k].setY(BASELINE - height);
            }
            
            // 2. 更新颜色
            refreshColors(); 
            for (int k = 0; k < state.length; k++) {
                if (k < i) { // 已排序部分
                    bars[k].setFill(Color.GRAY);
                } else if (k == minIdx) { // 当前最小值
                    bars[k].setFill(Color.RED);
                } else if (k == i) { // 当前起始位置
                    bars[k].setFill(Color.YELLOW);
                } else if (k == j && j != -1) { // 当前比较元素
                    bars[k].setFill(Color.ORANGE);
                } else if (k > i) { // 未排序部分
                    bars[k].setFill(Color.LIGHTGREEN);
                }
            }

            // 3. 高亮代码
            highlightLine(lineIndex);

            currentStep++;
        } else {
            // 排序完成
            if (currentStep == steps.size()) {
                // 最终帧，将所有颜色设为已排序颜色
                for (Rectangle bar : bars) {
                    bar.setFill(Color.GREEN);
                }
                highlightLine(-1);
                currentStep++;
            }
        }
    }
    
    // ... (其他方法如 visualizeSteps, reset, getTotalSteps 保持不变) ...
    @Override
    public void visualizeSteps(long stepDelay) {
        if (animation != null) {
            animation.stop();
        }

        animation = new Timeline(new KeyFrame(Duration.millis(stepDelay), e -> nextStep()));
        animation.setCycleCount(steps.size() - currentStep + 1);
        animation.setOnFinished(e -> isPlaying = false);
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
        
        // 初始化数组和 UI
        int[] initialArray = new int[0]; // 应该从某个地方获取初始数据，这里假设调用者知道
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