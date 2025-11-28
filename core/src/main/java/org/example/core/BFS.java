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
         steps.add(new TraversalStep(TraversalStep.Type.VISIT, startV, 0));
         
         while(!queue.isEmpty()) {
             int u = queue.poll();
             traversalOrder.add(u);
             
             steps.add(new TraversalStep(TraversalStep.Type.VISIT, u, 2)); 
             
             for(Edge e = graph.firstEdge(u); e != null; e = graph.nextEdge(e)) {
                 
                 int w = graph.toVertex(e);
                 if(!visited[w]) {
                     visited[w] = true;
                     queue.add(w);
                     
                     steps.add(new TraversalStep(TraversalStep.Type.VISIT_EDGE, e, 5));
                 } else {
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