package org.example.core;

import java.util.*;

public class AStar {

    private List<TraversalStep> steps = new ArrayList<>();

    /**
     * A* 搜索主函数
     * @param graph 图结构
     * @param startId 起点 ID
     * @param endId 终点 ID
     * @param nodePositions 节点坐标映射 (ID -> [x, y])，用于计算启发式距离
     */
    public List<TraversalStep> search(IGraph graph, int startId, int endId, Map<Integer, double[]> nodePositions) {
        steps.clear();
        
        // 1. 优先队列，按照 fScore (f = g + h) 从小到大排序
        // 数组结构: [0]=nodeId, [1]=fScore
        PriorityQueue<double[]> openSet = new PriorityQueue<>(Comparator.comparingDouble(a -> a[1]));
        
        // 2. 距离记录
        Map<Integer, Double> gScore = new HashMap<>(); // g(n): 起点到当前的实际代价
        Map<Integer, Double> fScore = new HashMap<>(); // f(n): 估算总代价
        Map<Integer, Integer> cameFrom = new HashMap<>(); // 路径回溯记录
        Set<Integer> closedSet = new HashSet<>(); // 已处理完的节点

        // 初始化
        gScore.put(startId, 0.0);
        double hStart = heuristic(startId, endId, nodePositions);
        fScore.put(startId, hStart);
        openSet.add(new double[]{startId, hStart});

        addStep(TraversalStep.Type.VISIT, startId, openSet, closedSet, 
                "A* 开始。起点:" + startId + " 终点:" + endId + " 初始h=" + String.format("%.1f", hStart));

        while (!openSet.isEmpty()) {
            // 取出 f 值最小的节点
            double[] current = openSet.poll();
            int u = (int) current[0];

            // 懒删除：如果节点已经在 closedSet 里，说明这是一个旧的、更差的路径记录，跳过
            if (closedSet.contains(u)) continue;

            // 标记为已处理
            closedSet.add(u);
            addStep(TraversalStep.Type.VISIT, u, openSet, closedSet, 
                    "探索节点 " + u + " (当前 f=" + String.format("%.1f", current[1]) + ")");

            // --- 找到终点 ---
            if (u == endId) {
                addStep(TraversalStep.Type.VISIT, u, openSet, closedSet, "找到终点！准备回溯路径。");
                reconstructPath(cameFrom, startId, endId);
                return steps;
            }

            // --- 遍历邻居 (使用 IGraph 接口) ---
            for (Edge e = graph.firstEdge(u); e != null; e = graph.nextEdge(e)) {
                int v = graph.toVertex(e);
                
                if (closedSet.contains(v)) continue; // 已在关闭列表中，跳过

                // 计算新的 g 值 = 起点到 u 的 g + 边权 (weight)
                // 注意：这里用到了图的实际权重！
                double weight = e.getMweight(); 
                double tentativeG = gScore.getOrDefault(u, Double.MAX_VALUE) + weight;

                if (tentativeG < gScore.getOrDefault(v, Double.MAX_VALUE)) {
                    // 发现更优路径，更新
                    cameFrom.put(v, u);
                    gScore.put(v, tentativeG);
                    
                    // 计算 h 值 (启发式：坐标距离)
                    double h = heuristic(v, endId, nodePositions);
                    double f = tentativeG + h;
                    fScore.put(v, f);

                    // 加入优先队列
                    openSet.add(new double[]{v, f});
                    
                    addStep(TraversalStep.Type.CHECK_EDGE, v, openSet, closedSet, 
                        "更新邻居 " + v + ": g=" + String.format("%.1f", tentativeG) + 
                        ", h=" + String.format("%.1f", h) + 
                        ", f=" + String.format("%.1f", f));
                }
            }
        }

        addStep(TraversalStep.Type.VISIT, startId, openSet, closedSet, "搜索结束，未能到达终点。");
        return steps;
    }

    // 启发函数：计算欧几里得距离 (直线距离)
// 启发函数：计算欧几里得距离 (直线距离)
    private double heuristic(int u, int target, Map<Integer, double[]> positions) {
        if (positions == null || !positions.containsKey(u) || !positions.containsKey(target)) {
            return 0.0; 
        }
        double[] p1 = positions.get(u);
        double[] p2 = positions.get(target);
        
        // 计算屏幕上的像素距离 (例如: 200.0)
        double pixelDistance = Math.sqrt(Math.pow(p1[0] - p2[0], 2) + Math.pow(p1[1] - p2[1], 2));
        
        // 【关键修改】进行缩放！
        // 解释：你的边权重通常是 1-10。
        // 而屏幕上两个节点的距离通常是 40-100 像素。
        // 所以我们需要把像素距离除以一个系数（例如 40.0），让 h(n) 变小。
        // 这样 h(n) 就不会远大于 g(n)，算法就会重新考虑权重，找到真正的最短路径。
        return pixelDistance / 40.0; 
    }

    // 辅助：从优先队列中提取所有节点ID，用于快照
    private List<Integer> getOpenSetIds(PriorityQueue<double[]> queue) {
        List<Integer> list = new ArrayList<>();
        for (double[] d : queue) list.add((int)d[0]);
        return list;
    }

    // 辅助：添加步骤
    private void addStep(TraversalStep.Type type, int current, PriorityQueue<double[]> openSet, Set<Integer> closedSet, String msg) {
        steps.add(new TraversalStep(
            type, 
            current, 
            getOpenSetIds(openSet), 
            closedSet, 
            msg
        ));
    }
    
    // 路径回溯
    private void reconstructPath(Map<Integer, Integer> cameFrom, int start, int end) {
        List<Integer> path = new ArrayList<>();
        Integer curr = end;
        while (curr != null) {
            path.add(0, curr);
            if (curr == start) break;
            curr = cameFrom.get(curr);
        }
        // 添加一个 PATH 类型的步骤
        steps.add(new TraversalStep(TraversalStep.Type.PATH, path, "路径构建完成，长度: " + path.size()));
    }
}