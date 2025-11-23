package org.example.app;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.example.core.*;
import org.example.ui.*;

public class MainApp extends Application {

    private VBox leftTopPane;
    private VBox rightTopPane;
    private VBox aiPane; 
    private Pane bottomPane;

    private LLMService llmService = new LLMService();
    private ComboBox<String> typeSelector;

    private AdjListGraph adjGraph;
    private AdjListGraphUI adjGraphUI;
    private MatrixGraph matrixGraph;
    private MatrixGraphUI matrixGraphUI;
    
    private SelectionSortUI selectionSortUI;
    private InsertSortUI insertSortUI;
    private FastSortUI fastSortUI;
    
    // 新增：排序竞技场UI
    private SortingRaceUI sortingRaceUI;

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();

        // ==================== 顶部区域 ====================
        HBox topPane = new HBox(15);
        topPane.setPadding(new Insets(15));
        topPane.setStyle("-fx-background-color: linear-gradient(to right, #e3f2fd, #f3e5f5);");

        // 1. 左上
        leftTopPane = new VBox(12);
        leftTopPane.setPadding(new Insets(15));
        leftTopPane.setPrefWidth(250);
        leftTopPane.setStyle("-fx-background-color: white; -fx-border-color: #bbdefb; -fx-border-radius: 8; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        // 2. 中上
        rightTopPane = new VBox(12);
        rightTopPane.setPadding(new Insets(15));
        rightTopPane.setPrefWidth(450);
        rightTopPane.setStyle("-fx-background-color: white; -fx-border-color: #bbdefb; -fx-border-radius: 8; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        // 3. 右上
        aiPane = initAIPanel(); 

        topPane.getChildren().addAll(leftTopPane, rightTopPane, aiPane);
        HBox.setHgrow(aiPane, Priority.ALWAYS);

        // ==================== 下半部分 ====================
        VBox bottomContainer = new VBox();
        bottomContainer.setPrefHeight(600);
        VBox.setVgrow(bottomContainer, Priority.ALWAYS);
        
        bottomPane = new Pane();
        bottomPane.prefHeightProperty().bind(bottomContainer.heightProperty());
        bottomPane.setStyle("-fx-border-color: #bbdefb; -fx-border-radius: 8; -fx-background-color: linear-gradient(to bottom, #fafafa, #ffffff); -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 8, 0, 0, 3);");
        
        bottomContainer.getChildren().add(bottomPane);

        root.setTop(topPane);
        root.setCenter(bottomContainer);

        // ==================== 初始化 ====================
        typeSelector = new ComboBox<>();
        typeSelector.getItems().addAll(
                "Selection Sort", "Insertion Sort", "Quick Sort",
                "Sorting Race", // 新增选项
                "Adjacency Matrix", "Adjacency List"
        );
        typeSelector.setValue("Selection Sort");
        leftTopPane.getChildren().addAll(new Label("Select Algorithm Type:"), typeSelector);

        int[] sortData = {8, 3, 5, 1, 6};
        selectionSortUI = new SelectionSortUI(sortData);
        insertSortUI = new InsertSortUI(sortData);
        fastSortUI = new FastSortUI(sortData);

        adjGraph = new AdjListGraph(5);
        adjGraphUI = new AdjListGraphUI(adjGraph);

        matrixGraph = new MatrixGraph(5);
        matrixGraphUI = new MatrixGraphUI(matrixGraph);
        for (int i = 0; i < 5; i++) matrixGraphUI.addVertex(i);

        typeSelector.setOnAction(e -> updateInputArea(typeSelector.getValue()));
        updateInputArea("Selection Sort");

        Scene scene = new Scene(root, 1200, 900);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Unified Algorithm & Graph Visualization + AI");
        primaryStage.show();
    }

    private VBox initAIPanel() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(15));
        box.setStyle("-fx-background-color: #f0f8ff; -fx-border-color: #90caf9; -fx-border-radius: 8; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        Label title = new Label("🤖 AI 智能绘图助手 (DeepSeek)");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1565c0;");

        TextArea promptInput = new TextArea();
        promptInput.setPromptText("在此输入自然语言，例如：\n- 创建一个5个点的环\n- 你是谁？");
        promptInput.setWrapText(true);
        promptInput.setPrefHeight(60);

