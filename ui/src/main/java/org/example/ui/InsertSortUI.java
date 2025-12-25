package org.example.ui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.example.core.InsertSort;
import org.example.core.SortFrame;
import java.util.List;

public class InsertSortUI extends ControllableSortUI {
    private List<SortFrame> steps;
    private int tempKeyOriginalIndex = -1; // 暂存 Key 的原始索引
    
    private final String[] pseudocode = {
        "for i from 1 to n-1",             // 0
        "  key = arr[i]",                  // 1
        "  j = i - 1",                     // 2
        "  while j >= 0 and arr[j] > key", // 3
        "    arr[j + 1] = arr[j]",         // 4
        "    j = j - 1",                   // 5
        "  arr[j + 1] = key"               // 6
    };

    public InsertSortUI(int[] array) {
        InsertSort sorter = new InsertSort();
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
            // 注意：SortFrame 中的 j 对应的是代码中的 j+1 (数组操作位置)
            int targetPos = frame.getJ(); 

            // [修复] 插入排序的 permutation 跟踪逻辑
            if (permutation != null) {
                if (lineIndex == 1) { 
                    // key = arr[i]，记录 key 的原始身份
                    tempKeyOriginalIndex = permutation[i];
                } else if (lineIndex == 4) { 
                    // arr[j + 1] = arr[j]
                    // frame.getJ() 是 j+1，所以是将 targetPos-1 处的值移到 targetPos
                    if (targetPos > 0 && targetPos < permutation.length) {
                        permutation[targetPos] = permutation[targetPos - 1];
                    }
                } else if (lineIndex == 6) { 
                    // arr[j + 1] = key
                    // 将 key 放入目标位置
                    if (targetPos >= 0 && targetPos < permutation.length) {
                        permutation[targetPos] = tempKeyOriginalIndex;
                    }
                }
            }

            // 更新 UI
            for (int k = 0; k < state.length; k++) {
                double height = state[k] * SCALE;
                bars[k].setHeight(height);
                bars[k].setY(BASELINE - height);

                // Update label
                labels[k].setText(String.valueOf(state[k]));
                labels[k].setX(k * (BAR_WIDTH + SPACING) + BAR_WIDTH / 2 - labels[k].getLayoutBounds().getWidth() / 2);
                labels[k].setY(BASELINE - height - 5);
            }
            
            refreshColors();
            for (int k = 0; k < state.length; k++) {
                if (k < i) { // 已排序
                    if (!stabilityMode) bars[k].setFill(Color.GRAY);
                } 
                if (k == i) bars[k].setFill(Color.YELLOW); // 边界
                if (k == targetPos - 1 && lineIndex == 3) bars[k].setFill(Color.ORANGE); // 比较对象
                if (k == targetPos && lineIndex == 6) bars[k].setFill(Color.RED); // 插入位置
            }
            
            highlightLine(lineIndex);
            currentStep++;
        } else {
            if (currentStep == steps.size()) {
                if (!stabilityMode) {
                    for (Rectangle bar : bars) bar.setFill(Color.GREEN);
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