package org.example;

import javafx.animation.FillTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Main extends Application {

    private static final int ARRAY_SIZE = 10;
    private static final int RECT_WIDTH = 40;
    private static final int RECT_MAX_HEIGHT = 200;

    private int[] array = {50, 150, 100, 80, 120, 60, 30, 90, 110, 70};
    private Rectangle[] rects = new Rectangle[ARRAY_SIZE];

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("冒泡排序可视化");

        Pane pane = new Pane();
        pane.setPrefSize(ARRAY_SIZE * RECT_WIDTH + 50, RECT_MAX_HEIGHT + 100);

        // 创建矩形条
        for (int i = 0; i < ARRAY_SIZE; i++) {
            Rectangle rect = new Rectangle(RECT_WIDTH - 5, array[i]);
            rect.setFill(Color.BLUE);
            rect.setX(i * RECT_WIDTH + 20);
            rect.setY(RECT_MAX_HEIGHT - array[i]);
            rects[i] = rect;
            pane.getChildren().add(rect);
        }

        Button sortButton = new Button("开始排序");
        sortButton.setLayoutX(20);
        sortButton.setLayoutY(RECT_MAX_HEIGHT + 20);

        sortButton.setOnAction(e -> bubbleSortAnimation());

        pane.getChildren().add(sortButton);

        Scene scene = new Scene(pane);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void bubbleSortAnimation() {
        SequentialTransition seq = new SequentialTransition();

        // 冒泡排序动画
        for (int i = 0; i < ARRAY_SIZE - 1; i++) {
            for (int j = 0; j < ARRAY_SIZE - 1 - i; j++) {
                if (array[j] > array[j + 1]) {
                    int a = array[j];
                    array[j] = array[j + 1];
                    array[j + 1] = a;

                    Rectangle r1 = rects[j];
                    Rectangle r2 = rects[j + 1];

                    TranslateTransition t1 = new TranslateTransition(Duration.millis(300), r1);
                    TranslateTransition t2 = new TranslateTransition(Duration.millis(300), r2);

                    t1.setByX(RECT_WIDTH);
                    t2.setByX(-RECT_WIDTH);

                    FillTransition f1 = new FillTransition(Duration.millis(300), r1, Color.BLUE, Color.RED);
                    FillTransition f2 = new FillTransition(Duration.millis(300), r2, Color.BLUE, Color.RED);

                    FillTransition f1Back = new FillTransition(Duration.millis(300), r1, Color.RED, Color.BLUE);
                    FillTransition f2Back = new FillTransition(Duration.millis(300), r2, Color.RED, Color.BLUE);

                    seq.getChildren().addAll(f1, f2, t1, t2, f1Back, f2Back);

                    // 交换数组位置对应矩形
                    rects[j] = r2;
                    rects[j + 1] = r1;
                }
            }
        }

        seq.play();
    }

    public static void main(String[] args) {
        launch(args);
    }
}