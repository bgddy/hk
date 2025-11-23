package org.example.core;
import java.util.ArrayList;
import java.util.List;

public class DFS {
    private boolean[] visited;
    private AdjListGraph graph;
    private List<Integer> traversalOrder;
    private List<TraversalStep> steps;

    public DFS(AdjListGraph graph) {
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
            deepFirstSearch(startVertex);
        }
        
        for (int i = 0; i < numVertices; i++) {
            if (!visited[i]) {
                deepFirstSearch(i);
            }
        }
    }

    private void deepFirstSearch(int v) {
        // 对应伪代码第0行: 函数入口
        steps.add(new TraversalStep(TraversalStep.Type.VISIT, v, 0));

        visited[v] = true;
        traversalOrder.add(v);
        
        // 对应伪代码第1,2行: 标记并处理
        steps.add(new TraversalStep(TraversalStep.Type.VISIT, v, 2));
        
        for(Edge e = graph.firstEdge(v); e != null; e = graph.nextEdge(e)) {
            int w = graph.toVertex(e);
            if(!visited[w]) {
                // 对应伪代码第4,5行: 发现未访问邻居，准备递归 (边变色)
                steps.add(new TraversalStep(TraversalStep.Type.VISIT_EDGE, e, 5));
                
                deepFirstSearch(w);
                
                // 递归返回，对应伪代码第6行: 回溯
                steps.add(new TraversalStep(TraversalStep.Type.BACKTRACK, v, 6));
            }
        }
    }
    
    public List<Integer> getTraversalOrder() { return new ArrayList<>(traversalOrder); }
    public List<TraversalStep> getSteps() { return new ArrayList<>(steps); }
    
    public String getTraversalResult() {
        StringBuilder sb = new StringBuilder();
        sb.append("DFS遍历顺序: ");
        for (int i = 0; i < traversalOrder.size(); i++) {
            sb.append(traversalOrder.get(i));
            if (i < traversalOrder.size() - 1) sb.append(" -> ");
        }
        return sb.toString();
    }
}