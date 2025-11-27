package org.example.core;

import java.util.*;

public class prim {
    private AdjListGraph graph;
    private List<TraversalStep> steps;

    public prim(AdjListGraph graph) {
        this.graph = graph;
        this.steps = new ArrayList<>();
    }

    public Edge[] generateMST() {
        steps.clear();
        int n = graph.verticesNumber();
        if (n == 0) return new Edge[0];

        boolean[] visited = new boolean[n];
        int[] dist = new int[n];
        int[] parent = new int[n]; // 记录父节点以便构建MST边
        
        Arrays.fill(dist, Integer.MAX_VALUE);
        Arrays.fill(parent, -1);

        // 默认从 0 号顶点开始
        int startNode = 0;
        dist[startNode] = 0;
        
        // 优先队列存储 [节点ID, 距离]
        PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> Integer.compare(a[1], b[1]));
        pq.offer(new int[]{startNode, 0});

        List<Edge> mstEdges = new ArrayList<>();
        
        // 对应伪代码第0行: 初始化
        steps.add(new TraversalStep(TraversalStep.Type.VISIT, startNode, 0));

        while (!pq.isEmpty()) {
            int[] curr = pq.poll();
            int u = curr[0];

            // 对应伪代码第1行: 取出最小权值节点
            steps.add(new TraversalStep(TraversalStep.Type.VISIT, u, 1));

            if (visited[u]) continue;
            visited[u] = true;

            // 如果有父节点，说明找到了一条MST边，将其加入结果
            if (parent[u] != -1) {
                Edge e = findEdge(parent[u], u);
                if (e != null) {
                    mstEdges.add(e);
                    // 对应伪代码第2行: 确认加入 MST
                    steps.add(new TraversalStep(TraversalStep.Type.ADD_EDGE, e, 2));
                }
            }

            // 遍历邻居
            for (Edge e = graph.firstEdge(u); e != null; e = graph.nextEdge(e)) {
                int v = e.getMto();
                int w = e.getMweight();
                
                if (visited[v]) continue;

                // 对应伪代码第3行: 检查邻边
                steps.add(new TraversalStep(TraversalStep.Type.CHECK_EDGE, e, 3));

                if (w < dist[v]) {
                    dist[v] = w;
                    parent[v] = u;
                    pq.offer(new int[]{v, w});
                    // 对应伪代码第4行: 更新距离 (此处虽未显式变色，但在逻辑上已更新)
                } else {
                    // 对应伪代码第5行: 忽略 (距离更长或已访问)
                    steps.add(new TraversalStep(TraversalStep.Type.REJECT_EDGE, e, 5));
                }
            }
        }
        
        return mstEdges.toArray(new Edge[0]);
    }

    // 辅助方法：在图中查找连接 u 和 v 的边对象
    private Edge findEdge(int u, int v) {
        for (Edge e = graph.firstEdge(u); e != null; e = graph.nextEdge(e)) {
            if (e.getMto() == v) return e;
        }
        return null;
    }

    public List<TraversalStep> getSteps() {
        return steps;
    }
}