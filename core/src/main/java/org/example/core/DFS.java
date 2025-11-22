package org.example.core;
import java.util.ArrayList;
import java.util.List;

public class DFS {
    private boolean[] visited;
    private AdjListGraph graph;
    private List<Integer> traversalOrder;
    private List<TraversalStep> steps; // 记录动画步骤

    public DFS(AdjListGraph graph) {
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
            deepFirstSearch(startVertex);
        }
        
        // 2. [核心修改] 检查其他未访问的连通分量
        for (int i = 0; i < numVertices; i++) {
            if (!visited[i]) {
                // 对每个未访问的节点启动新的 DFS
                deepFirstSearch(i);
            }
        }
    }

    private void deepFirstSearch(int v) {
        visited[v] = true;
        traversalOrder.add(v);
        
        // 记录: 访问节点 (变深色)
        steps.add(new TraversalStep(TraversalStep.Type.VISIT, v));
        
        for(Edge e = graph.firstEdge(v); e != null; e = graph.nextEdge(e)) {
            int w = graph.toVertex(e);
            if(!visited[w]) {
                // 记录: 前进到下一层 (边变色)
                steps.add(new TraversalStep(TraversalStep.Type.VISIT_EDGE, e));
                
                deepFirstSearch(w);
                
                // 记录: 递归回溯到 v (节点变浅色/回溯色)
                steps.add(new TraversalStep(TraversalStep.Type.BACKTRACK, v));
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