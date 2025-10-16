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
        HBox topPane = new HBox(10);
        topPane.setPadding(new Insets(10));
        topPane.setPrefHeight(180);

        leftTopPane = new VBox(10);
        leftTopPane.setPadding(new Insets(10));
        leftTopPane.setPrefWidth(220);
        leftTopPane.setStyle("-fx-border-color: black;");

        rightTopPane = new VBox(10);
        rightTopPane.setPadding(new Insets(10));
        rightTopPane.setPrefWidth(350);
        rightTopPane.setStyle("-fx-border-color: black;");

        topPane.getChildren().addAll(leftTopPane, rightTopPane);

        // ==================== 下半部分：画布 ====================
        bottomPane = new Pane();
        bottomPane.setPrefHeight(600);
        bottomPane.setStyle("-fx-border-color: black; -fx-background-color: white;");
        VBox.setVgrow(bottomPane, Priority.ALWAYS);

        root.setTop(topPane);
        root.setCenter(bottomPane);

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

        switch (type) {
            case "Selection Sort":
            case "Insertion Sort":
            case "Quick Sort":
                TextField arrayInput = new TextField();
                arrayInput.setPromptText("输入数组，如: 8,3,5,1,6");
                Button sortBtn = new Button("开始排序");
                rightTopPane.getChildren().addAll(new Label("排序输入:"), arrayInput, sortBtn);

                sortBtn.setOnAction(ev -> {
                    String[] parts = arrayInput.getText().split(",");
                    int[] arr = new int[parts.length];
                    for (int i = 0; i < parts.length; i++)
                        arr[i] = Integer.parseInt(parts[i].trim());

                    bottomPane.getChildren().clear();
                    if (type.equals("Selection Sort")) {
                        selectionSortUI = new SelectionSortUI(arr);
                        bottomPane.getChildren().add(selectionSortUI.getRoot());
                        selectionSortUI.visualizeSteps(new SelectionSort().sort(arr), 1000);
                    } else if (type.equals("Insertion Sort")) {
                        insertSortUI = new InsertSortUI(arr);
                        bottomPane.getChildren().add(insertSortUI.getRoot());
                        insertSortUI.visualizeSteps(new InsertSort().sort(arr), 1000);
                    } else {
                        fastSortUI = new FastSortUI(arr);
                        bottomPane.getChildren().add(fastSortUI.getRoot());
                        QuickSortStep[] stepsQuick = new FastSort().sort(arr);
                        fastSortUI.visualizeSteps(stepsQuick, 1000);
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

    public static void main(String[] args) {
        launch(args);
    }
}