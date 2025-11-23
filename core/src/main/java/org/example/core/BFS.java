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
   private List<TraversalStep> steps; 

    public BFS(AdjListGraph graph) {
        this.graph = graph;
        visited = new boolean[graph.verticesNumber()];
        traversalOrder = new ArrayList<>();
        steps = new ArrayList<>();
    }

    public void traverseFromVertex(int startVertex) {
        traversalOrder.clear();
        steps.clear();
        for(int i = 0; i< visited.length; i++) visited[i] = false;
        
        int numVertices = graph.verticesNumber();
        
        if (startVertex >= 0 && startVertex < numVertices) {
            broadFirstSearch(startVertex);
        }
        
        for (int i = 0; i < numVertices; i++) {
            if (!visited[i]) {
                broadFirstSearch(i);
            }
        }
    }

    public void broadFirstSearch(int startV) {
         visited[startV] = true;
         queue.add(startV);
         // 对应伪代码第0行: 入队
         steps.add(new TraversalStep(TraversalStep.Type.VISIT, startV, 0));
         
         while(!queue.isEmpty()) {
             // 对应伪代码第1行: while loop check
             // (可选：如果想让循环判断也高亮，可以加一步，但通常省略以免闪烁太快)
             
             int u = queue.poll();
             traversalOrder.add(u);
             
             // 对应伪代码第2行: u = dequeue
             steps.add(new TraversalStep(TraversalStep.Type.VISIT, u, 2)); 
             
             for(Edge e = graph.firstEdge(u); e != null; e = graph.nextEdge(e)) {
                 // 对应伪代码第3行: for each neighbor
                 // 我们可以用 VISIT_EDGE 的一种变体来高亮这行，这里简单复用 VISIT_EDGE
                 
                 int w = graph.toVertex(e);
                 if(!visited[w]) {
                     visited[w] = true;
                     queue.add(w);
                     
                     // 对应伪代码第5行: 发现新节点，标记并入队 (伴随边的动画)
                     steps.add(new TraversalStep(TraversalStep.Type.VISIT_EDGE, e, 5));
                 } else {
                     // 对应伪代码第4行: if not visited (检查了但已访问)
                     // 可选记录，为了动画流畅性，这里通常不记录"未命中"的情况
                 }
             }
         }
    }
    
    public List<Integer> getTraversalOrder() { return new ArrayList<>(traversalOrder); }
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