package org.example.ui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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
            int low = frame.getL();   
            int high = frame.getR();  

            // [修复] 同步 permutation 交换
            if (lineIndex == 6) {
                // swap(arr[i], arr[j])
                swapPermutation(i, j);
            } else if (lineIndex == 7) {
                // swap(arr[i + 1], arr[high])
                // 注意：在 FastSort.java 中，frame.getI() 传递的是 finalPos (即 i+1)
                // 所以这里应该是 swapPermutation(finalPos, high)
                swapPermutation(i, high);
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
                if (k < low || k > high) { 
                    if (!stabilityMode) bars[k].setFill(Color.GRAY);
                } else if (k == j) { // Pivot 原始位置 / 扫描位置
                    bars[k].setFill(Color.PURPLE);
                } else if (k == i && i != -1 && lineIndex < 7) { 
                    bars[k].setFill(Color.RED);
                } else if (k > i && k < j) { 
                    bars[k].setFill(Color.ORANGE);
                } else if (k == i && lineIndex >= 7) { 
                    bars[k].setFill(Color.GREEN);
                } else {
                    if (!stabilityMode) bars[k].setFill(Color.LIGHTGREEN);
                }
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