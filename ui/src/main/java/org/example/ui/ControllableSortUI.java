package org.example.ui;

import javafx.animation.Animation;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class ControllableSortUI {
    protected HBox root;          
    protected Pane barsContainer;  
    protected ListView<String> codeListView; 
    
    protected TextArea explanationArea;
    protected VBox rightPanel;

    protected Rectangle[] bars;
    protected Text[] labels;
    protected Animation animation;
    protected int currentStep = 0;
    protected boolean isPlaying = false;
    protected boolean stabilityMode = false;
    protected boolean isRaceMode = false; 
    
    // 用于跟踪每个位置当前的元素在原始数组中的索引
    protected int[] permutation; 
    protected Map<Integer, Color> colorMap = new HashMap<>();
    
    protected static final double BAR_WIDTH = 40; 
    protected static final double SCALE = 40;
    protected static final double BASELINE = 400;
    protected static final double SPACING = 5;

    public ControllableSortUI() {
        root = new HBox(20); 
        root.setStyle("-fx-padding: 20;");
        
        barsContainer = new Pane();
        barsContainer.setPrefSize(600, 450);
        HBox.setHgrow(barsContainer, Priority.ALWAYS);
        
        root.getChildren().add(barsContainer);
    }

    protected void initCodeView(String[] pseudocode) {
        if (rightPanel != null) {
            root.getChildren().remove(rightPanel);
        }

        codeListView = new ListView<>();
        codeListView.getItems().addAll(pseudocode);
        codeListView.setPrefWidth(280); 
        codeListView.setPrefHeight(300);
        codeListView.setStyle("-fx-font-family: 'Consolas', 'Monaco', monospace; -fx-font-size: 14px; -fx-border-color: #ccc; -fx-border-width: 1;");
        
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
                        setStyle("-fx-background-color: #ffeb3b; -fx-text-fill: #000; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-background-color: white; -fx-text-fill: #333;");
                    }
                }
            }
        });

        explanationArea = new TextArea();
        explanationArea.setPrefSize(280, 100);
        explanationArea.setWrapText(true);
        explanationArea.setEditable(false);
        explanationArea.setPromptText("算法步骤解释将显示在这里...");
        explanationArea.setStyle("-fx-font-family: 'Microsoft YaHei', sans-serif; -fx-font-size: 14px; -fx-control-inner-background: #f4f4f4;");

        rightPanel = new VBox(10); 
        rightPanel.getChildren().addAll(new Label("算法伪代码:"), codeListView, new Label("当前步骤详解:"), explanationArea);

        root.getChildren().add(rightPanel);
    }

    // === [新增] 动态调整动画速度 ===
    // 允许在动画播放过程中实时改变速度
    public void setAnimationSpeed(long delay) {
        // 只有当前正在播放时，才需要重启时间轴来应用新速度
        // 如果当前是暂停状态，只需要下次调用 visualizeSteps 时传入新值即可（这由 MainApp 控制）
        if (isPlaying() && animation != null) {
            visualizeSteps(delay);
        }
    }

    protected void highlightLine(int lineIndex) {
        if (codeListView != null && lineIndex >= 0 && lineIndex < codeListView.getItems().size()) {
            codeListView.getSelectionModel().select(lineIndex);
            codeListView.scrollTo(lineIndex);
        } else if (codeListView != null) {
            codeListView.getSelectionModel().clearSelection();
        }
    }
    
    protected void updateExplanation(String text) {
        if (explanationArea != null) {
            explanationArea.setText(text);
        }
    }

    public HBox getRoot() { return root; }
    public abstract void visualizeSteps(long stepDelay);
    public abstract void nextStep();
    public void pause() { if (animation != null && animation.getStatus() == Animation.Status.RUNNING) { animation.pause(); isPlaying = false; } }
    public void play() { if (animation != null && animation.getStatus() == Animation.Status.PAUSED) { animation.play(); isPlaying = true; } }
    public abstract void reset();
    public boolean isPlaying() { return isPlaying; }
    public int getCurrentStep() { return currentStep; }
    public abstract int getTotalSteps();
    
    public void setStabilityMode(boolean enable, int[] originalData) { 
        this.stabilityMode = enable; 
        if (enable && originalData != null) {
            initPermutation(originalData); 
            refreshColors(); 
        }
    }
    public void setRaceMode(boolean enable) { this.isRaceMode = enable; }
    
    // 初始化颜色映射：相同值的元素会获得不同的颜色（蓝、红、紫、橙）
    protected void initPermutation(int[] originalData) {
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

    // 根据 permutation 刷新所有柱子的颜色
    protected void refreshColors() {
        if (!stabilityMode || bars == null || permutation == null) return;
        for (int i = 0; i < bars.length; i++) {
            int originalIndex = permutation[i];
            // 如果该元素的原始索引在 colorMap 中（说明它是重复值之一），使用特定颜色
            if (colorMap.containsKey(originalIndex)) {
                bars[i].setFill(colorMap.get(originalIndex));
            } else {
                // 非重复元素，默认浅绿
                bars[i].setFill(Color.LIGHTGREEN); 
            }
        }
    }

    // 交换 permutation 中的两个位置，模拟元素交换
    protected void swapPermutation(int i, int j) {
        if (permutation == null || i < 0 || j < 0 || i >= permutation.length || j >= permutation.length) return;
        int temp = permutation[i];
        permutation[i] = permutation[j];
        permutation[j] = temp;
    }
}