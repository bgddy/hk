package org.example.ui;

import javafx.animation.*;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.example.core.SelectionSort;
import org.example.core.SortFrame;
import java.util.List;

public class SelectionSortUI extends ControllableSortUI {

    private int[] originalData;
    private List<SortFrame> steps;
    
    private static final String[] PSEUDO_CODE = {
        "for i from 0 to n-1",           // 0
        "  min_idx = i",                 // 1
        "  for j from i+1 to n",         // 2
        "    if arr[j] < arr[min_idx]",  // 3
        "      min_idx = j",             // 4
        "  swap(arr[i], arr[min_idx])"   // 5
    };

    public SelectionSortUI(int[] data) {
        super();
        this.originalData = data.clone();
        
        // 这里使用父类的 barsContainer
        initBars(data);
        initCodeView(PSEUDO_CODE);
        
        SelectionSort sorter = new SelectionSort();
        this.steps = sorter.sort(data);
    }

    private void initBars(int[] data) {
        barsContainer.getChildren().clear();
        bars = new Rectangle[data.length];
        for (int i = 0; i < data.length; i++) {
            double height = data[i] * SCALE;
            Rectangle rect = new Rectangle(BAR_WIDTH, height, Color.LIGHTGREEN);
            rect.setTranslateX(i * (BAR_WIDTH + SPACING));
            rect.setTranslateY(BASELINE - height);
            bars[i] = rect;
            barsContainer.getChildren().add(rect);
        }
    }

    @Override
    public void visualizeSteps(long stepDelay) {
        if (animation != null) animation.stop();
        this.isPlaying = true;
        if (currentStep >= steps.size()) currentStep = 0;
        
        if (stabilityMode) setStabilityMode(true, originalData);
        
        final int[] prevArray = originalData.clone();
        int stepsPerFrame = isRaceMode ? 50 : 1;

        Timeline timeline = new Timeline();
        timeline.setCycleCount(Animation.INDEFINITE);
        
        KeyFrame kf = new KeyFrame(Duration.millis(stepDelay), e -> {
            for (int k = 0; k < stepsPerFrame && currentStep < steps.size(); k++) {
                SortFrame frame = steps.get(currentStep);
                int[] currentArray = frame.getArrayState();
                
                // 稳定性模式处理
                if (stabilityMode) {
                    int s1 = -1, s2 = -1;
                    for (int m = 0; m < currentArray.length; m++) {
                        if (currentArray[m] != prevArray[m]) {
                            if (s1 == -1) s1 = m; else s2 = m;
                        }
                    }
                    if (s1 != -1 && s2 != -1) swapPermutation(s1, s2);
                }
                System.arraycopy(currentArray, 0, prevArray, 0, currentArray.length);
                
                // 渲染更新 (仅在每帧最后一步)
                if (k == stepsPerFrame - 1 || currentStep == steps.size() - 1) {
                    // 传递 i, j, minIdx 进行高亮渲染
                    updateBarsWithHighlights(currentArray, frame.i, frame.j, frame.extra);
                    
                    if (stabilityMode) refreshColors();
                    
                    // 同步高亮代码行
                    if (!isRaceMode) highlightLine(frame.getLineIndex());
                    else highlightLine(-1);
                }
                currentStep++;
            }

            if (currentStep >= steps.size()) {
                timeline.stop();
                isPlaying = false;
                highlightLine(-1);
                resetBarColors(); // 完成后恢复绿色
            }
        });
        
        timeline.getKeyFrames().add(kf);
        this.animation = timeline;
        timeline.play();
    }

    // 核心：带高亮参数的更新方法
    private void updateBarsWithHighlights(int[] arr, int idxI, int idxJ, int idxMin) {
        for (int k = 0; k < bars.length; k++) {
            double height = arr[k] * SCALE;
            bars[k].setHeight(height);
            bars[k].setTranslateY(BASELINE - height);
            
            if (!stabilityMode) {
                // 优先级：minIdx (橙) > j (红) > i (蓝) > 普通 (绿)
                if (k == idxMin && idxMin != -1) {
                    bars[k].setFill(Color.ORANGE); // 最小值
                } else if (k == idxJ && idxJ != -1) {
                    bars[k].setFill(Color.RED);    // 正在比较
                } else if (k == idxI && idxI != -1) {
                    bars[k].setFill(Color.ROYALBLUE); // 当前轮次基准
                } else {
                    bars[k].setFill(Color.LIGHTGREEN);
                }
            }
        }
    }
    
    private void resetBarColors() {
        if(!stabilityMode) {
            for(Rectangle r : bars) r.setFill(Color.LIGHTGREEN);
        }
    }

    @Override
    public void reset() {
        if (animation != null) animation.stop();
        isPlaying = false;
        currentStep = 0;
        highlightLine(-1);
        initBars(originalData);
        if (stabilityMode) setStabilityMode(true, originalData);
    }
    
    @Override 
    public void nextStep() { 
        if (currentStep < steps.size()) {
            SortFrame frame = steps.get(currentStep);
            updateBarsWithHighlights(frame.getArrayState(), frame.i, frame.j, frame.extra);
            highlightLine(frame.getLineIndex());
            currentStep++;
        }
    }
    
    @Override public int getTotalSteps() { return steps.size(); }
}