// vmArgs = --module-path /Users/yuyongwenshu/.m2/repository/org/openjfx/javafx-base/20/javafx-base-20.jar:/Users/yuyongwenshu/.m2/repository/org/openjfx/javafx-controls/20/javafx-controls-20.jar:/Users/yuyongwenshu/.m2/repository/org/openjfx/javafx-graphics/20/javafx-graphics-20.jar:/Users/yuyongwenshu/.m2/repository/org/openjfx/javafx-fxml/20/javafx-fxml-20.jar --add-modules javafx.controls,javafx.fxml,javafx.graphics
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

        // ==================== 上半部分 (控制区域) ====================
        HBox topPane = new HBox(15);
        topPane.setPadding(new Insets(15));
        // [修复] 移除固定高度 200，改为根据内容自适应，防止按钮被遮挡
        // topPane.setPrefHeight(200); 
        topPane.setStyle("-fx-background-color: linear-gradient(to right, #e3f2fd, #f3e5f5);");

        leftTopPane = new VBox(12);
        leftTopPane.setPadding(new Insets(15));
        leftTopPane.setPrefWidth(250);
        leftTopPane.setStyle("-fx-background-color: white; -fx-border-color: #bbdefb; -fx-border-radius: 8; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        rightTopPane = new VBox(12);
        rightTopPane.setPadding(new Insets(15));
        rightTopPane.setPrefWidth(450); // 稍微加宽以容纳更多按钮
        rightTopPane.setStyle("-fx-background-color: white; -fx-border-color: #bbdefb; -fx-border-radius: 8; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        topPane.getChildren().addAll(leftTopPane, rightTopPane);

        // ==================== 下半部分：画布 ====================
        VBox bottomContainer = new VBox();
        bottomContainer.setPrefHeight(600); // 给画布预留足够空间
        VBox.setVgrow(bottomContainer, Priority.ALWAYS);
        
        bottomPane = new Pane();
        // 让 bottomPane 填充剩余空间
        bottomPane.prefHeightProperty().bind(bottomContainer.heightProperty());
        bottomPane.setStyle("-fx-border-color: #bbdefb; -fx-border-radius: 8; -fx-background-color: linear-gradient(to bottom, #fafafa, #ffffff); -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 8, 0, 0, 3);");
        
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

        // 调整窗口初始高度以适应更大的控制面板
        Scene scene = new Scene(root, 1100, 900);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Unified Algorithm & Graph Visualization");
        primaryStage.show();
    }

    /** 动态更新右上角输入区 **/
    private void updateInputArea(String type) {
        rightTopPane.getChildren().clear();
        bottomPane.getChildren().clear();

        // 排序算法底部的控制栏
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
                    // 清理旧的控制面板，确保只添加一个新的
                    bottomContainer.getChildren().removeIf(node -> node instanceof HBox);
                    bottomContainer.getChildren().add(finalControlPanel);
                });

                // 控制按钮事件
                autoPlayBtn.setOnAction(ev -> {
                    if (type.equals("Selection Sort") && selectionSortUI != null) {
                        selectionSortUI.visualizeSteps(1000L);
                    } else if (type.equals("Insertion Sort") && insertSortUI != null) {
                        insertSortUI.visualizeSteps(1000L);
                    } else if (type.equals("Quick Sort") && fastSortUI != null) {
                        fastSortUI.visualizeSteps(1500L);
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
                rightTopPane.getChildren().addAll(new Label("邻接表图操作:"));

                VBox adjListContent = new VBox(8);
                adjListContent.setPadding(new Insets(5));
                
                // --- 边操作 ---
                HBox edgeInputs = new HBox(5);
                TextField fromList = new TextField(); fromList.setPromptText("From"); fromList.setPrefWidth(60);
                TextField toList = new TextField(); toList.setPromptText("To"); toList.setPrefWidth(60);
                TextField weightList = new TextField(); weightList.setPromptText("权重"); weightList.setPrefWidth(60);
                edgeInputs.getChildren().addAll(fromList, toList, weightList);
                
                HBox edgeButtons = new HBox(5);
                Button addEdgeList = new Button("加边");
                Button delEdgeList = new Button("删边");
                edgeButtons.getChildren().addAll(addEdgeList, delEdgeList);

                // --- 图管理 ---
                Label graphManageLabel = new Label("图管理:");
                graphManageLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
                HBox graphManagementButtons = new HBox(5);
                Button clearAllEdgesBtn = createStyledButton("清空", "#f44336");
                Button randomGraphBtn = createStyledButton("随机", "#9c27b0");
                Button saveGraphBtn = createStyledButton("保存", "#607d8b");
                Button loadGraphBtn = createStyledButton("打开", "#607d8b");
                graphManagementButtons.getChildren().addAll(clearAllEdgesBtn, randomGraphBtn, saveGraphBtn, loadGraphBtn);
                
                // --- 算法控制 ---
                Label algoLabel = new Label("算法与路径:");
                algoLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");

                HBox pathInputs = new HBox(5);
                TextField startVertexField = new TextField(); startVertexField.setPromptText("起点"); startVertexField.setPrefWidth(80);
                TextField endVertexField = new TextField(); endVertexField.setPromptText("终点(最短路)"); endVertexField.setPrefWidth(100);
                pathInputs.getChildren().addAll(startVertexField, endVertexField);
                
                HBox algoButtons = new HBox(5);
                Button bfsButton = createStyledButton("BFS", "#4caf50");
                Button dfsButton = createStyledButton("DFS", "#2196f3");
                Button mstButton = createStyledButton("MST", "#ff9800");
                // [重点] 确保这个按钮被添加
                Button dijkstraButton = createStyledButton("Dijkstra最短路", "#e91e63");
                algoButtons.getChildren().addAll(bfsButton, dfsButton, mstButton, dijkstraButton);

                // 绑定事件
                clearAllEdgesBtn.setOnAction(ev -> adjGraphUI.clearAllEdges());
                randomGraphBtn.setOnAction(ev -> adjGraphUI.generateRandomGraph());
                saveGraphBtn.setOnAction(ev -> adjGraphUI.saveGraph());
                loadGraphBtn.setOnAction(ev -> adjGraphUI.loadGraph());

                adjListContent.getChildren().addAll(
                        new Label("边编辑:"), edgeInputs, edgeButtons,
                        new Separator(),
                        graphManageLabel, graphManagementButtons,
                        new Separator(),
                        algoLabel, pathInputs, algoButtons
                );
                
                rightTopPane.getChildren().add(adjListContent);
                bottomPane.getChildren().add(adjGraphUI.getPane());

                addEdgeList.setOnAction(ev -> {
                    try {
                        int from = Integer.parseInt(fromList.getText());
                        int to = Integer.parseInt(toList.getText());
                        int w = Integer.parseInt(weightList.getText());
                        adjGraphUI.addEdge(from, to, w);
                    } catch(NumberFormatException ex) { System.out.println("输入错误"); }
                });
                delEdgeList.setOnAction(ev -> {
                    try {
                        int from = Integer.parseInt(fromList.getText());
                        int to = Integer.parseInt(toList.getText());
                        adjGraphUI.removeEdge(from, to);
                    } catch(NumberFormatException ex) { System.out.println("输入错误"); }
                });

                bfsButton.setOnAction(ev -> adjGraphUI.performBFS(startVertexField.getText()));
                dfsButton.setOnAction(ev -> adjGraphUI.performDFS(startVertexField.getText()));
                mstButton.setOnAction(ev -> adjGraphUI.performMST());
                dijkstraButton.setOnAction(ev -> adjGraphUI.performDijkstra(startVertexField.getText(), endVertexField.getText()));
                break;

            case "Adjacency Matrix":
                rightTopPane.getChildren().addAll(new Label("邻接矩阵图操作:"));

                VBox matrixContent = new VBox(8);
                matrixContent.setPadding(new Insets(5));
                
                // --- 顶点/边操作 ---
                HBox mVertexOps = new HBox(5);
                TextField matrixVertexField = new TextField(); matrixVertexField.setPromptText("顶点ID"); matrixVertexField.setPrefWidth(60);
                Button matrixAddVertexBtn = new Button("+顶点");
                Button matrixDelVertexBtn = new Button("-顶点");
                mVertexOps.getChildren().addAll(matrixVertexField, matrixAddVertexBtn, matrixDelVertexBtn);

                HBox mEdgeInputs = new HBox(5);
                TextField matrixFrom = new TextField(); matrixFrom.setPromptText("From"); matrixFrom.setPrefWidth(60);
                TextField matrixTo = new TextField(); matrixTo.setPromptText("To"); matrixTo.setPrefWidth(60);
                TextField matrixWeight = new TextField(); matrixWeight.setPromptText("权重"); matrixWeight.setPrefWidth(60);
                mEdgeInputs.getChildren().addAll(matrixFrom, matrixTo, matrixWeight);

                HBox mEdgeOps = new HBox(5);
                Button matrixAddEdge = new Button("加边");
                Button matrixDelEdge = new Button("删边");
                mEdgeOps.getChildren().addAll(matrixAddEdge, matrixDelEdge);

                // --- 图管理 ---
                HBox mManageBtns = new HBox(5);
                Button matrixSaveBtn = createStyledButton("保存", "#607d8b");
                Button matrixLoadBtn = createStyledButton("打开", "#607d8b");
                mManageBtns.getChildren().addAll(matrixSaveBtn, matrixLoadBtn);
                
                // --- 算法 ---
                HBox mPathInputs = new HBox(5);
                TextField mStartField = new TextField(); mStartField.setPromptText("起点"); mStartField.setPrefWidth(80);
                TextField mEndField = new TextField(); mEndField.setPromptText("终点(最短路)"); mEndField.setPrefWidth(100);
                mPathInputs.getChildren().addAll(mStartField, mEndField);
                
                // [重点] 最短路径按钮
                Button matrixDijkstraBtn = createStyledButton("Dijkstra最短路", "#e91e63");
                
                // 绑定事件
                matrixSaveBtn.setOnAction(ev -> matrixGraphUI.saveGraph());
                matrixLoadBtn.setOnAction(ev -> matrixGraphUI.loadGraph());
                matrixDijkstraBtn.setOnAction(ev -> matrixGraphUI.performDijkstra(mStartField.getText(), mEndField.getText()));

                matrixContent.getChildren().addAll(
                        new Label("结构编辑:"), mVertexOps, mEdgeInputs, mEdgeOps,
                        new Separator(),
                        new Label("管理:"), mManageBtns,
                        new Separator(),
                        new Label("算法:"), mPathInputs, matrixDijkstraBtn
                );
                
                rightTopPane.getChildren().add(matrixContent);
                bottomPane.getChildren().add(matrixGraphUI.getPane());

                matrixAddVertexBtn.setOnAction(ev -> {
                    try {
                        int id = Integer.parseInt(matrixVertexField.getText());
                        matrixGraphUI.addVertex(id);
                    } catch (NumberFormatException ex) { System.out.println("输入错误"); }
                });
                matrixDelVertexBtn.setOnAction(ev -> {
                    try {
                        int id = Integer.parseInt(matrixVertexField.getText());
                        matrixGraphUI.removeVertex(id);
                    } catch (NumberFormatException ex) { System.out.println("输入错误"); }
                });
                matrixAddEdge.setOnAction(ev -> {
                    try {
                        int from = Integer.parseInt(matrixFrom.getText());
                        int to = Integer.parseInt(matrixTo.getText());
                        int w = Integer.parseInt(matrixWeight.getText());
                        matrixGraphUI.addEdge(from, to, w);
                    } catch (NumberFormatException ex) { System.out.println("输入错误"); }
                });
                matrixDelEdge.setOnAction(ev -> {
                    try {
                        int from = Integer.parseInt(matrixFrom.getText());
                        int to = Integer.parseInt(matrixTo.getText());
                        matrixGraphUI.removeEdge(from, to);
                    } catch (NumberFormatException ex) { System.out.println("输入错误"); }
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
                       "-fx-font-size: 12px; " +
                       "-fx-padding: 6 12; " +
                       "-fx-border-radius: 4; " +
                       "-fx-background-radius: 4; " +
                       "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 2, 0, 0, 1);");
        
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: " + darkenColor(color) + "; " +
                                                     "-fx-text-fill: white; " +
                                                     "-fx-font-weight: bold; " +
                                                     "-fx-font-size: 12px; " +
                                                     "-fx-padding: 6 12; " +
                                                     "-fx-border-radius: 4; " +
                                                     "-fx-background-radius: 4; " +
                                                     "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 4, 0, 0, 2);"));
        
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: " + color + "; " +
                                                    "-fx-text-fill: white; " +
                                                    "-fx-font-weight: bold; " +
                                                    "-fx-font-size: 12px; " +
                                                    "-fx-padding: 6 12; " +
                                                    "-fx-border-radius: 4; " +
                                                    "-fx-background-radius: 4; " +
                                                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 2, 0, 0, 1);"));
        return button;
    }
    
    /** 加深颜色 */
    private String darkenColor(String color) {
        switch (color) {
            case "#4caf50": return "#388e3c"; // 绿色加深
            case "#2196f3": return "#1976d2"; // 蓝色加深
            case "#ff9800": return "#f57c00"; // 橙色加深
            case "#f44336": return "#d32f2f"; // 红色加深
            case "#9c27b0": return "#7b1fa2"; // 紫色加深
            case "#607d8b": return "#455a64"; // 蓝灰加深
            case "#e91e63": return "#c2185b"; // 粉色加深
            default: return color;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}