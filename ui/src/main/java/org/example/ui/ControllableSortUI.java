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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class ControllableSortUI {
    protected HBox root;          
    protected Pane barsContainer;  
    protected ListView<String> codeListView; 
    
    // 解释文本区域和右侧容器
    protected TextArea explanationArea;
    protected VBox rightPanel;

    protected Rectangle[] bars;
    protected Animation animation;
    protected int currentStep = 0;
    protected boolean isPlaying = false;
    protected boolean stabilityMode = false;
    protected boolean isRaceMode = false; 
    
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

    // [修复] 修改了此方法，防止重置时重复添加右侧面板
    protected void initCodeView(String[] pseudocode) {
        // 1. 关键修复：如果右侧面板已经存在，先从根布局中移除它！
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

        // 初始化解释区域
        explanationArea = new TextArea();
        explanationArea.setPrefSize(280, 100);
        explanationArea.setWrapText(true);
        explanationArea.setEditable(false);
        explanationArea.setPromptText("算法步骤解释将显示在这里...");
        explanationArea.setStyle("-fx-font-family: 'Microsoft YaHei', sans-serif; -fx-font-size: 14px; -fx-control-inner-background: #f4f4f4;");

        // 创建新的右侧面板
        rightPanel = new VBox(10); // 间距 10
        rightPanel.getChildren().addAll(new Label("算法伪代码:"), codeListView, new Label("当前步骤详解:"), explanationArea);

        // 添加到根布局
        root.getChildren().add(rightPanel);
    }

    protected void highlightLine(int lineIndex) {
        if (codeListView != null && lineIndex >= 0 && lineIndex < codeListView.getItems().size()) {
            codeListView.getSelectionModel().select(lineIndex);
            codeListView.scrollTo(lineIndex);
        } else if (codeListView != null) {
            codeListView.getSelectionModel().clearSelection();
        }
    }
    
    // 更新解释文本的方法
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
    
    public void setStabilityMode(boolean enable, int[] originalData) { this.stabilityMode = enable; if (!enable) return; initPermutation(originalData); refreshColors(); }
    public void setRaceMode(boolean enable) { this.isRaceMode = enable; }
    
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