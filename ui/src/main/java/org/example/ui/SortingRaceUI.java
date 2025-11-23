package org.example.ui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group; // 关键导入
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.util.Random;

public class SortingRaceUI {
    private HBox root; // 根布局：水平排列
    
    private SelectionSortUI selectionSortUI;
    private InsertSortUI insertSortUI;
    private FastSortUI fastSortUI;
    
    private int[] raceData;
    
    // === 仪表盘控件 ===
    private Label[] timeLabels;
    private Label[] rankLabels;
    
    // === 监控数据 ===
    private long startTime;
    private int finishedCount = 0;
    private Timeline raceMonitor;

    public SortingRaceUI() {
        // 1. 初始化根布局 (HBox)
        root = new HBox(15);
        root.setPadding(new Insets(15));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #f5f5f5;");
        
        // 2. 初始化显示控件数组
        timeLabels = new Label[3];
        rankLabels = new Label[3];
        
        // 3. 生成初始数据 (默认100个，配合你修改后的核心算法效果最佳)
        generateNewData(100);
    }

    public HBox getRoot() {
        return root;
    }

    public void generateNewData(int size) {
        stopMonitor();
        finishedCount = 0;
        
        raceData = new int[size];
        Random rand = new Random();
        for (int i = 0; i < size; i++) {
            raceData[i] = rand.nextInt(8) + 1; 
        }

        // 初始化三个算法 UI
        selectionSortUI = new SelectionSortUI(raceData.clone());
        insertSortUI = new InsertSortUI(raceData.clone());
        fastSortUI = new FastSortUI(raceData.clone());

        // 重建界面
        root.getChildren().clear();
        
        // 添加三个赛道
        addRaceTrack(0, "Selection Sort", "O(N²)", selectionSortUI);
        addRaceTrack(1, "Insertion Sort", "O(N²)", insertSortUI);
        addRaceTrack(2, "Quick Sort", "O(N log N)", fastSortUI);
    }

    private void addRaceTrack(int index, String title, String complexity, ControllableSortUI sortUI) {
        // 单个赛道容器 (垂直布局: 标题 -> 信息 -> 图表)
        VBox track = new VBox(10);
        track.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 8, 0, 0, 2);");
        track.setPadding(new Insets(15));
        track.setAlignment(Pos.TOP_CENTER);
        
        // 限制宽度，确保三个能放下并自动填充
        HBox.setHgrow(track, Priority.ALWAYS);
        track.setMaxWidth(Double.MAX_VALUE);
        
        // 1. 标题栏
        Label nameLabel = new Label(title);
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        nameLabel.setTextFill(Color.web("#333"));
        
        Label complexityLabel = new Label(complexity);
        complexityLabel.setStyle("-fx-background-color: #e3f2fd; -fx-text-fill: #1565c0; -fx-padding: 2 6; -fx-background-radius: 4; -fx-font-size: 12px; -fx-font-weight: bold;");

        HBox header = new HBox(10, nameLabel, complexityLabel);
        header.setAlignment(Pos.CENTER);

        // 2. 状态栏 (时间和名次)
        timeLabels[index] = new Label("⏱ 0 ms");
        timeLabels[index].setFont(Font.font("Monaco", FontWeight.NORMAL, 14));
        timeLabels[index].setTextFill(Color.web("#555"));
        
        rankLabels[index] = new Label("Waiting");
        rankLabels[index].setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #999;");
        
        HBox statusBox = new HBox(20, timeLabels[index], rankLabels[index]);
        statusBox.setAlignment(Pos.CENTER);
        statusBox.setStyle("-fx-background-color: #fafafa; -fx-padding: 8; -fx-background-radius: 5;");

        // 3. 算法图表 (使用 Group 修复缩放布局问题)
        HBox algoRoot = sortUI.getRoot();
        algoRoot.setAlignment(Pos.BOTTOM_LEFT); 
        
        // 智能缩放：数据量 100 -> 宽 ~5500px。我们需要它缩放到 ~350px。
        // Scale = 350 / 5500 ≈ 0.065
        double scale = 0.065; 
        algoRoot.setScaleX(scale);
        algoRoot.setScaleY(0.6); // Y轴保持较高可见度
        
        // 【核心修复】用 Group 包裹 algoRoot，让布局容器识别缩放后的真实大小
        Group chartGroup = new Group(algoRoot);
        
        // 使用 StackPane 居中显示 Group
        StackPane chartContainer = new StackPane(chartGroup);
        chartContainer.setPrefHeight(200); 
        chartContainer.setAlignment(Pos.BOTTOM_CENTER);
        
