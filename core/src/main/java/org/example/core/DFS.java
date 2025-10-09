package org.example.core;

public class DFS {
    private boolean[] visited;
    private  AdjListGraph graph;

    public DFS(AdjListGraph graph)
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
                deepFirstSearch(i);
            }
        }
    }

    public void deepFirstSearch(int v)
    {
        visited[v] = true;
        for(Edge e = graph.firstEdge(v);  e != null; e = graph.nextEdge(e))
        {
            if(visited[graph.toVertex(e)] == false)
            {
                deepFirstSearch(graph.toVertex(e));
            }
        }
    }
}
