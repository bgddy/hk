package org.example.app;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.core.FastSort;
import org.example.core.InsertSort;
import org.example.core.QuickSortStep;
import org.example.core.SelectionSort;
import org.example.ui.FastSortUI;
import org.example.ui.InsertSortUI;
import org.example.ui.SelectionSortUI;

import java.util.Optional;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        Button selSortBtn = new Button("选择排序");
        Button insSortBtn = new Button("插入排序");
        Button fastSortBtn = new Button("快速排序");

        // 选择排序
        selSortBtn.setOnAction(e -> {
            int[] data = getInputArray();
            SelectionSort sorter = new SelectionSort();
            int[][] steps = sorter.sort(data);

            SelectionSortUI ui = new SelectionSortUI(data);
            Stage stage = new Stage();
            stage.setTitle("选择排序可视化");
            stage.setScene(new Scene(ui.getRoot(), 800, 400));
            stage.show();

            ui.visualizeSteps(steps, 600);
        });

        // 插入排序
        insSortBtn.setOnAction(e -> {
            int[] data = getInputArray();
            InsertSort sorter = new InsertSort();
            int[][] steps = sorter.sort(data);

            InsertSortUI ui = new InsertSortUI(data);
            Stage stage = new Stage();
            stage.setTitle("插入排序可视化");
            stage.setScene(new Scene(ui.getRoot(), 800, 400));
            stage.show();

            ui.visualizeSteps(steps, 600);
        });

        // 快速排序
        fastSortBtn.setOnAction(e -> {
            int[] data = getInputArray();
            FastSort sorter = new FastSort();
            QuickSortStep[] steps = sorter.sort(data);

            FastSortUI ui = new FastSortUI(data);
            Stage stage = new Stage();
            stage.setTitle("快速排序可视化");
            stage.setScene(new Scene(ui.getRoot(), 900, 400));
            stage.show();

            ui.visualizeSteps(steps, 600);
        });

        VBox root = new VBox(10, selSortBtn, insSortBtn, fastSortBtn);
        primaryStage.setScene(new Scene(root, 1500, 750));
        primaryStage.setTitle("数据结构可视化");
        primaryStage.show();
    }

    private int[] getInputArray() {
        TextInputDialog dialog = new TextInputDialog("10,3,7,1,9,5,2,8,4,6");
        dialog.setTitle("输入数组");
        dialog.setHeaderText("请输入整数数组，用逗号分隔");
        dialog.setContentText("数组：");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String[] parts = result.get().split(",");
            int[] data = new int[parts.length];
            for (int i = 0; i < parts.length; i++) data[i] = Integer.parseInt(parts[i].trim());
            return data;
        }
        return new int[]{10,3,7,1,9,5,2,8,4,6};
    }

    public static void main(String[] args) {
        launch(args);
    }
}