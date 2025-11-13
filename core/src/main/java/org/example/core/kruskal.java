package org.example.core;

import java.util.PriorityQueue;

public class kruskal {

    private AdjListGraph graph;

    public kruskal(AdjListGraph graph) {
        this.graph = graph; // 将图传入算法类
    }

    public Edge[] generateMST() {
        int n = graph.verticesNumber();
        Edge[] mst = new Edge[n - 1];
        int mstIndex = 0;
        UnionFind uf = new UnionFind(n);
        Edge[] edgeArray = graph.getAllEdge();
        
        // 使用更稳定的排序方法，确保相同权重边的处理一致
        java.util.Arrays.sort(edgeArray, (e1, e2) -> {
            int weightCompare = Integer.compare(e1.getMweight(), e2.getMweight());
            if (weightCompare != 0) {
                return weightCompare;
            }
            // 如果权重相同，按顶点编号排序以确保稳定性
            int fromCompare = Integer.compare(e1.getMfrom(), e2.getMfrom());
            if (fromCompare != 0) {
                return fromCompare;
            }
            return Integer.compare(e1.getMto(), e2.getMto());
        });

        // 5️⃣ Kruskal 主循环
        for (Edge e : edgeArray) {
            if (mstIndex == n - 1) break;
            
            int from = e.getMfrom();
            int to = e.getMto();

            if (!uf.isConnected(from, to)) {
                uf.union(from, to);      // 合并集合
                mst[mstIndex++] = e;     // 加入 MST
            }
        }

        // 6️⃣ 检查 MST 是否完整
        if (mstIndex != n - 1) {
            System.err.println("最小生成树不存在！");
            return null;
        }

        return mst;
    }
}
