import org.example.core.AdjListGraph;
import org.example.ui.AdjListGraphUI;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class TestAdjListUI extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        // 创建邻接表图（5个顶点）
        AdjListGraph graph = new AdjListGraph(5);
        
        // 创建UI
        AdjListGraphUI adjListUI = new AdjListGraphUI(graph);
        
        // 创建场景并显示
        Scene scene = new Scene(adjListUI.getPane(), 1000, 600);
        primaryStage.setTitle("邻接表图可视化测试 - 输入框扩大验证");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        System.out.println("邻接表UI已启动，请检查：");
        System.out.println("1. 起始顶点输入框是否已扩大（宽度180px，高度35px）");
        System.out.println("2. 邻接表显示区域是否使用ScrollPane");
        System.out.println("3. 图管理按钮是否正常工作");
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
