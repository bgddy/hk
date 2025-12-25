package org.example.app;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.example.core.*;
import org.example.ui.*;

import java.util.Random;
import java.util.function.LongSupplier;

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
    
    private SortingRaceUI sortingRaceUI;
    
    private boolean isStabilityTest = false;

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();

        HBox topPane = new HBox(15);
        topPane.setPadding(new Insets(15));
        topPane.setStyle("-fx-background-color: linear-gradient(to right, #e3f2fd, #f3e5f5);");

        leftTopPane = new VBox(12);
        leftTopPane.setPadding(new Insets(15));
        leftTopPane.setPrefWidth(250);
        leftTopPane.setStyle("-fx-background-color: white; -fx-border-color: #bbdefb; -fx-border-radius: 8; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        rightTopPane = new VBox(12);
        rightTopPane.setPadding(new Insets(15));
        rightTopPane.setPrefWidth(600); 
        rightTopPane.setStyle("-fx-background-color: white; -fx-border-color: #bbdefb; -fx-border-radius: 8; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        aiPane = initAIPanel(); 

        topPane.getChildren().addAll(leftTopPane, rightTopPane, aiPane);
        HBox.setHgrow(aiPane, Priority.ALWAYS);

        VBox bottomContainer = new VBox();
        bottomContainer.setPrefHeight(600);
        VBox.setVgrow(bottomContainer, Priority.ALWAYS);
        
        bottomPane = new Pane();
        bottomPane.prefHeightProperty().bind(bottomContainer.heightProperty());
        bottomPane.setStyle("-fx-border-color: #bbdefb; -fx-border-radius: 8; -fx-background-color: linear-gradient(to bottom, #fafafa, #ffffff); -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 8, 0, 0, 3);");
        
        bottomContainer.getChildren().add(bottomPane);

        root.setTop(topPane);
        root.setCenter(bottomContainer);

        typeSelector = new ComboBox<>();
        typeSelector.getItems().addAll(
                "Selection Sort", "Insertion Sort", "Quick Sort",
                "Sorting Race", 
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

        Scene scene = new Scene(root, 1250, 900);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Unified Algorithm & Graph Visualization + AI");
        primaryStage.show();
    }

    private VBox initAIPanel() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(15));
        box.setStyle("-fx-background-color: #f0f8ff; -fx-border-color: #90caf9; -fx-border-radius: 8; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        Label title = new Label("🤖 AI 智能助手 (DeepSeek)");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1565c0;");

        TextArea promptInput = new TextArea();
        promptInput.setPromptText("在此输入：\n1. 绘图指令 (如: '画个五角星')\n2. 提问 (如: '这个图有环吗？')");
        promptInput.setWrapText(true);
        promptInput.setPrefHeight(60);

        HBox buttonBox = new HBox(8);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        Button drawBtn = createStyledButton("🎨 执行绘图", "#4caf50");
        Button askBtn = createStyledButton("💬 咨询教授", "#2196f3");
        Button analyzeBtn = createStyledButton("🧠 一键分析", "#9c27b0");

        drawBtn.setMaxWidth(Double.MAX_VALUE);
        askBtn.setMaxWidth(Double.MAX_VALUE);
        analyzeBtn.setMaxWidth(Double.MAX_VALUE);
        
        buttonBox.getChildren().addAll(drawBtn, askBtn, analyzeBtn);
        HBox.setHgrow(drawBtn, Priority.ALWAYS);
        HBox.setHgrow(askBtn, Priority.ALWAYS);
        HBox.setHgrow(analyzeBtn, Priority.ALWAYS);

        Label responseLabel = new Label("AI 回复:");
        responseLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #555;");
        
        TextArea responseArea = new TextArea();
        responseArea.setPromptText("AI 的分析结果将显示在这里...");
        responseArea.setEditable(false);
        responseArea.setWrapText(true);
        responseArea.setPrefHeight(120); 
        responseArea.setStyle("-fx-control-inner-background: #eef; -fx-font-family: 'Microsoft YaHei', monospace; -fx-font-size: 12px;");

        Label statusLabel = new Label("准备就绪");
        statusLabel.setStyle("-fx-text-fill: #757575; -fx-font-size: 11px;");

        drawBtn.setOnAction(e -> {
            String input = promptInput.getText();
            if (input.isEmpty()) return;
            handleAIRequest(input, true, statusLabel, responseArea);
        });

        askBtn.setOnAction(e -> {
            String input = promptInput.getText();
            if (input.isEmpty()) {
                statusLabel.setText("⚠️ 请先输入问题"); 
                return;
            }
            handleAIRequest(input, false, statusLabel, responseArea);
        });

        analyzeBtn.setOnAction(e -> {
            promptInput.setText("请详细分析当前图的结构特点、连通性以及适合的算法。");
            handleAIRequest("请详细分析当前图的结构特点、连通性以及适合的算法。", false, statusLabel, responseArea);
        });

        box.getChildren().addAll(title, promptInput, buttonBox, statusLabel, responseLabel, responseArea);
        return box;
    }

    private void handleAIRequest(String input, boolean isDrawing, Label statusLabel, TextArea responseArea) {
        String mode = typeSelector.getValue();
        if (mode == null || (!mode.contains("Adjacency") && !mode.contains("Matrix"))) {
            statusLabel.setText("⚠️ 请先选择图论模式");
            statusLabel.setTextFill(Color.RED);
            return;
        }

        statusLabel.setText(isDrawing ? "🎨 AI 正在绘图..." : "🧠 AI 教授正在思考...");
        statusLabel.setTextFill(Color.BLUE);
        responseArea.setText("");

        String currentGraphDSL = "";
        if (mode.equals("Adjacency List") && adjGraphUI != null) {
            currentGraphDSL = adjGraphUI.getGraphDSL();
        } else if (mode.equals("Adjacency Matrix") && matrixGraphUI != null) {
            currentGraphDSL = matrixGraphUI.getGraphDSL();
        }

        if (isDrawing) {
            llmService.generateDSL(input, currentGraphDSL).thenAccept(response -> 
                Platform.runLater(() -> processAIResponse(response, statusLabel, responseArea, true))
            );
        } else {
            llmService.chatWithGraph(input, currentGraphDSL).thenAccept(response -> 
                Platform.runLater(() -> processAIResponse(response, statusLabel, responseArea, false))
            );
        }
    }

    private void processAIResponse(String response, Label statusLabel, TextArea responseArea, boolean isDrawingMode) {
        if (isDrawingMode && response.startsWith("[DSL]")) {
            String dslContent = response.replace("[DSL]", "").trim();
            responseArea.setText("✅ 执行绘图指令:\n" + dslContent);
            applyDSL(dslContent);
            statusLabel.setText("✅ 图形已更新");
            statusLabel.setTextFill(Color.GREEN);
        } else {
            String msgContent = response.replace("[MSG]", "").trim();
            responseArea.setText(msgContent);
            statusLabel.setText("💬 回复完毕");
            statusLabel.setTextFill(Color.GRAY);
        }
    }

    private void applyDSL(String dsl) {
        String type = typeSelector.getValue();
        System.out.println("AI Generated DSL:\n" + dsl);

        if (type.equals("Adjacency List")) {
            adjGraphUI.renderFromDSL(dsl);
        } else if (type.equals("Adjacency Matrix")) {
            matrixGraphUI.renderFromDSL(dsl);
        }
    }

    private void updateInputArea(String type) {
        rightTopPane.getChildren().clear();
        bottomPane.getChildren().clear();

        if (bottomPane.getParent() instanceof VBox) {
            VBox container = (VBox) bottomPane.getParent();
            container.getChildren().removeIf(node -> node instanceof HBox);
        }

        HBox controlPanel = null;
        
        if (type.contains("Sort") || type.contains("Race")) {
            Button autoPlayBtn = createStyledButton("自动播放", "#4caf50");
            if (type.contains("Race")) autoPlayBtn.setText("开始竞速");

            Button nextStepBtn = createStyledButton("下一步", "#2196f3");
            Button resetBtn = createStyledButton("重置", "#ff9800");
            Button pauseBtn = createStyledButton("暂停", "#f44336");
            
            // === 动画速度滑块 ===
            Slider speedSlider = new Slider(1, 20, 5); // 速度范围: 1x 到 20x
            speedSlider.setPrefWidth(120);
            speedSlider.setShowTickMarks(true);
            speedSlider.setMajorTickUnit(5);
            speedSlider.setBlockIncrement(1);
            speedSlider.setTooltip(new Tooltip("调整动画播放速度 (左慢右快)"));
            
            Label speedLabel = new Label("⚡ 速度:");
            speedLabel.setStyle("-fx-font-weight: bold;");

            // 辅助：计算延迟毫秒数 (基础值 600ms / 倍速)
            LongSupplier calcDelay = () -> (long) (600 / speedSlider.getValue());
            
            // 绑定滑块监听：拖动时实时改变速度
            speedSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                long newDelay = (long) (600 / newVal.doubleValue());
                if (type.equals("Selection Sort") && selectionSortUI != null) {
                    selectionSortUI.setAnimationSpeed(newDelay);
                } else if (type.equals("Insertion Sort") && insertSortUI != null) {
                    insertSortUI.setAnimationSpeed(newDelay);
                } else if (type.equals("Quick Sort") && fastSortUI != null) {
                    fastSortUI.setAnimationSpeed(newDelay);
                }
            });

            controlPanel = new HBox(15);
            controlPanel.setPadding(new Insets(15));
            controlPanel.setStyle("-fx-background-color: linear-gradient(to right, #e8f5e8, #e3f2fd); -fx-border-color: #c8e6c9; -fx-border-radius: 8;");
            controlPanel.setPrefHeight(70);
            
            // 将滑块加入控制面板
            controlPanel.getChildren().addAll(autoPlayBtn, nextStepBtn, pauseBtn, resetBtn, new Separator(), speedLabel, speedSlider);
            controlPanel.setAlignment(Pos.CENTER_LEFT);
            
            if (type.equals("Selection Sort") && selectionSortUI != null) {
                autoPlayBtn.setOnAction(ev -> selectionSortUI.visualizeSteps(calcDelay.getAsLong()));
                nextStepBtn.setOnAction(ev -> selectionSortUI.nextStep());
                resetBtn.setOnAction(ev -> selectionSortUI.reset());
                pauseBtn.setOnAction(ev -> selectionSortUI.pause());
            } else if (type.equals("Insertion Sort") && insertSortUI != null) {
                autoPlayBtn.setOnAction(ev -> insertSortUI.visualizeSteps(calcDelay.getAsLong()));
                nextStepBtn.setOnAction(ev -> insertSortUI.nextStep());
                resetBtn.setOnAction(ev -> insertSortUI.reset());
                pauseBtn.setOnAction(ev -> insertSortUI.pause());
            } else if (type.equals("Quick Sort") && fastSortUI != null) {
                autoPlayBtn.setOnAction(ev -> fastSortUI.visualizeSteps(calcDelay.getAsLong()));
                nextStepBtn.setOnAction(ev -> fastSortUI.nextStep());
                resetBtn.setOnAction(ev -> fastSortUI.reset());
                pauseBtn.setOnAction(ev -> fastSortUI.pause());
            } else if (type.equals("Sorting Race")) {
                if (sortingRaceUI == null) sortingRaceUI = new SortingRaceUI();
                
                Button newDataBtn = createStyledButton("换一组数据", "#9c27b0");
                newDataBtn.setOnAction(ev -> sortingRaceUI.generateNewData(100));
                controlPanel.getChildren().add(1, newDataBtn); // 插在 NextStep 前面

                autoPlayBtn.setOnAction(ev -> sortingRaceUI.startRace());
                nextStepBtn.setOnAction(ev -> sortingRaceUI.nextStep());
                resetBtn.setOnAction(ev -> sortingRaceUI.resetRace());
                pauseBtn.setOnAction(ev -> sortingRaceUI.pauseRace());
                // 竞速模式通常不使用全局滑块
                speedSlider.setDisable(true); 
            }
        }

        switch (type) {
            case "Selection Sort":
            case "Insertion Sort":
            case "Quick Sort":
                TextField arrayInput = new TextField();
                arrayInput.setPromptText("如: 8,3,5,1,6");
                arrayInput.setPrefWidth(160);

                Button randomBtn = createStyledButton("随机", "#9c27b0");
                randomBtn.setTooltip(new Tooltip("生成5-10个不超过10的随机数"));
                randomBtn.setOnAction(e -> {
                    Random rand = new Random();
                    int count = rand.nextInt(6) + 5; 
                    StringBuilder sb = new StringBuilder();
                    for(int i=0; i<count; i++) {
                        sb.append(rand.nextInt(10) + 1); 
                        if(i < count - 1) sb.append(",");
                    }
                    arrayInput.setText(sb.toString());
                    isStabilityTest = false;
                });

                Button stabilityBtn = createStyledButton("测稳定", "#e91e63");
                stabilityBtn.setTooltip(new Tooltip("加载强效测试数据(5,5,2)，必定触发不稳定现象"));
                stabilityBtn.setOnAction(e -> {
                    arrayInput.setText("5,5,2");
                    isStabilityTest = true;
                });

                Button sortBtn = createStyledButton("开始排序", "#2196f3");
                
                HBox inputBox = new HBox(8);
                inputBox.setAlignment(Pos.CENTER_LEFT);
                inputBox.getChildren().addAll(new Label("输入:"), arrayInput, randomBtn, stabilityBtn, sortBtn);
                
                rightTopPane.getChildren().add(inputBox);

                final HBox finalControlPanel = controlPanel;
                sortBtn.setOnAction(ev -> {
                    String text = arrayInput.getText();
                    if(text == null || text.trim().isEmpty()) return;
                    
                    String[] parts = text.split(",");
                    int[] arr = new int[parts.length];
                    try {
                        for (int i = 0; i < parts.length; i++) arr[i] = Integer.parseInt(parts[i].trim());
                        
                        bottomPane.getChildren().clear();
                        if (type.equals("Selection Sort")) {
                            selectionSortUI = new SelectionSortUI(arr);
                            if(isStabilityTest) selectionSortUI.setStabilityMode(true, arr);
                            selectionSortUI.getRoot().prefWidthProperty().bind(bottomPane.widthProperty());
                            selectionSortUI.getRoot().prefHeightProperty().bind(bottomPane.heightProperty());
                            bottomPane.getChildren().add(selectionSortUI.getRoot());
                        } else if (type.equals("Insertion Sort")) {
                            insertSortUI = new InsertSortUI(arr);
                            if(isStabilityTest) insertSortUI.setStabilityMode(true, arr);
                            insertSortUI.getRoot().prefWidthProperty().bind(bottomPane.widthProperty());
                            insertSortUI.getRoot().prefHeightProperty().bind(bottomPane.heightProperty());
                            bottomPane.getChildren().add(insertSortUI.getRoot());
                        } else {
                            fastSortUI = new FastSortUI(arr);
                            if(isStabilityTest) fastSortUI.setStabilityMode(true, arr);
                            fastSortUI.getRoot().prefWidthProperty().bind(bottomPane.widthProperty());
                            fastSortUI.getRoot().prefHeightProperty().bind(bottomPane.heightProperty());
                            bottomPane.getChildren().add(fastSortUI.getRoot());
                        }
                        VBox container = (VBox) bottomPane.getParent();
                        container.getChildren().removeIf(node -> node instanceof HBox);
                        container.getChildren().add(finalControlPanel);
                    } catch (Exception ex) {
                        System.out.println("输入解析错误");
                        new Alert(Alert.AlertType.ERROR, "请输入有效的整数序列，用逗号分隔！").show();
                    }
                    isStabilityTest = false;
                });
                break;
            
            case "Sorting Race":
                if (sortingRaceUI == null) sortingRaceUI = new SortingRaceUI();
                bottomPane.getChildren().clear();
                HBox raceRoot = sortingRaceUI.getRoot(); 
                raceRoot.prefWidthProperty().bind(bottomPane.widthProperty());
                raceRoot.prefHeightProperty().bind(bottomPane.heightProperty());
                bottomPane.getChildren().add(raceRoot);
                rightTopPane.getChildren().clear(); 
                rightTopPane.getChildren().add(new Label("🏆 算法竞速模式 - 实时监控中"));
                VBox container = (VBox) bottomPane.getParent();
                container.getChildren().removeIf(node -> node instanceof HBox);
                container.getChildren().add(controlPanel);
                break;
                
            case "Adjacency List":
                buildGraphControlPanel("邻接表", adjGraphUI, rightTopPane);
                adjGraphUI.getPane().prefWidthProperty().bind(bottomPane.widthProperty());
                adjGraphUI.getPane().prefHeightProperty().bind(bottomPane.heightProperty());
                bottomPane.getChildren().add(adjGraphUI.getPane());
                break;
                
            case "Adjacency Matrix":
                buildMatrixControlPanel("邻接矩阵", matrixGraphUI, rightTopPane);
                matrixGraphUI.getPane().prefWidthProperty().bind(bottomPane.widthProperty());
                matrixGraphUI.getPane().prefHeightProperty().bind(bottomPane.heightProperty());
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
        
        Button bfsBtn = createStyledButton("BFS" , "#4caf50");
        Button dfsBtn = createStyledButton("DFS", "#2196f3");
        
        Button kruskalBtn = createStyledButton("Kruskal", "#ff9800");
        Button primBtn = createStyledButton("Prim", "#ff5722");
        
        algoBox.getChildren().addAll(startT, endT, bfsBtn, dfsBtn);
        
        HBox algoBox2 = new HBox(5);
        Button dijBtn = createStyledButton("Dijkstra(单)", "#e91e63");
        Button dijAllBtn = createStyledButton("Dijkstra(全)", "#c2185b");
        Button aStarBtn = createStyledButton("A* Search", "#9c27b0"); 
        
        algoBox2.getChildren().addAll(kruskalBtn, primBtn, dijBtn, aStarBtn); 
        
        HBox algoBox3 = new HBox(5);
        algoBox3.getChildren().add(dijAllBtn);
        
        // === [新增] 割点分析按钮 (添加在 algoBox3) ===
        Button apBtn = createStyledButton("寻找割点", "#d32f2f"); // 红色警示风格，非常醒目
        apBtn.setTooltip(new Tooltip("寻找无向图中的关键节点（Articulation Points）"));
        apBtn.setOnAction(e -> ui.performArticulationPoints());
        
        algoBox3.getChildren().add(apBtn); // 加入到 Dijkstra(全) 旁边

        Label controlLabel = new Label("动画控制:");
        controlLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
        HBox controlBox = new HBox(5);
        Button playBtn = createStyledButton("播放", "#4caf50");
        Button pauseBtn = createStyledButton("暂停", "#ff9800");
        Button nextBtn = createStyledButton("下一步", "#2196f3");
        Button resetBtn = createStyledButton("重置", "#f44336");
        controlBox.getChildren().addAll(playBtn, pauseBtn, nextBtn, resetBtn);

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
        
        kruskalBtn.setOnAction(e -> ui.performMST());
        primBtn.setOnAction(e -> ui.performPrim());
        
        dijBtn.setOnAction(e -> ui.performDijkstra(startT.getText(), endT.getText()));
        dijAllBtn.setOnAction(e -> ui.performDijkstraAll(startT.getText()));

        aStarBtn.setOnAction(e -> ui.performAStar(startT.getText(), endT.getText()));

        playBtn.setOnAction(e -> ui.play());
        pauseBtn.setOnAction(e -> ui.pause());
        nextBtn.setOnAction(e -> ui.nextStep());
        resetBtn.setOnAction(e -> ui.resetAnimation());

        pane.getChildren().addAll(
            new Label("边管理:"), edgeInputs, 
            new Separator(), graphManageLabel, graphManagementButtons,
            new Separator(), dslLabel, dslArea, renderDslBtn,
            new Separator(), algoLabel, algoBox, algoBox2, algoBox3,
            new Separator(), controlLabel, controlBox 
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
        
        Button dijBtn = createStyledButton("最短路(单)", "#e91e63");
        Button dijAllBtn = createStyledButton("最短路(全)", "#c2185b");
        
        algoBox.getChildren().addAll(startT, endT, dijBtn, dijAllBtn);

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
        dijAllBtn.setOnAction(e -> ui.performDijkstraAll(startT.getText()));

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
            case "#ff5722": return "#e64a19";
            default: return color;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}