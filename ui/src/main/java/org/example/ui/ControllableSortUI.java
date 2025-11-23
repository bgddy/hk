package org.example.ui;

import javafx.animation.Animation;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class ControllableSortUI {
    protected HBox root;           // 主容器
    protected Pane barsContainer;  // 柱状图容器
    protected ListView<String> codeListView; // 代码显示列表
    
    protected Rectangle[] bars;
    protected Animation animation;
    protected int currentStep = 0;
    protected boolean isPlaying = false;
    
    // === 模式控制 ===
    protected boolean stabilityMode = false;
    protected boolean isRaceMode = false; 
    
    protected int[] permutation; 
    protected Map<Integer, Color> colorMap = new HashMap<>();
    
    protected static final double BAR_WIDTH = 40; // 稍微调窄一点以适应代码框
    protected static final double SCALE = 40;
    protected static final double BASELINE = 400;
    protected static final double SPACING = 5;

    public ControllableSortUI() {
        // 初始化根布局
        root = new HBox(20); // 间距20
        root.setStyle("-fx-padding: 20;");
        
        // 初始化柱状图区域
        barsContainer = new Pane();
        barsContainer.setPrefSize(600, 450);
        
        // 将柱状图区域设为自适应增长
        HBox.setHgrow(barsContainer, Priority.ALWAYS);
        
        root.getChildren().add(barsContainer);
    }

    // 新增：初始化代码视图
    protected void initCodeView(String[] pseudocode) {
        codeListView = new ListView<>();
        codeListView.getItems().addAll(pseudocode);
        codeListView.setPrefWidth(250);
        codeListView.setPrefHeight(300);
        // 简单的等宽字体样式
        codeListView.setStyle("-fx-font-family: 'Consolas', 'Monaco', monospace; -fx-font-size: 14px; -fx-border-color: #ccc; -fx-border-width: 1;");
        
        // 自定义单元格工厂以实现高亮
        codeListView.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (isSelected()) {
                        // 选中行高亮 (黄色背景)
                        setStyle("-fx-background-color: #ffeb3b; -fx-text-fill: #000; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-background-color: white; -fx-text-fill: #333;");
                    }
                }
            }
        });
        
        // 将代码视图添加到右侧
        if (!root.getChildren().contains(codeListView)) {
            root.getChildren().add(codeListView);
        }
    }

    // 新增：高亮指定行
    protected void highlightLine(int lineIndex) {
        if (codeListView != null && lineIndex >= 0 && lineIndex < codeListView.getItems().size()) {
            // JavaFX 的选择模型会自动触发 CellFactory 的 updateItem
            codeListView.getSelectionModel().select(lineIndex);
            codeListView.scrollTo(lineIndex);
        } else if (codeListView != null) {
            codeListView.getSelectionModel().clearSelection();
        }
    }

    public HBox getRoot() { return root; }
    public abstract void visualizeSteps(long stepDelay);
    public abstract void nextStep();

    public void pause() {
        if (animation != null && animation.getStatus() == Animation.Status.RUNNING) {
            animation.pause();
            isPlaying = false;
        }
    }

    public void play() {
        if (animation != null && animation.getStatus() == Animation.Status.PAUSED) {
            animation.play();
            isPlaying = true;
        }
    }

    public abstract void reset();
    public boolean isPlaying() { return isPlaying; }
    public int getCurrentStep() { return currentStep; }
    public abstract int getTotalSteps();

    // ... (stabilityMode 相关代码保持不变，直接复制过来即可) ...
    public void setStabilityMode(boolean enable, int[] originalData) {
        this.stabilityMode = enable;
        if (!enable) return;
        initPermutation(originalData);
        refreshColors();
    }
    
    public void setRaceMode(boolean enable) {
        this.isRaceMode = enable;
    }

    private void initPermutation(int[] originalData) {
        this.permutation = new int[originalData.length];
        for (int i = 0; i < originalData.length; i++) permutation[i] = i;

        Map<Integer, List<Integer>> valueIndices = new HashMap<>();
        for (int i = 0; i < originalData.length; i++) {
            valueIndices.computeIfAbsent(originalData[i], k -> new ArrayList<>()).add(i);
        }

        colorMap.clear();
        Color[] palette = {Color.BLUE, Color.RED, Color.PURPLE, Color.ORANGE}; 

        for (List<Integer> indices : valueIndices.values()) {
            if (indices.size() > 1) { 
                for (int k = 0; k < indices.size(); k++) {
                    colorMap.put(indices.get(k), palette[k % palette.length]);
                }
            }
        }
    }

    protected void refreshColors() {
        if (!stabilityMode || bars == null) return;
        for (int i = 0; i < bars.length; i++) {
            int originalIndex = permutation[i];
            if (colorMap.containsKey(originalIndex)) {
                bars[i].setFill(colorMap.get(originalIndex));
            } else {
                bars[i].setFill(Color.LIGHTGREEN); 
            }
        }
    }
    
    protected void swapPermutation(int i, int j) {
        if (permutation == null) return;
        int temp = permutation[i];
        permutation[i] = permutation[j];
        permutation[j] = temp;
    }
}