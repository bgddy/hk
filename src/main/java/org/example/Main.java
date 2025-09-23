package org.example;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("JavaFX 测试窗口");

        Label label = new Label("还没点击按钮");
        Button btn = new Button("点击我");

        btn.setOnAction(e -> label.setText("按钮被点击了！"));

        VBox root = new VBox(20); // 20 是控件间距
        root.getChildren().addAll(btn, label);
        root.setStyle("-fx-padding: 30; -fx-alignment: center;");

        Scene scene = new Scene(root, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}