        // 裁剪溢出 (防止动画过程中柱子飞出边界)
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(track.widthProperty().subtract(30)); // 减去 padding
        clip.setHeight(200);
        chartContainer.setClip(clip);
        
        track.getChildren().addAll(header, statusBox, chartContainer);
        root.getChildren().add(track);
    }

    // === 控制逻辑 ===

    public void startRace() {
        stopMonitor();
        resetLabels();
        finishedCount = 0;
        
        // === 速度设置 ===
        // 如果你已经按照建议修改了 SelectionSort 和 InsertSort 的核心代码（记录内层循环），
        // 那么这里必须设为极速 (1ms)，否则动画会跑很久。
        // 即使是 1ms，因为 N^2 步数巨大，它们也会比快排慢很多。
        long delay = 1; 
        
        if (selectionSortUI != null) selectionSortUI.visualizeSteps(delay);
        if (insertSortUI != null) insertSortUI.visualizeSteps(delay);
        if (fastSortUI != null) fastSortUI.visualizeSteps(delay);
        
        startTime = System.currentTimeMillis();
        startMonitor();
    }

    public void pauseRace() {
        stopMonitor();
        if (selectionSortUI != null) selectionSortUI.pause();
        if (insertSortUI != null) insertSortUI.pause();
        if (fastSortUI != null) fastSortUI.pause();
    }

    public void resetRace() {
        stopMonitor();
        resetLabels();
        if (selectionSortUI != null) selectionSortUI.reset();
        if (insertSortUI != null) insertSortUI.reset();
        if (fastSortUI != null) fastSortUI.reset();
    }
    
    public void nextStep() {
        if (selectionSortUI != null) selectionSortUI.nextStep();
        if (insertSortUI != null) insertSortUI.nextStep();
        if (fastSortUI != null) fastSortUI.nextStep();
    }

    private void resetLabels() {
        for (int i = 0; i < 3; i++) {
            if (timeLabels[i] != null) {
                timeLabels[i].setText("⏱ 0 ms");
                timeLabels[i].setTextFill(Color.web("#555"));
                rankLabels[i].setText("Running...");
                rankLabels[i].setTextFill(Color.web("#999"));
                rankLabels[i].setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #999;");
            }
        }
    }

    private void startMonitor() {
        boolean[] finished = new boolean[3]; 
        // 30ms 刷新一次界面时间
        raceMonitor = new Timeline(new KeyFrame(Duration.millis(30), e -> { 
            long now = System.currentTimeMillis();
            long elapsed = now - startTime;
            
            // 更新实时时间
            for (int i = 0; i < 3; i++) {
                if (!finished[i] && timeLabels[i] != null) {
                    timeLabels[i].setText("⏱ " + elapsed + " ms");
                }
            }
            
            // 检查完成状态
            if (!finished[0] && checkFinished(selectionSortUI)) {
                markFinished(0, elapsed);
                finished[0] = true;
            }
            if (!finished[1] && checkFinished(insertSortUI)) {
                markFinished(1, elapsed);
                finished[1] = true;
            }
            if (!finished[2] && checkFinished(fastSortUI)) {
                markFinished(2, elapsed);
                finished[2] = true;
            }
            
            if (finishedCount >= 3) stopMonitor();
        }));
        raceMonitor.setCycleCount(Timeline.INDEFINITE);
        raceMonitor.play();
    }
    
    private void markFinished(int index, long time) {
        finishedCount++;
        
        String rankText = "";
        String color = "";
        
        if (finishedCount == 1) {
            rankText = "🥇 冠军";
            color = "#d32f2f"; // 红色醒目
        } else if (finishedCount == 2) {
            rankText = "🥈 亚军";
            color = "#f57c00"; // 橙色
        } else {
            rankText = "🥉 季军";
            color = "#757575"; // 灰色
        }
        
        rankLabels[index].setText(rankText);
        rankLabels[index].setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        
        timeLabels[index].setText("🏁 " + time + " ms");
        timeLabels[index].setTextFill(Color.web("#2e7d32")); // 绿色表示完成
    }
    
    private void stopMonitor() {
        if (raceMonitor != null) raceMonitor.stop();
    }

    private boolean checkFinished(ControllableSortUI ui) {
        if (ui == null) return true;
        return !ui.isPlaying() && ui.getCurrentStep() >= ui.getTotalSteps() && ui.getTotalSteps() > 0;
    }
}