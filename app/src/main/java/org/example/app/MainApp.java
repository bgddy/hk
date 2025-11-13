package org.example.app;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.example.core.*;
import org.example.ui.*;

public class MainApp extends Application {

    private VBox leftTopPane;
    private VBox rightTopPane;
    private Pane bottomPane;

    private AdjListGraph adjGraph;
    private AdjListGraphUI adjGraphUI;
    private MatrixGraph matrixGraph;
    private MatrixGraphUI matrixGraphUI;
    private SelectionSortUI selectionSortUI;
    private InsertSortUI insertSortUI;
    private FastSortUI fastSortUI;

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();

        // ==================== 上半部分 ====================
        HBox topPane = new HBox(15);
        topPane.setPadding(new Insets(15));
        topPane.setPrefHeight(200);
        topPane.setStyle("-fx-background-color: linear-gradient(to right, #e3f2fd, #f3e5f5);");

        leftTopPane = new VBox(12);
        leftTopPane.setPadding(new Insets(15));
        leftTopPane.setPrefWidth(250);
        leftTopPane.setStyle("-fx-background-color: white; -fx-border-color: #bbdefb; -fx-border-radius: 8; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        rightTopPane = new VBox(12);
        rightTopPane.setPadding(new Insets(15));
        rightTopPane.setPrefWidth(400);
        rightTopPane.setStyle("-fx-background-color: white; -fx-border-color: #bbdefb; -fx-border-radius: 8; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        topPane.getChildren().addAll(leftTopPane, rightTopPane);

        // ==================== 下半部分：画布和控制面板 ====================
        VBox bottomContainer = new VBox();
        bottomContainer.setPrefHeight(650);
        
        bottomPane = new Pane();
        bottomPane.setPrefHeight(580);
        bottomPane.setStyle("-fx-border-color: #bbdefb; -fx-border-radius: 8; -fx-background-color: linear-gradient(to bottom, #fafafa, #ffffff); -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 8, 0, 0, 3);");
        VBox.setVgrow(bottomPane, Priority.ALWAYS);
        
        // 控制面板将在updateInputArea中动态创建
        bottomContainer.getChildren().add(bottomPane);

        root.setTop(topPane);
        root.setCenter(bottomContainer);

        // ==================== 左上角：算法选择 ====================
        ComboBox<String> typeSelector = new ComboBox<>();
        typeSelector.getItems().addAll(
                "Selection Sort", "Insertion Sort", "Quick Sort",
                "Adjacency Matrix", "Adjacency List"
        );
        typeSelector.setValue("Selection Sort");
        leftTopPane.getChildren().addAll(new Label("Select Algorithm Type:"), typeSelector);

        // ==================== 初始化核心数据 ====================
        int[] sortData = {8, 3, 5, 1, 6};
        selectionSortUI = new SelectionSortUI(sortData);
        insertSortUI = new InsertSortUI(sortData);
        fastSortUI = new FastSortUI(sortData);

        adjGraph = new AdjListGraph(5);
        adjGraphUI = new AdjListGraphUI(adjGraph);

        matrixGraph = new MatrixGraph(5);
        matrixGraphUI = new MatrixGraphUI(matrixGraph);
        for (int i = 0; i < 5; i++) matrixGraphUI.addVertex(i);

        // ==================== 动态输入区：根据算法类型切换 ====================
        typeSelector.setOnAction(e -> updateInputArea(typeSelector.getValue()));

        // 默认加载排序输入
        updateInputArea("Selection Sort");

        Scene scene = new Scene(root, 1100, 800);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Unified Algorithm & Graph Visualization");
        primaryStage.show();
    }

    /** 动态更新右上角输入区 **/
    private void updateInputArea(String type) {
        rightTopPane.getChildren().clear();
        bottomPane.getChildren().clear();

        // 只在排序算法时创建控制面板
        HBox controlPanel = null;
        Button autoPlayBtn = null;
        Button nextStepBtn = null;
        Button resetBtn = null;
        Button pauseBtn = null;
        
        if (type.equals("Selection Sort") || type.equals("Insertion Sort") || type.equals("Quick Sort")) {
            autoPlayBtn = createStyledButton("自动播放", "#4caf50");
            nextStepBtn = createStyledButton("下一步", "#2196f3");
            resetBtn = createStyledButton("重置", "#ff9800");
            pauseBtn = createStyledButton("暂停", "#f44336");
            
            controlPanel = new HBox(15);
            controlPanel.setPadding(new Insets(15));
            controlPanel.setStyle("-fx-background-color: linear-gradient(to right, #e8f5e8, #e3f2fd); -fx-border-color: #c8e6c9; -fx-border-radius: 8;");
            controlPanel.setPrefHeight(70);
            controlPanel.getChildren().addAll(autoPlayBtn, nextStepBtn, pauseBtn, resetBtn);
        }

        switch (type) {
            case "Selection Sort":
            case "Insertion Sort":
            case "Quick Sort":
                TextField arrayInput = new TextField();
                arrayInput.setPromptText("输入数组，如: 8,3,5,1,6");
                Button sortBtn = new Button("开始排序");
                
                rightTopPane.getChildren().addAll(
                    new Label("排序输入:"), arrayInput, sortBtn
                );

                // 创建effectively final的引用
                final HBox finalControlPanel = controlPanel;
                
                sortBtn.setOnAction(ev -> {
                    String[] parts = arrayInput.getText().split(",");
                    int[] arr = new int[parts.length];
                    for (int i = 0; i < parts.length; i++)
                        arr[i] = Integer.parseInt(parts[i].trim());

                    bottomPane.getChildren().clear();
                    if (type.equals("Selection Sort")) {
                        selectionSortUI = new SelectionSortUI(arr);
                        bottomPane.getChildren().add(selectionSortUI.getRoot());
                    } else if (type.equals("Insertion Sort")) {
                        insertSortUI = new InsertSortUI(arr);
                        bottomPane.getChildren().add(insertSortUI.getRoot());
                    } else {
                        fastSortUI = new FastSortUI(arr);
                        bottomPane.getChildren().add(fastSortUI.getRoot());
                    }
                    
                    // 添加控制面板到底部
                    VBox bottomContainer = (VBox) bottomPane.getParent();
                    if (bottomContainer.getChildren().size() > 1) {
                        bottomContainer.getChildren().set(1, finalControlPanel);
                    } else {
                        bottomContainer.getChildren().add(finalControlPanel);
                    }
                });

                // 控制按钮事件
                autoPlayBtn.setOnAction(ev -> {
                    if (type.equals("Selection Sort") && selectionSortUI != null) {
                        selectionSortUI.visualizeSteps(1000L);
                    } else if (type.equals("Insertion Sort") && insertSortUI != null) {
                        insertSortUI.visualizeSteps(1000L);
                    } else if (type.equals("Quick Sort") && fastSortUI != null) {
                        fastSortUI.visualizeSteps(1500L); // 增加快速排序速度
                    }
                });

                nextStepBtn.setOnAction(ev -> {
                    if (type.equals("Selection Sort") && selectionSortUI != null) {
                        selectionSortUI.nextStep();
                    } else if (type.equals("Insertion Sort") && insertSortUI != null) {
                        insertSortUI.nextStep();
                    } else if (type.equals("Quick Sort") && fastSortUI != null) {
                        fastSortUI.nextStep();
                    }
                });

                pauseBtn.setOnAction(ev -> {
                    // 暂停功能暂时不实现，因为排序UI类没有pause方法
                    System.out.println("暂停功能暂未实现");
                });

                resetBtn.setOnAction(ev -> {
                    if (type.equals("Selection Sort") && selectionSortUI != null) {
                        selectionSortUI.reset();
                    } else if (type.equals("Insertion Sort") && insertSortUI != null) {
                        insertSortUI.reset();
                    } else if (type.equals("Quick Sort") && fastSortUI != null) {
                        fastSortUI.reset();
                    }
                });
                break;

            case "Adjacency List":
                rightTopPane.getChildren().addAll(new Label("邻接表图输入:"));

                // 直接使用VBox容器，移除滚动面板
                VBox adjListContent = new VBox(8);
                adjListContent.setPadding(new Insets(10));
                
                // 邻接表顶点固定，只保留边操作
                Label vertexInfo = new Label("顶点: 0-4 (固定5个顶点)");
                vertexInfo.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");

                TextField fromList = new TextField();
                fromList.setPromptText("From 顶点 (0-4)");
                fromList.setPrefWidth(180); // 扩大输入框宽度
                fromList.setPrefHeight(35); // 扩大输入框高度
                TextField toList = new TextField();
                toList.setPromptText("To 顶点 (0-4)");
                toList.setPrefWidth(180); // 扩大输入框宽度
                toList.setPrefHeight(35); // 扩大输入框高度
                TextField weightList = new TextField();
                weightList.setPromptText("权重");
                weightList.setPrefWidth(180); // 扩大输入框宽度
                weightList.setPrefHeight(35); // 扩大输入框高度

                Button addEdgeList = new Button("添加边");
                Button delEdgeList = new Button("删除边");

                // 添加遍历控制和起始点选择
                Label traversalLabel = new Label("遍历控制:");
                traversalLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
                
                TextField startVertexField = new TextField();
                startVertexField.setPromptText("起始顶点 (0-4)");
                startVertexField.setPrefWidth(180);
                startVertexField.setPrefHeight(35);
                
                // 遍历控制按钮
                HBox traversalButtons = new HBox(5);
                Button bfsButton = createStyledButton("BFS遍历", "#4caf50");
                Button dfsButton = createStyledButton("DFS遍历", "#2196f3");
                Button mstButton = createStyledButton("最小生成树", "#ff9800");
                traversalButtons.getChildren().addAll(bfsButton, dfsButton, mstButton);

                // 创建水平布局来紧凑排列按钮
                HBox edgeButtons = new HBox(5);
                edgeButtons.getChildren().addAll(addEdgeList, delEdgeList);

                // 添加图管理按钮
                Label graphManagementLabel = new Label("图管理:");
                graphManagementLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
                
                HBox graphManagementButtons = new HBox(5);
                Button clearAllEdgesBtn = createStyledButton("清空所有边", "#f44336");
                Button randomGraphBtn = createStyledButton("随机生成图", "#9c27b0");
                graphManagementButtons.getChildren().addAll(clearAllEdgesBtn, randomGraphBtn);
                
                // 图管理按钮事件
                clearAllEdgesBtn.setOnAction(ev -> {
                    adjGraphUI.clearAllEdges();
                });
                
                randomGraphBtn.setOnAction(ev -> {
                    adjGraphUI.generateRandomGraph();
                });

                adjListContent.getChildren().addAll(
                        vertexInfo,
                        new Label("边操作:"), fromList, toList, weightList, edgeButtons,
                        graphManagementLabel, graphManagementButtons,
                        traversalLabel, startVertexField, traversalButtons
                );
                
                rightTopPane.getChildren().add(adjListContent);

                bottomPane.getChildren().add(adjGraphUI.getPane());

                addEdgeList.setOnAction(ev -> {
                    int from = Integer.parseInt(fromList.getText());
                    int to = Integer.parseInt(toList.getText());
                    int w = Integer.parseInt(weightList.getText());
                    adjGraphUI.addEdge(from, to, w);
                });
                delEdgeList.setOnAction(ev -> {
                    int from = Integer.parseInt(fromList.getText());
                    int to = Integer.parseInt(toList.getText());
                    adjGraphUI.removeEdge(from, to);
                });

                // 算法按钮事件
                bfsButton.setOnAction(ev -> {
                    try {
                        String startVertexText = startVertexField.getText();
                        adjGraphUI.performBFS(startVertexText);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

                dfsButton.setOnAction(ev -> {
                    try {
                        String startVertexText = startVertexField.getText();
                        adjGraphUI.performDFS(startVertexText);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

                mstButton.setOnAction(ev -> {
                    try {
                        adjGraphUI.performMST();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                break;

            case "Adjacency Matrix":
                rightTopPane.getChildren().addAll(new Label("邻接矩阵图输入:"));

                // 简化邻接矩阵输入区域，只保留基本操作
                VBox matrixContent = new VBox(8);
                matrixContent.setPadding(new Insets(10));
                
                Label matrixVertexInfo = new Label("顶点操作:");
                matrixVertexInfo.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");

                TextField matrixVertexField = new TextField();
                matrixVertexField.setPromptText("顶点 ID");
                matrixVertexField.setPrefWidth(180);
                matrixVertexField.setPrefHeight(35);
                
                HBox matrixVertexButtons = new HBox(5);
                Button matrixAddVertexBtn = new Button("添加顶点");
                Button matrixDelVertexBtn = new Button("删除顶点");
                matrixVertexButtons.getChildren().addAll(matrixAddVertexBtn, matrixDelVertexBtn);

                Label matrixEdgeInfo = new Label("边操作:");
                matrixEdgeInfo.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");

                TextField matrixFrom = new TextField();
                matrixFrom.setPromptText("From 顶点");
                matrixFrom.setPrefWidth(180);
                matrixFrom.setPrefHeight(35);
                TextField matrixTo = new TextField();
                matrixTo.setPromptText("To 顶点");
                matrixTo.setPrefWidth(180);
                matrixTo.setPrefHeight(35);
                TextField matrixWeight = new TextField();
                matrixWeight.setPromptText("权重");
                matrixWeight.setPrefWidth(180);
                matrixWeight.setPrefHeight(35);

                HBox matrixEdgeButtons = new HBox(5);
                Button matrixAddEdge = new Button("添加边");
                Button matrixDelEdge = new Button("删除边");
                matrixEdgeButtons.getChildren().addAll(matrixAddEdge, matrixDelEdge);

                matrixContent.getChildren().addAll(
                        matrixVertexInfo, matrixVertexField, matrixVertexButtons,
                        matrixEdgeInfo, matrixFrom, matrixTo, matrixWeight, matrixEdgeButtons
                );
                
                rightTopPane.getChildren().add(matrixContent);

                bottomPane.getChildren().add(matrixGraphUI.getPane());

                matrixAddVertexBtn.setOnAction(ev -> {
                    int id = Integer.parseInt(matrixVertexField.getText());
                    matrixGraphUI.addVertex(id);
                });
                matrixDelVertexBtn.setOnAction(ev -> {
                    int id = Integer.parseInt(matrixVertexField.getText());
                    matrixGraphUI.removeVertex(id);
                });
                matrixAddEdge.setOnAction(ev -> {
                    int from = Integer.parseInt(matrixFrom.getText());
                    int to = Integer.parseInt(matrixTo.getText());
                    int w = Integer.parseInt(matrixWeight.getText());
                    matrixGraphUI.addEdge(from, to, w);
                });
                matrixDelEdge.setOnAction(ev -> {
                    int from = Integer.parseInt(matrixFrom.getText());
                    int to = Integer.parseInt(matrixTo.getText());
                    matrixGraphUI.removeEdge(from, to);
                });
                break;
        }
    }

    /** 创建样式化按钮 */
    private Button createStyledButton(String text, String color) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: " + color + "; " +
                       "-fx-text-fill: white; " +
                       "-fx-font-weight: bold; " +
                       "-fx-font-size: 14px; " +
                       "-fx-padding: 8 16; " +
                       "-fx-border-radius: 6; " +
                       "-fx-background-radius: 6; " +
                       "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 4, 0, 0, 2);");
        
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: " + darkenColor(color) + "; " +
                                                     "-fx-text-fill: white; " +
                                                     "-fx-font-weight: bold; " +
                                                     "-fx-font-size: 14px; " +
                                                     "-fx-padding: 8 16; " +
                                                     "-fx-border-radius: 6; " +
                                                     "-fx-background-radius: 6; " +
                                                     "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 6, 0, 0, 3);"));
        
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: " + color + "; " +
                                                    "-fx-text-fill: white; " +
                                                    "-fx-font-weight: bold; " +
                                                    "-fx-font-size: 14px; " +
                                                    "-fx-padding: 8 16; " +
                                                    "-fx-border-radius: 6; " +
                                                    "-fx-background-radius: 6; " +
                                                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 4, 0, 0, 2);"));
        return button;
    }
    
    /** 加深颜色 */
    private String darkenColor(String color) {
        switch (color) {
            case "#4caf50": return "#388e3c"; // 绿色加深
            case "#2196f3": return "#1976d2"; // 蓝色加深
            case "#ff9800": return "#f57c00"; // 橙色加深
            case "#f44336": return "#d32f2f"; // 红色加深
            default: return color;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
