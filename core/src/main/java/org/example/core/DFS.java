package org.example.core;
import java.util.ArrayList;
import java.util.List;

public class DFS {
    private boolean[] visited;
    private  AdjListGraph graph;
    private List<Integer> traversalOrder;

    public DFS(AdjListGraph graph)
    {
        this.graph = graph;
        visited = new boolean[graph.verticesNumber()];
        traversalOrder = new ArrayList<>();
    }

    public void traverseGraph()
    {
        traversalOrder.clear();
        for(int i = 0; i< visited.length; i++)
        {
            visited[i] = false;
        }
        for(int i = 0;i < graph.verticesNumber(); i++)
        {
            if(visited[i] == false)
            {
                deepFirstSearch(i);
            }
        }
    }
    
    public void traverseFromVertex(int startVertex)
    {
        traversalOrder.clear();
        for(int i = 0; i< visited.length; i++)
        {
            visited[i] = false;
        }
        if (startVertex >= 0 && startVertex < graph.verticesNumber()) {
            deepFirstSearch(startVertex);
        }
    }

    public void deepFirstSearch(int v)
    {
        visited[v] = true;
        visit(v); // 访问节点
        for(Edge e = graph.firstEdge(v);  e != null; e = graph.nextEdge(e))
        {
            if(visited[graph.toVertex(e)] == false)
            {
                deepFirstSearch(graph.toVertex(e));
            }
        }
    }
    
    private void visit(int v) {
        System.out.print(v + " ");
        traversalOrder.add(v);
    }
    
    public List<Integer> getTraversalOrder() {
        return new ArrayList<>(traversalOrder);
    }
    
    public String getTraversalResult() {
        StringBuilder sb = new StringBuilder();
        sb.append("DFS遍历顺序: ");
        for (int i = 0; i < traversalOrder.size(); i++) {
            sb.append(traversalOrder.get(i));
            if (i < traversalOrder.size() - 1) {
                sb.append(" → ");
            }
        }
        return sb.toString();
    }
}
