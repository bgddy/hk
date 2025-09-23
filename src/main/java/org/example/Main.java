package org.example;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        Button btn = new Button("点我!");
        btn.setOnAction(e -> System.out.println("按钮被点击啦！"));

        StackPane root = new StackPane(btn);
        Scene scene = new Scene(root, 400, 300);

        primaryStage.setTitle("JavaFX 测试");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}