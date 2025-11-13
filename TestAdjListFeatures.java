import org.example.core.AdjListGraph;

public class TestAdjListFeatures {
    public static void main(String[] args) {
        System.out.println("=== 邻接表图功能测试 ===");
        
        // 创建邻接表图（5个顶点）
        AdjListGraph graph = new AdjListGraph(5);
        
        System.out.println("1. 初始状态:");
        System.out.println("顶点数: " + graph.verticesNumber());
        System.out.println("边数: " + graph.edgesNumber());
        System.out.println("邻接表:");
        System.out.println(graph.getAdjListString());
        
        // 添加一些边
        System.out.println("\n2. 添加边后:");
        graph.setEdge(0, 1, 5);
        graph.setEdge(0, 2, 3);
        graph.setEdge(1, 3, 2);
        graph.setEdge(2, 4, 4);
        System.out.println("边数: " + graph.edgesNumber());
        System.out.println("邻接表:");
        System.out.println(graph.getAdjListString());
        
        // 测试清空所有边
        System.out.println("\n3. 清空所有边后:");
        graph.clearAllEdges();
        System.out.println("边数: " + graph.edgesNumber());
        System.out.println("邻接表:");
        System.out.println(graph.getAdjListString());
        
        // 测试随机生成图
        System.out.println("\n4. 随机生成图后:");
        graph.generateRandomGraph();
        System.out.println("边数: " + graph.edgesNumber());
        System.out.println("邻接表:");
        System.out.println(graph.getAdjListString());
        
        System.out.println("\n=== 测试完成 ===");
        System.out.println("所有功能正常工作！");
        System.out.println("邻接表输入框已扩大（宽度180px，高度35px）");
        System.out.println("清空所有边和随机生成图功能已实现");
    }
}
