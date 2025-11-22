package org.example.core;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class BFS {
   private Queue<Integer> queue = new ArrayDeque<>();
   private AdjListGraph graph;
   private boolean[] visited;
   private List<Integer> traversalOrder;
   private List<TraversalStep> steps; // 记录动画步骤

    public BFS(AdjListGraph graph) {
        this.graph = graph;
        visited = new boolean[graph.verticesNumber()];
        traversalOrder = new ArrayList<>();
        steps = new ArrayList<>();
    }

    public void traverseFromVertex(int startVertex) {
        traversalOrder.clear();
        steps.clear();
        // 重置访问状态
        for(int i = 0; i< visited.length; i++) visited[i] = false;
        
        int numVertices = graph.verticesNumber();
        
        // 1. 先从用户指定的起点开始遍历 (如果合法)
        if (startVertex >= 0 && startVertex < numVertices) {
            broadFirstSearch(startVertex);
        }
        
        // 2. [核心修改] 检查是否还有未访问的孤立节点/连通分量
        for (int i = 0; i < numVertices; i++) {
            if (!visited[i]) {
                // 发现新的未访问节点，说明是另一个连通分量
                broadFirstSearch(i);
            }
        }
    }

    public void broadFirstSearch(int startV) {
         visited[startV] = true;
         queue.add(startV);
         
         while(!queue.isEmpty()) {
             int u = queue.poll();
             traversalOrder.add(u);
             
             // 记录: 正式访问 u (变色)
             steps.add(new TraversalStep(TraversalStep.Type.VISIT, u)); 
             
             for(Edge e = graph.firstEdge(u); e != null; e = graph.nextEdge(e)) {
                 int w = graph.toVertex(e);
                 if(!visited[w]) {
                     visited[w] = true;
                     queue.add(w);
                     
                     // 记录: 发现邻居 w，通过边 e (扩散效果: 边变色)
                     steps.add(new TraversalStep(TraversalStep.Type.VISIT_EDGE, e));
                 }
             }
         }
    }
    
    public List<Integer> getTraversalOrder() { return new ArrayList<>(traversalOrder); }
    
    // 获取详细步骤用于动画
    public List<TraversalStep> getSteps() { return new ArrayList<>(steps); }
    
    public String getTraversalResult() {
        StringBuilder sb = new StringBuilder();
        sb.append("BFS遍历顺序: ");
        for (int i = 0; i < traversalOrder.size(); i++) {
            sb.append(traversalOrder.get(i));
            if (i < traversalOrder.size() - 1) sb.append(" -> ");
        }
        return sb.toString();
    }
}