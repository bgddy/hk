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

    public BFS(AdjListGraph graph)
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
                broadFirstSearch(i);
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
            broadFirstSearch(startVertex);
        }
    }

    public void broadFirstSearch(int v)
    {
         visited[v] = true;
         queue.add(v);
         while(!queue.isEmpty())
         {
             int u = queue.poll();
             visit(u);
             for(Edge e = graph.firstEdge(u);e != null; e = graph.nextEdge(e))
             {
                 if(visited[graph.toVertex(e)] == false)
                 {
                     visited[graph.toVertex(e)] = true;
                     queue.add(graph.toVertex(e));
                 }
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
        sb.append("BFS遍历顺序: ");
        for (int i = 0; i < traversalOrder.size(); i++) {
            sb.append(traversalOrder.get(i));
            if (i < traversalOrder.size() - 1) {
                sb.append(" → ");
            }
        }
        return sb.toString();
    }
}