        Button sendBtn = createStyledButton("发送指令", "#2196f3");
        sendBtn.setMaxWidth(Double.MAX_VALUE);

        Label responseLabel = new Label("AI 回复:");
        responseLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #555;");
        
        TextArea responseArea = new TextArea();
        responseArea.setPromptText("AI 的回复将显示在这里...");
        responseArea.setEditable(false);
        responseArea.setWrapText(true);
        responseArea.setPrefHeight(100);
        responseArea.setStyle("-fx-control-inner-background: #eef; -fx-font-family: monospace; -fx-font-size: 11px;");

        Label statusLabel = new Label("准备就绪");
        statusLabel.setStyle("-fx-text-fill: #757575; -fx-font-size: 11px;");

        sendBtn.setOnAction(e -> {
            String input = promptInput.getText();
            if (input.isEmpty()) return;

            String mode = typeSelector.getValue();
            if (mode == null || (!mode.contains("Adjacency") && !mode.contains("Matrix"))) {
                statusLabel.setText("⚠️ 请先选择一种图论算法模式");
                statusLabel.setTextFill(Color.RED);
                return;
            }

            statusLabel.setText("Thinking... (DeepSeek)");
            statusLabel.setTextFill(Color.BLUE);
            sendBtn.setDisable(true);
            responseArea.setText(""); 

            llmService.generateDSL(input).thenAccept(response -> {
                Platform.runLater(() -> {
                    sendBtn.setDisable(false);
                    if (response.startsWith("[DSL]")) {
                        String dslContent = response.replace("[DSL]", "").trim();
                        responseArea.setText("✅ 已识别为绘图指令，正在生成...\n\n" + dslContent);
                        applyDSL(dslContent); 
                        statusLabel.setText("✅ 图形已生成");
                        statusLabel.setTextFill(Color.GREEN);
                    } else if (response.startsWith("[MSG]")) {
                        String msgContent = response.replace("[MSG]", "").trim();
                        responseArea.setText(msgContent);
                        statusLabel.setText("💬 AI 回复完毕");
                        statusLabel.setTextFill(Color.GRAY);
                    } else {
                        responseArea.setText(response);
                        statusLabel.setText("❓ 未知格式");
                        statusLabel.setTextFill(Color.ORANGE);
                    }
                });
            }).exceptionally(ex -> {
                Platform.runLater(() -> {
                    statusLabel.setText("❌ 请求失败");
                    responseArea.setText("错误: " + ex.getMessage());
                    statusLabel.setTextFill(Color.RED);
                    sendBtn.setDisable(false);
                });
                return null;
            });
        });

