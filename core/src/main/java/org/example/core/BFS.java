package org.example.core;
import java.util.ArrayDeque;
import java.util.Queue;

public class BFS {
   private Queue<Integer> queue = new ArrayDeque<>();
   private AdjListGraph graph;
   private boolean[] visited;

    public BFS(AdjListGraph graph)
    {
        this.graph = graph;
        visited = new boolean[graph.vecticesNumber()];
    }

    public void traverseGraph()
    {
        for(int i = 0; i< visited.length; i++)
        {
            visited[i] = false;
        }
        for(int i = 0;i < graph.vecticesNumber(); i++)
        {
            if(visited[i] == false)
            {
                broadFirstSearch(i);
            }
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
             for(Edge e = graph.firstEdge(u); graph.isEdge(e); e = graph.nextEdge(e))
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
    }
}
