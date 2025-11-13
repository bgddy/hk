import org.example.core.AdjListGraph;
import org.example.core.Edge;

public class CheckCurrentGraph {
    public static void main(String[] args) {
        // 创建与应用程序相同的图
        AdjListGraph graph = new AdjListGraph(5);
        // 添加与应用程序相同的默认边
        graph.setEdge(0, 1, 4);
        graph.setEdge(0, 2, 2);
        graph.setEdge(1, 2, 1);
        graph.setEdge(1, 3, 5);
        graph.setEdge(2, 3, 8);
        graph.setEdge(2, 4, 10);
        graph.setEdge(3, 4, 2);
        
        System.out.println("当前图的邻接表:");
        System.out.println(graph.getAdjListString());
        
        System.out.println("\n所有边:");
        Edge[] allEdges = graph.getAllEdge();
        System.out.println("边数: " + allEdges.length);
        for (Edge edge : allEdges) {
            System.out.println(edge.getMfrom() + " - " + edge.getMto() + " (权重: " + edge.getMweight() + ")");
        }
        
        System.out.println("\n顶点数: " + graph.verticesNumber());
        System.out.println("最小生成树需要的边数: " + (graph.verticesNumber() - 1));
        
        if (allEdges.length < graph.verticesNumber() - 1) {
            System.out.println("\n⚠️ 警告: 边数不足，无法形成完整的最小生成树！");
            System.out.println("需要至少 " + (graph.verticesNumber() - 1) + " 条边来连接所有顶点。");
        }
    }
}
