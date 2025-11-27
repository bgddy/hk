package org.example.ui;

import javafx.animation.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.example.core.FastSort;
import org.example.core.SortFrame;
import java.util.List;

public class FastSortUI extends ControllableSortUI {

    private int[] originalData;
    private List<SortFrame> steps;
    
    
    private static final String[] PSEUDO_CODE = {
        "quickSort(arr, low, high)",         // 0
        "  if (low < high)",                 // 1
        "    pivot = arr[high]; i = low-1",  // 2
        "    for (j=low; j<high; j++)",      // 3
        "      if (arr[j] < pivot)",         // 4
        "        i++; swap(arr[i], arr[j])", // 5
        "    swap(arr[i+1], arr[high])",     // 6
        "    pi = i + 1",                    // 7
        "    quickSort(arr, low, pi-1)",     // 8
        "    quickSort(arr, pi+1, high)"     // 9
    };

    public FastSortUI(int[] data) {
        super();
        this.originalData = data.clone();
        
        initBars(data);
        initCodeView(PSEUDO_CODE);
        
        FastSort sorter = new FastSort();
        this.steps = sorter.sort(data);
    }

    private void initBars(int[] data) {
        barsContainer.getChildren().clear();
        bars = new Rectangle[data.length];
        for (int i = 0; i < data.length; i++) {
            double height = data[i] * SCALE;
            Rectangle rect = new Rectangle(BAR_WIDTH, height, Color.LIGHTBLUE);
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
                
                // 稳定性模式检查
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
                
                // UI 更新
                if (k == stepsPerFrame - 1 || currentStep == steps.size() - 1) {
                    // 传入详细参数进行高亮渲染
                    updateBarsWithHighlights(currentArray, frame);
                    
                    if (stabilityMode) refreshColors();
                    
                    if (!isRaceMode) highlightLine(frame.getLineIndex());
                    else highlightLine(-1);
                }
                currentStep++;
            }

            if (currentStep >= steps.size()) {
                timeline.stop();
                isPlaying = false;
                highlightLine(-1);
                resetBarColors();
            }
        });
        
        timeline.getKeyFrames().add(kf);
        this.animation = timeline;
        timeline.play();
    }

    // 核心高亮逻辑
    private void updateBarsWithHighlights(int[] arr, SortFrame frame) {
        int iPtr = frame.i;
        int jPtr = frame.j;
        int pivotIdx = frame.extra; // 约定 extra 为 pivot 的位置
        int left = frame.l;
        int right = frame.r;

        for (int k = 0; k < bars.length; k++) {
            double height = arr[k] * SCALE;
            bars[k].setHeight(height);
            bars[k].setTranslateY(BASELINE - height);
            
            if (!stabilityMode) {
                // 默认颜色
                Color color = Color.LIGHTBLUE;
                
                // 1. 标记当前递归区间 (稍微深一点的背景色，或者保持浅蓝但其他变灰)
                if (left != -1 && right != -1) {
                    if (k >= left && k <= right) {
                        color = Color.SKYBLUE; // 活跃区间
                    } else {
                        color = Color.rgb(220, 220, 220); // 非活跃区间变灰
                    }
                }
                
                // 2. 高亮特殊角色 (优先级高于区间)
                if (k == pivotIdx) {
                    color = Color.PURPLE; // Pivot
                } else if (k == jPtr) {
                    color = Color.RED;    // j (扫描)
                } else if (k == iPtr) {
                    color = Color.BLUE;   // i (小于pivot的边界)
                }
                
                bars[k].setFill(color);
            }
        }
    }
    
    private void resetBarColors() {
        if(!stabilityMode) {
            for(Rectangle r : bars) r.setFill(Color.LIGHTBLUE);
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
            updateBarsWithHighlights(frame.getArrayState(), frame);
            highlightLine(frame.getLineIndex());
            currentStep++;
        }
    }
    
    @Override public int getTotalSteps() { return steps.size(); }
}