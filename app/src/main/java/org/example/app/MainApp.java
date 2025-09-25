package org.example.app;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.core.SelectionSort;
import org.example.ui.SelectionSortUI;
import org.example.core.InsertSort;
import org.example.ui.InsertsortUI;


import java.util.Optional;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {

        Button sortButton = new Button("排序算法可视化");

        sortButton.setOnAction(e -> {
            // 弹出输入对话框
            TextInputDialog dialog = new TextInputDialog("5,2,9,1,6");
            dialog.setTitle("输入数组");
            dialog.setHeaderText("请输入整数数组，用逗号分隔，例如：5,2,9,1,6");
            dialog.setContentText("数组：");

            Optional<String> result = dialog.showAndWait();
            int[] data;
            if (result.isPresent()) {
                String input = result.get();
                String[] parts = input.split(",");
                data = new int[parts.length];
                for (int i = 0; i < parts.length; i++) {
                    data[i] = Integer.parseInt(parts[i].trim());
                }
            } else {
                data = new int[]{5,2,9,1,6}; // 默认数组
            }

            // 调用 core 模块算法
            SelectionSort sorter = new SelectionSort();
            int[][] steps = sorter.sort(data);

            // 调用 ui 模块显示
            SelectionSortUI ui = new SelectionSortUI(data);
            Stage sortStage = new Stage();
            sortStage.setTitle("选择排序可视化");
            sortStage.setScene(new Scene(ui.getRoot(), 600, 400));
            sortStage.show();

            ui.visualizeSteps(steps, 1000); // 每步 1 秒
        });

        VBox root = new VBox(10, sortButton);
        primaryStage.setTitle("数据结构可视化入口");
        primaryStage.setScene(new Scene(root, 300, 100));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);


    }
}