        box.getChildren().addAll(title, promptInput, sendBtn, statusLabel, responseLabel, responseArea);
        return box;
    }

    private void applyDSL(String dsl) {
        String type = typeSelector.getValue();
        System.out.println("AI Generated DSL:\n" + dsl);

        if (type.equals("Adjacency List")) {
            adjGraphUI.resetToDefault(); 
            adjGraphUI.renderFromDSL(dsl);
        } else if (type.equals("Adjacency Matrix")) {
            matrixGraphUI.resetToDefault(); 
            matrixGraphUI.renderFromDSL(dsl);
        }
    }

    private void updateInputArea(String type) {
        rightTopPane.getChildren().clear();
        bottomPane.getChildren().clear();

        HBox controlPanel = null;
        
        // 修改判断条件，包含 Sorting Race
        if (type.contains("Sort") || type.contains("Race")) {
            Button autoPlayBtn = createStyledButton("自动播放", "#4caf50");
            if (type.contains("Race")) autoPlayBtn.setText("开始竞速"); // 竞速模式特殊文字

            Button nextStepBtn = createStyledButton("下一步", "#2196f3");
            Button resetBtn = createStyledButton("重置", "#ff9800");
            Button pauseBtn = createStyledButton("暂停", "#f44336");
            
            controlPanel = new HBox(15);
            controlPanel.setPadding(new Insets(15));
            controlPanel.setStyle("-fx-background-color: linear-gradient(to right, #e8f5e8, #e3f2fd); -fx-border-color: #c8e6c9; -fx-border-radius: 8;");
            controlPanel.setPrefHeight(70);
            controlPanel.getChildren().addAll(autoPlayBtn, nextStepBtn, pauseBtn, resetBtn);
            
            if (type.equals("Selection Sort") && selectionSortUI != null) {
                autoPlayBtn.setOnAction(ev -> selectionSortUI.visualizeSteps(1000L));
                nextStepBtn.setOnAction(ev -> selectionSortUI.nextStep());
                resetBtn.setOnAction(ev -> selectionSortUI.reset());
                pauseBtn.setOnAction(ev -> selectionSortUI.pause());
            } else if (type.equals("Insertion Sort") && insertSortUI != null) {
                autoPlayBtn.setOnAction(ev -> insertSortUI.visualizeSteps(1000L));
                nextStepBtn.setOnAction(ev -> insertSortUI.nextStep());
                resetBtn.setOnAction(ev -> insertSortUI.reset());
                pauseBtn.setOnAction(ev -> insertSortUI.pause());
            } else if (type.equals("Quick Sort") && fastSortUI != null) {
                autoPlayBtn.setOnAction(ev -> fastSortUI.visualizeSteps(1500L));
                nextStepBtn.setOnAction(ev -> fastSortUI.nextStep());
                resetBtn.setOnAction(ev -> fastSortUI.reset());
                pauseBtn.setOnAction(ev -> fastSortUI.pause());
            } else if (type.equals("Sorting Race")) {
                // 竞速模式的逻辑绑定
                if (sortingRaceUI == null) {
                    sortingRaceUI = new SortingRaceUI();
                }
                
                // 添加竞速模式特有的“生成新数据”按钮
                Button newDataBtn = createStyledButton("换一组数据", "#9c27b0");
                newDataBtn.setOnAction(ev -> sortingRaceUI.generateNewData(100));
                controlPanel.getChildren().add(newDataBtn); // 添加到控制面板末尾

                autoPlayBtn.setOnAction(ev -> sortingRaceUI.startRace());
                nextStepBtn.setOnAction(ev -> sortingRaceUI.nextStep());
                resetBtn.setOnAction(ev -> sortingRaceUI.resetRace());
                pauseBtn.setOnAction(ev -> sortingRaceUI.pauseRace());
            }
        }

        switch (type) {
            case "Selection Sort":
            case "Insertion Sort":
            case "Quick Sort":
                TextField arrayInput = new TextField();
                arrayInput.setPromptText("输入数组，如: 8,3,5,1,6");
                Button sortBtn = new Button("开始排序");
                rightTopPane.getChildren().addAll(new Label("排序输入:"), arrayInput, sortBtn);

                final HBox finalControlPanel = controlPanel;
                sortBtn.setOnAction(ev -> {
                    String[] parts = arrayInput.getText().split(",");
                    int[] arr = new int[parts.length];
                    for (int i = 0; i < parts.length; i++) arr[i] = Integer.parseInt(parts[i].trim());
                    
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
                    VBox container = (VBox) bottomPane.getParent();
                    container.getChildren().removeIf(node -> node instanceof HBox);
                    container.getChildren().add(finalControlPanel);
                });
                break;
            
            // 新增：竞速模式
            case "Sorting Race":
                if (sortingRaceUI == null) {
                    sortingRaceUI = new SortingRaceUI();
                }
                
                // 1. 清空并设置中间区域
                bottomPane.getChildren().clear();
                HBox raceRoot = sortingRaceUI.getRoot(); // 获取新的 HBox 根节点
                
                // 绑定宽度和高度，让它填满区域
                raceRoot.prefWidthProperty().bind(bottomPane.widthProperty());
                raceRoot.prefHeightProperty().bind(bottomPane.heightProperty());
                
                bottomPane.getChildren().add(raceRoot);
                
                // 2. 清空顶部中间区域（不再需要计分板占位，因为计分板已经集成在赛道里了）
                rightTopPane.getChildren().clear(); 
                rightTopPane.getChildren().add(new Label("🏆 算法竞速模式 - 实时监控中")); // 可以放个简单标题
                
                // 3. 设置底部控制按钮
                VBox container = (VBox) bottomPane.getParent();
                container.getChildren().removeIf(node -> node instanceof HBox);
                container.getChildren().add(controlPanel);
                break;
            case "Adjacency List":
                buildGraphControlPanel("邻接表", adjGraphUI, rightTopPane);
                bottomPane.getChildren().add(adjGraphUI.getPane());
                break;

            case "Adjacency Matrix":
                buildMatrixControlPanel("邻接矩阵", matrixGraphUI, rightTopPane);
                bottomPane.getChildren().add(matrixGraphUI.getPane());
                break;
        }
    }

    private void buildGraphControlPanel(String title, AdjListGraphUI ui, VBox pane) {
        pane.getChildren().add(new Label(title + "操作:"));
        
        HBox edgeInputs = new HBox(5);
        TextField fromT = new TextField(); fromT.setPromptText("From"); fromT.setPrefWidth(50);
        TextField toT = new TextField(); toT.setPromptText("To"); toT.setPrefWidth(50);
        TextField wT = new TextField(); wT.setPromptText("W"); wT.setPrefWidth(50);
        Button addBtn = new Button("加"), delBtn = new Button("删");
        edgeInputs.getChildren().addAll(fromT, toT, wT, addBtn, delBtn);
        
        Label graphManageLabel = new Label("图管理:");
        graphManageLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        HBox graphManagementButtons = new HBox(5);
        Button clearAllEdgesBtn = createStyledButton("清空", "#f44336");
        Button randomGraphBtn = createStyledButton("随机", "#9c27b0");
        Button saveGraphBtn = createStyledButton("保存", "#607d8b");
        Button loadGraphBtn = createStyledButton("打开", "#607d8b");
        graphManagementButtons.getChildren().addAll(clearAllEdgesBtn, randomGraphBtn, saveGraphBtn, loadGraphBtn);

        Label algoLabel = new Label("算法与路径:");
        algoLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        
        HBox algoBox = new HBox(5);
        TextField startT = new TextField(); startT.setPromptText("Start"); startT.setPrefWidth(50);
        TextField endT = new TextField(); endT.setPromptText("End"); endT.setPrefWidth(50);
        Button bfsBtn = createStyledButton("BFS", "#4caf50");
        Button dfsBtn = createStyledButton("DFS", "#2196f3");
        Button mstBtn = createStyledButton("MST", "#ff9800");
        Button dijBtn = createStyledButton("Dijkstra", "#e91e63");
        algoBox.getChildren().addAll(startT, endT, bfsBtn, dfsBtn);
        HBox algoBox2 = new HBox(5);
        algoBox2.getChildren().addAll(mstBtn, dijBtn);

        Label dslLabel = new Label("DSL 手动输入:");
        dslLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        TextArea dslArea = new TextArea();
        dslArea.setPromptText("手动输入 DSL...");
        dslArea.setPrefHeight(60);
        Button renderDslBtn = createStyledButton("渲染 DSL", "#009688");

        addBtn.setOnAction(e -> {
            try { ui.addEdge(Integer.parseInt(fromT.getText()), Integer.parseInt(toT.getText()), Integer.parseInt(wT.getText())); } 
            catch(Exception ex){} 
        });
        delBtn.setOnAction(e -> {
            try { ui.removeEdge(Integer.parseInt(fromT.getText()), Integer.parseInt(toT.getText())); } 
            catch(Exception ex){} 
        });
        clearAllEdgesBtn.setOnAction(ev -> ui.resetToDefault());
        randomGraphBtn.setOnAction(ev -> ui.generateRandomGraph());
        saveGraphBtn.setOnAction(ev -> ui.saveGraph());
        loadGraphBtn.setOnAction(ev -> ui.loadGraph());
        
        renderDslBtn.setOnAction(ev -> ui.renderFromDSL(dslArea.getText()));

        bfsBtn.setOnAction(e -> ui.performBFS(startT.getText()));
        dfsBtn.setOnAction(e -> ui.performDFS(startT.getText()));
        mstBtn.setOnAction(e -> ui.performMST());
        dijBtn.setOnAction(e -> ui.performDijkstra(startT.getText(), endT.getText()));

        pane.getChildren().addAll(
            new Label("边管理:"), edgeInputs, 
            new Separator(), graphManageLabel, graphManagementButtons,
            new Separator(), dslLabel, dslArea, renderDslBtn,
            new Separator(), algoLabel, algoBox, algoBox2
        );
    }

    private void buildMatrixControlPanel(String title, MatrixGraphUI ui, VBox pane) {
        pane.getChildren().add(new Label(title + "操作:"));
        
        HBox vertexInputs = new HBox(5);
        TextField vertexIdT = new TextField(); vertexIdT.setPromptText("ID"); vertexIdT.setPrefWidth(50);
        Button addVBtn = new Button("+顶点"), delVBtn = new Button("-顶点");
        vertexInputs.getChildren().addAll(vertexIdT, addVBtn, delVBtn);

        HBox edgeInputs = new HBox(5);
        TextField fromT = new TextField(); fromT.setPromptText("From"); fromT.setPrefWidth(50);
        TextField toT = new TextField(); toT.setPromptText("To"); toT.setPrefWidth(50);
        TextField wT = new TextField(); wT.setPromptText("W"); wT.setPrefWidth(50);
        Button addBtn = new Button("加"), delBtn = new Button("删");
        edgeInputs.getChildren().addAll(fromT, toT, wT, addBtn, delBtn);
        
        HBox mManageBtns = new HBox(5);
        Button matrixClearBtn = createStyledButton("清空", "#f44336");
        Button matrixRandomBtn = createStyledButton("随机", "#9c27b0");
        Button matrixSaveBtn = createStyledButton("保存", "#607d8b");
        Button matrixLoadBtn = createStyledButton("打开", "#607d8b");
        mManageBtns.getChildren().addAll(matrixClearBtn, matrixRandomBtn, matrixSaveBtn, matrixLoadBtn);

        HBox algoBox = new HBox(5);
        TextField startT = new TextField(); startT.setPromptText("Start"); startT.setPrefWidth(50);
        TextField endT = new TextField(); endT.setPromptText("End"); endT.setPrefWidth(50);
        Button dijBtn = createStyledButton("Dijkstra最短路", "#e91e63");
        algoBox.getChildren().addAll(startT, endT, dijBtn);

        TextArea mDslArea = new TextArea();
        mDslArea.setPromptText("手动输入 DSL...");
        mDslArea.setPrefHeight(60);
        Button mRenderDslBtn = createStyledButton("渲染 DSL", "#009688");

        addVBtn.setOnAction(e -> {
            try { ui.addVertex(Integer.parseInt(vertexIdT.getText())); } catch(Exception ex){}
        });
        delVBtn.setOnAction(e -> {
            try { ui.removeVertex(Integer.parseInt(vertexIdT.getText())); } catch(Exception ex){}
        });

        addBtn.setOnAction(e -> {
            try { ui.addEdge(Integer.parseInt(fromT.getText()), Integer.parseInt(toT.getText()), Integer.parseInt(wT.getText())); } 
            catch(Exception ex){} 
        });
        delBtn.setOnAction(e -> {
            try { ui.removeEdge(Integer.parseInt(fromT.getText()), Integer.parseInt(toT.getText())); } 
            catch(Exception ex){} 
        });
        
        matrixClearBtn.setOnAction(ev -> ui.resetToDefault());
        matrixRandomBtn.setOnAction(ev -> ui.generateRandomGraph());
        matrixSaveBtn.setOnAction(ev -> ui.saveGraph());
        matrixLoadBtn.setOnAction(ev -> ui.loadGraph());
        mRenderDslBtn.setOnAction(ev -> ui.renderFromDSL(mDslArea.getText()));
        
        dijBtn.setOnAction(e -> ui.performDijkstra(startT.getText(), endT.getText()));

        pane.getChildren().addAll(
            new Label("顶点管理:"), vertexInputs,
            new Label("边管理:"), edgeInputs, 
            new Separator(), new Label("图管理:"), mManageBtns,
            new Separator(), new Label("DSL:"), mDslArea, mRenderDslBtn,
            new Separator(), new Label("算法:"), algoBox
        );
    }

    private Button createStyledButton(String text, String color) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-radius: 4; -fx-background-radius: 4;");
        
        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: " + darkenColor(color) + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-radius: 4; -fx-background-radius: 4;"));
        button.setOnMouseExited(e -> button.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-radius: 4; -fx-background-radius: 4;"));
        
        return button;
    }
    
    private String darkenColor(String color) {
        switch (color) {
            case "#4caf50": return "#388e3c";
            case "#2196f3": return "#1976d2";
            case "#ff9800": return "#f57c00";
            case "#f44336": return "#d32f2f";
            case "#9c27b0": return "#7b1fa2";
            case "#607d8b": return "#455a64";
            case "#e91e63": return "#c2185b";
            case "#009688": return "#00796b";
            default: return color;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}