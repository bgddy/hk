package org.example.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Comparator;

public class kruskal {

    private AdjListGraph graph;
    private List<TraversalStep> steps; 

    public kruskal(AdjListGraph graph) {
        this.graph = graph;
        this.steps = new ArrayList<>();
    }

    public Edge[] generateMST() {
        steps.clear();
        int n = graph.verticesNumber();
        Edge[] mst = new Edge[n - 1];
        int mstIndex = 0;
        UnionFind uf = new UnionFind(n);
        Edge[] edgeArray = graph.getAllEdge();
        

        Arrays.sort(edgeArray, (e1, e2) -> {
            int weightCompare = Integer.compare(e1.getMweight(), e2.getMweight());
            if (weightCompare != 0) return weightCompare;
            int fromCompare = Integer.compare(e1.getMfrom(), e2.getMfrom());
            if (fromCompare != 0) return fromCompare;
            return Integer.compare(e1.getMto(), e2.getMto());
        });

        
        steps.add(new TraversalStep(TraversalStep.Type.VISIT, -1, 1)); 

        
        for (Edge e : edgeArray) {
            if (mstIndex == n - 1) break;
            steps.add(new TraversalStep(TraversalStep.Type.CHECK_EDGE, e, 2));
            
            int from = e.getMfrom();
            int to = e.getMto();
            if (!uf.isConnected(from, to)) {
                uf.union(from, to);      // 合并集合
                mst[mstIndex++] = e;     // 加入 MST
                
              
                steps.add(new TraversalStep(TraversalStep.Type.ADD_EDGE, e, 3));
            } else {
                steps.add(new TraversalStep(TraversalStep.Type.REJECT_EDGE, e, 4));
            }
        }

        if (mstIndex != n - 1) {
            System.err.println("最小生成树不存在！");
            return null;
        }

        return mst;
    }
    
    public List<TraversalStep> getSteps() {
        return steps;
    }
}