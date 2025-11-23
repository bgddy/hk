package org.example.ui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.util.Random;

public class SortingRaceUI {
    private HBox root; 
    private SelectionSortUI selectionSortUI;
    private InsertSortUI insertSortUI;
    private FastSortUI fastSortUI;
    private int[] raceData;
    
    private Label[] timeLabels;
    private Label[] rankLabels;
    
    private long startTime;
    private int finishedCount = 0;
    private Timeline raceMonitor;

    public SortingRaceUI() {
        root = new HBox(15);
        root.setPadding(new Insets(15));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #f5f5f5;");
        
        timeLabels = new Label[3];
        rankLabels = new Label[3];
        
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
            // 【重要修复】范围改为 1-100，避免大量重复导致的快排退化
            raceData[i] = rand.nextInt(100) + 1; 
        }

        // 初始化
        selectionSortUI = new SelectionSortUI(raceData.clone());
        insertSortUI = new InsertSortUI(raceData.clone());
        fastSortUI = new FastSortUI(raceData.clone());
        
        // 【核心修复】必须手动开启 RaceMode，通知算法使用高速批量渲染
        selectionSortUI.setRaceMode(true);
        insertSortUI.setRaceMode(true);
        fastSortUI.setRaceMode(true);

        root.getChildren().clear();
        
        addRaceTrack(0, "Selection Sort", "O(N²)", selectionSortUI);
        addRaceTrack(1, "Insertion Sort", "O(N²)", insertSortUI);
        addRaceTrack(2, "Quick Sort", "O(N log N)", fastSortUI);
    }

    private void addRaceTrack(int index, String title, String complexity, ControllableSortUI sortUI) {
        VBox track = new VBox(10);
        track.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 8, 0, 0, 2);");
        track.setPadding(new Insets(15));
        track.setAlignment(Pos.TOP_CENTER);
        
        HBox.setHgrow(track, Priority.ALWAYS);
        track.setMaxWidth(Double.MAX_VALUE);
        
        Label nameLabel = new Label(title);
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        nameLabel.setTextFill(Color.web("#333"));
        
        Label complexityLabel = new Label(complexity);
        complexityLabel.setStyle("-fx-background-color: #e3f2fd; -fx-text-fill: #1565c0; -fx-padding: 2 6; -fx-background-radius: 4; -fx-font-size: 12px; -fx-font-weight: bold;");

        HBox header = new HBox(10, nameLabel, complexityLabel);
        header.setAlignment(Pos.CENTER);

        timeLabels[index] = new Label("⏱ 0 ms");
        timeLabels[index].setFont(Font.font("Monaco", FontWeight.NORMAL, 14));
        timeLabels[index].setTextFill(Color.web("#555"));
        
        rankLabels[index] = new Label("Waiting");
        rankLabels[index].setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #999;");
        
        HBox statusBox = new HBox(20, timeLabels[index], rankLabels[index]);
        statusBox.setAlignment(Pos.CENTER);
        statusBox.setStyle("-fx-background-color: #fafafa; -fx-padding: 8; -fx-background-radius: 5;");

        HBox algoRoot = sortUI.getRoot();
        algoRoot.setAlignment(Pos.BOTTOM_LEFT); 
        
        double scale = 0.065; 
        algoRoot.setScaleX(scale);
        algoRoot.setScaleY(0.6);
        
        Group chartGroup = new Group(algoRoot);
        
        StackPane chartContainer = new StackPane(chartGroup);
        chartContainer.setPrefHeight(200); 
        chartContainer.setAlignment(Pos.BOTTOM_CENTER);
        
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(track.widthProperty().subtract(30));
        clip.setHeight(200);
        chartContainer.setClip(clip);
        
        track.getChildren().addAll(header, statusBox, chartContainer);
        root.getChildren().add(track);
    }

    public void startRace() {
        stopMonitor();
        resetLabels();
        finishedCount = 0;
        
        // 极速延迟
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
        raceMonitor = new Timeline(new KeyFrame(Duration.millis(30), e -> { 
            long now = System.currentTimeMillis();
            long elapsed = now - startTime;
            
            for (int i = 0; i < 3; i++) {
                if (!finished[i] && timeLabels[i] != null) {
                    timeLabels[i].setText("⏱ " + elapsed + " ms");
                }
            }
            
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
            rankText = "🥇 冠军"; color = "#d32f2f";
        } else if (finishedCount == 2) {
            rankText = "🥈 亚军"; color = "#f57c00";
        } else {
            rankText = "🥉 季军"; color = "#757575";
        }
        
        rankLabels[index].setText(rankText);
        rankLabels[index].setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        timeLabels[index].setText("🏁 " + time + " ms");
        timeLabels[index].setTextFill(Color.web("#2e7d32"));
    }
    
    private void stopMonitor() {
        if (raceMonitor != null) raceMonitor.stop();
    }

    private boolean checkFinished(ControllableSortUI ui) {
        if (ui == null) return true;
        return !ui.isPlaying() && ui.getCurrentStep() >= ui.getTotalSteps() && ui.getTotalSteps() > 0;
    }
}