package org.example.ui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.example.core.InsertSort;
import org.example.core.SortFrame;
import java.util.List;

public class InsertSortUI extends ControllableSortUI {
    private List<SortFrame> steps;
    private final String[] pseudocode = {
        "for i from 1 to n-1",
        "  key = arr[i]",
        "  j = i - 1",
        "  while j >= 0 and arr[j] > key",
        "    arr[j + 1] = arr[j]",
        "    j = j - 1",
        "  arr[j + 1] = key"
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
            int i = frame.getI(); // 当前元素/已排序边界
            int j = frame.getJ(); // 扫描指针
            int key = frame.getExtra(); // key值（在数组中是虚拟的）

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
                } 
                if (k == i) { // 边界 (i)
                    bars[k].setFill(Color.YELLOW);
                } 
                if (k == j) { // 当前被比较元素 (j)
                    bars[k].setFill(Color.ORANGE);
                }
                if (k == j + 1 && lineIndex == 6) { // 插入 Key 的位置
                     bars[k].setFill(Color.RED);
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