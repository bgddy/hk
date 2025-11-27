package org.example.ui;

import javafx.animation.*;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.example.core.InsertSort;
import org.example.core.SortFrame;
import java.util.List;

public class InsertSortUI extends ControllableSortUI {

    private int[] originalData;
    private List<SortFrame> steps;
    
    
    private int tempKeyPermIndex = -1;
    
    private static final String[] PSEUDO_CODE = {
        "for i from 1 to n-1",           // 0
        "  key = arr[i]; j = i - 1",     // 1
        "  while j >= 0 && arr[j] > key",// 2
        "    arr[j + 1] = arr[j]",       // 3
        "    j = j - 1",                 // 4
        "  arr[j + 1] = key"             // 5
    };

    public InsertSortUI(int[] data) {
        super(); 
        this.originalData = data.clone();
        
        initBars(data);
        initCodeView(PSEUDO_CODE);
        
        InsertSort sorter = new InsertSort();
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
        
       
        if (stabilityMode) {
            setStabilityMode(true, originalData);
            tempKeyPermIndex = -1;
        }

        int stepsPerFrame = isRaceMode ? 50 : 1;

        Timeline timeline = new Timeline();
        timeline.setCycleCount(Animation.INDEFINITE);
        
        KeyFrame kf = new KeyFrame(Duration.millis(stepDelay), e -> {
            for (int k = 0; k < stepsPerFrame && currentStep < steps.size(); k++) {
                SortFrame frame = steps.get(currentStep);
                int[] currentArray = frame.getArrayState();
                
                
                if (stabilityMode && permutation != null) {
                    int line = frame.getLineIndex();
                    
                    
                    if (line == 1) {
                        if (frame.i >= 0 && frame.i < permutation.length) {
                            tempKeyPermIndex = permutation[frame.i];
                        }
                    }
                   
                    else if (line == 3) {
                        
                        if (frame.j >= 0 && frame.extra >= 0 && frame.extra < permutation.length) {
                            permutation[frame.extra] = permutation[frame.j];
                        }
                    }
                    
                    else if (line == 5) {
                        if (frame.extra >= 0 && frame.extra < permutation.length && tempKeyPermIndex != -1) {
                            permutation[frame.extra] = tempKeyPermIndex;
                        }
                    }
                }
                
                // UI 更新
                if (k == stepsPerFrame - 1 || currentStep == steps.size() - 1) {
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

    private void updateBarsWithHighlights(int[] arr, SortFrame frame) {
        int idxI = frame.i;
        int idxJ = frame.j;
        int idxExtra = frame.extra; // 用于标记 key 或 移动目标

        for (int k = 0; k < bars.length; k++) {
            double height = arr[k] * SCALE;
            bars[k].setHeight(height);
            bars[k].setTranslateY(BASELINE - height);
            
            if (!stabilityMode) {
                Color color = Color.LIGHTGREEN; // 默认颜色
                
                // 颜色优先级逻辑：
                if (k == idxExtra && idxExtra != -1) {
                    color = Color.ORANGE; // 移动目标
                } else if (k == idxJ && idxJ != -1) {
                    color = Color.RED;    // 比较对象
                } else if (k == idxI && idxI != -1) {
                    color = Color.ROYALBLUE; // 待插入元素初始位置
                } 
                
                bars[k].setFill(color);
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
            updateBarsWithHighlights(frame.getArrayState(), frame);
            highlightLine(frame.getLineIndex());
            currentStep++;
        }
    }
    
    @Override public int getTotalSteps() { return steps.size(); }
}