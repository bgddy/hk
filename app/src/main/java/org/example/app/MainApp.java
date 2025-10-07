package org.example.app;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.example.core.AdjListGraph;
import org.example.ui.AdjListGraphUI;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        int numVertex = 5;
        AdjListGraph graph = new AdjListGraph(numVertex);
        AdjListGraphUI graphUI = new AdjListGraphUI(graph);
        graphUI.refresh();

        VBox controlBox = new VBox(5);
        controlBox.setPrefWidth(250);

        TextField fromField = new TextField();
        fromField.setPromptText("from");

        TextField toField = new TextField();
        toField.setPromptText("to");

        TextField weightField = new TextField();
        weightField.setPromptText("weight");

        Button addEdgeBtn = new Button("Add Edge");
        addEdgeBtn.setOnAction(e -> {
            try {
                int from = Integer.parseInt(fromField.getText());
                int to = Integer.parseInt(toField.getText());
                int weight = Integer.parseInt(weightField.getText());
                graph.setEdge(from, to, weight);
                graphUI.refresh();
            } catch (Exception ex) { System.out.println("Invalid input"); }
        });

        Button delEdgeBtn = new Button("Delete Edge");
        delEdgeBtn.setOnAction(e -> {
            try {
                int from = Integer.parseInt(fromField.getText());
                int to = Integer.parseInt(toField.getText());
                graph.delEdge(from, to);
                graphUI.refresh();
            } catch (Exception ex) { System.out.println("Invalid input"); }
        });

        controlBox.getChildren().addAll(new Label("Edge Operations"), fromField, toField, weightField, addEdgeBtn, delEdgeBtn);

        HBox root = new HBox();
        root.getChildren().addAll(controlBox, graphUI.getRoot());

        Scene scene = new Scene(root, 950, 700);
        primaryStage.setTitle("AdjListGraph Interactive Visualization");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}