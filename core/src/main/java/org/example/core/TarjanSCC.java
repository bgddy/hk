package org.example.core;

import java.util.*;

/**
 * Tarjan 算法实现：用于查找有向图的强连通分量 (SCC)。
 * 支持处理节点被“逻辑删除”的情况 (isVertexExists)。
 */
public class TarjanSCC {
    private AdjListGraph graph;
    private int[] dfn;         // 节点的被访问时间戳
    private int[] low;         // 节点能追溯到的最早时间戳
    private boolean[] inStack; // 节点是否在递归栈中
    private Stack<Integer> stack;
    private int timestamp;
    private List<List<Integer>> sccs; // 存储结果：每个 List<Integer> 是一个强连通分量

    public TarjanSCC(AdjListGraph graph) {
        this.graph = graph;
    }

    /**
     * 执行算法，返回所有强连通分量的列表
     */
    public List<List<Integer>> compute() {
        int n = graph.verticesNumber();
        dfn = new int[n];
        low = new int[n];
        inStack = new boolean[n];
        stack = new Stack<>();
        sccs = new ArrayList<>();
        timestamp = 0;
        
        // 初始化 dfn 数组为 -1，表示未访问
        Arrays.fill(dfn, -1); 

        for (int i = 0; i < n; i++) {
            // 关键：只处理存在的节点
            if (graph.isVertexExists(i) && dfn[i] == -1) {
                tarjan(i);
            }
        }
        return sccs;
    }

    private void tarjan(int u) {
        dfn[u] = low[u] = ++timestamp;
        stack.push(u);
        inStack[u] = true;

        // 遍历所有邻居
        for (Edge e = graph.firstEdge(u); e != null; e = graph.nextEdge(e)) {
            int v = e.getMto();
            
            // 如果目标点已被逻辑删除，跳过
            if (!graph.isVertexExists(v)) continue;

            if (dfn[v] == -1) {
                // v 尚未访问，继续递归
                tarjan(v);
                low[u] = Math.min(low[u], low[v]);
            } else if (inStack[v]) {
                // v 在栈中，说明 v 是 u 的祖先（后向边），更新 low
                low[u] = Math.min(low[u], dfn[v]);
            }
        }

        // 如果 u 是 SCC 的根节点
        if (low[u] == dfn[u]) {
            List<Integer> component = new ArrayList<>();
            int v;
            do {
                v = stack.pop();
                inStack[v] = false;
                component.add(v);
            } while (u != v);
            sccs.add(component);
        }
    }
}