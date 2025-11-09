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

        // 创建控制面板按钮
        Button autoPlayBtn = createStyledButton("自动播放", "#4caf50");
        Button nextStepBtn = createStyledButton("下一步", "#2196f3");
        Button resetBtn = createStyledButton("重置", "#ff9800");
        Button pauseBtn = createStyledButton("暂停", "#f44336");
        
        HBox controlPanel = new HBox(15);
        controlPanel.setPadding(new Insets(15));
        controlPanel.setStyle("-fx-background-color: linear-gradient(to right, #e8f5e8, #e3f2fd); -fx-border-color: #c8e6c9; -fx-border-radius: 8;");
        controlPanel.setPrefHeight(70);
        controlPanel.getChildren().addAll(autoPlayBtn, nextStepBtn, pauseBtn, resetBtn);

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
                        bottomContainer.getChildren().set(1, controlPanel);
                    } else {
                        bottomContainer.getChildren().add(controlPanel);
                    }
                });

                // 控制按钮事件
                autoPlayBtn.setOnAction(ev -> {
                    if (type.equals("Selection Sort") && selectionSortUI != null) {
                        selectionSortUI.visualizeSteps(1000);
                    } else if (type.equals("Insertion Sort") && insertSortUI != null) {
                        insertSortUI.visualizeSteps(1000);
                    } else if (type.equals("Quick Sort") && fastSortUI != null) {
                        fastSortUI.visualizeSteps(1500); // 增加快速排序速度
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
                    if (type.equals("Selection Sort") && selectionSortUI != null) {
                        selectionSortUI.pause();
                    } else if (type.equals("Insertion Sort") && insertSortUI != null) {
                        insertSortUI.pause();
                    } else if (type.equals("Quick Sort") && fastSortUI != null) {
                        fastSortUI.pause();
                    }
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
                TextField fromList = new TextField();
                fromList.setPromptText("From 顶点");
                TextField toList = new TextField();
                toList.setPromptText("To 顶点");
                TextField weightList = new TextField();
                weightList.setPromptText("权重");

                Button addEdgeList = new Button("添加边");
                Button delEdgeList = new Button("删除边");
                Button dfsBtn = new Button("DFS 遍历");
                Button bfsBtn = new Button("BFS 遍历");
                Button kruskalBtn = new Button("最小生成树");

                rightTopPane.getChildren().addAll(fromList, toList, weightList, addEdgeList, delEdgeList, dfsBtn, bfsBtn, kruskalBtn);
                bottomPane.getChildren().add(adjGraphUI.getRoot());
                adjGraphUI.drawEdges();

                addEdgeList.setOnAction(ev -> {
                    int from = Integer.parseInt(fromList.getText());
                    int to = Integer.parseInt(toList.getText());
                    int w = Integer.parseInt(weightList.getText());
                    adjGraphUI.insertEdge(from, to, w);
                });
                delEdgeList.setOnAction(ev -> {
                    int from = Integer.parseInt(fromList.getText());
                    int to = Integer.parseInt(toList.getText());
                    adjGraphUI.deleteEdge(from, to);
                });

                dfsBtn.setOnAction(ev -> {
                    TextInputDialog dialog = new TextInputDialog("0");
                    dialog.setHeaderText("请输入DFS起始顶点:");
                    int start = Integer.parseInt(dialog.showAndWait().get());
                    adjGraphUI.visualizeDFS(start);
                });

                bfsBtn.setOnAction(ev -> {
                    TextInputDialog dialog = new TextInputDialog("0");
                    dialog.setHeaderText("请输入BFS起始顶点:");
                    int start = Integer.parseInt(dialog.showAndWait().get());
                    adjGraphUI.visualizeBFS(start);
                });

                kruskalBtn.setOnAction(ev -> {
                    Edge[] mstEdges = new kruskal(adjGraph).generateMST();
                    adjGraphUI.visualizeKruskal(mstEdges);
                });
                break;

            case "Adjacency Matrix":
                rightTopPane.getChildren().addAll(new Label("邻接矩阵图输入:"));

                TextField vertexField = new TextField();
                vertexField.setPromptText("顶点 ID");
                Button addVertexBtn = new Button("添加顶点");
                Button delVertexBtn = new Button("删除顶点");

                TextField fromM = new TextField();
                fromM.setPromptText("From 顶点");
                TextField toM = new TextField();
                toM.setPromptText("To 顶点");
                TextField weightM = new TextField();
                weightM.setPromptText("权重");

                Button addEdgeM = new Button("添加边");
                Button delEdgeM = new Button("删除边");

                rightTopPane.getChildren().addAll(
                        new Label("顶点操作:"), vertexField, addVertexBtn, delVertexBtn,
                        new Label("边操作:"), fromM, toM, weightM, addEdgeM, delEdgeM
                );

                bottomPane.getChildren().add(matrixGraphUI.getPane());

                addVertexBtn.setOnAction(ev -> {
                    int id = Integer.parseInt(vertexField.getText());
                    matrixGraphUI.addVertex(id);
                });
                delVertexBtn.setOnAction(ev -> {
                    int id = Integer.parseInt(vertexField.getText());
                    matrixGraphUI.removeVertex(id);
                });
                addEdgeM.setOnAction(ev -> {
                    int from = Integer.parseInt(fromM.getText());
                    int to = Integer.parseInt(toM.getText());
                    int w = Integer.parseInt(weightM.getText());
                    matrixGraphUI.addEdge(from, to, w);
                });
                delEdgeM.setOnAction(ev -> {
                    int from = Integer.parseInt(fromM.getText());
                    int to = Integer.parseInt(toM.getText());
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
