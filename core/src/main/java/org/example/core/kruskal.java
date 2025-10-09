package org.example.core;

import java.util.PriorityQueue;

public class kruskal {

    private AdjListGraph graph;

    public kruskal(AdjListGraph graph) {
        this.graph = graph; // 将图传入算法类
    }

    public Edge[] generateMST() {
        int n = graph.vecticesNumber();
        Edge[] mst = new Edge[n - 1];
        int mstIndex = 0;
        UnionFind uf = new UnionFind(n);
        Edge[] edgeArray = graph.getAllEdge();
        PriorityQueue<Edge> pq = new PriorityQueue<>(edgeArray.length,
                (e1, e2) -> Integer.compare(e1.getMweight(), e2.getMweight()));
        for (Edge e : edgeArray) {
            pq.add(e);
        }

        // 4️⃣ 初始化等价类数量
        int equalNumber = n;

        // 5️⃣ Kruskal 主循环
        while (!pq.isEmpty() && equalNumber > 1) {
            Edge e = pq.poll(); // 取最小边
            int from = e.getMfrom();
            int to = e.getMto();

            if (!uf.isConnected(from, to)) {
                uf.union(from, to);      // 合并集合
                mst[mstIndex++] = e;     // 加入 MST
                equalNumber--;           // 等价类数量减少
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