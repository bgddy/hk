package org.example.core;

import java.util.*;

/**
 * 寻找无向图的割点 (Articulation Points / Cut Vertices)
 * 核心逻辑：使用 Tarjan 算法思想 (DFS + 时间戳)
 */
public class ArticulationPointFinder {
    private AdjListGraph graph;
    private int[] dfn;         // 节点被访问的时间戳 (discovery time)
    private int[] low;         // 节点能回溯到的最早时间戳 (low-link value)
    private int timestamp;
    private Set<Integer> articulationPoints; // 存储找到的割点 ID

    public ArticulationPointFinder(AdjListGraph graph) {
        this.graph = graph;
    }

    /**
     * 执行算法，返回所有割点的集合
     */
    public Set<Integer> find() {
        int n = graph.verticesNumber();
        dfn = new int[n];
        low = new int[n];
        Arrays.fill(dfn, -1); // 初始化为 -1，表示未访问
        articulationPoints = new HashSet<>();
        timestamp = 0;

        for (int i = 0; i < n; i++) {
            // 只处理存在的点 (支持逻辑删除)
            if (graph.isVertexExists(i) && dfn[i] == -1) {
                // 根节点需要特殊处理
                tarjan(i, -1);
            }
        }
        return articulationPoints;
    }

    /**
     * Tarjan DFS 递归函数
     * @param u 当前节点
     * @param p 父节点 (parent)
     */
    private void tarjan(int u, int p) {
        dfn[u] = low[u] = ++timestamp;
        int children = 0; // 记录子树数量 (用于根节点判断)

        for (Edge e = graph.firstEdge(u); e != null; e = graph.nextEdge(e)) {
            int v = e.getMto();
            
            // 跳过已删除的点
            if (!graph.isVertexExists(v)) continue;
            
            // 无向图：不能走回头路去父节点
            if (v == p) continue; 

            if (dfn[v] == -1) {
                // v 未访问，它是 u 的子节点
                children++;
                tarjan(v, u);
                
                // 回溯时更新 low 值
                low[u] = Math.min(low[u], low[v]);
                
                // === 割点判定 ===
                if (p != -1 && low[v] >= dfn[u]) {
                    articulationPoints.add(u);
                }
            } else {
                // v 已访问，说明这是一条回边 (back-edge)
                low[u] = Math.min(low[u], dfn[v]);
            }
        }

        // === 根节点判定 ===
        // 如果根节点有两个以上独立的子树，去掉它图也会断，所以也是割点
        if (p == -1 && children > 1) {
            articulationPoints.add(u);
        }
    }
}