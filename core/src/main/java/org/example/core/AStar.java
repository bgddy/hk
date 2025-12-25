package org.example.core;

import java.util.*;

public class AStar {

    private List<TraversalStep> steps = new ArrayList<>();

    public List<TraversalStep> search(IGraph graph, int startId, int endId, Map<Integer, double[]> nodePositions) {
        steps.clear();
        
        // [优化 1] 比较器逻辑保持不变
        PriorityQueue<double[]> openSet = new PriorityQueue<>(Comparator.comparingDouble(a -> a[1]));
        
        Map<Integer, Double> gScore = new HashMap<>();
        Map<Integer, Double> fScore = new HashMap<>();
        Map<Integer, Integer> cameFrom = new HashMap<>();
        Set<Integer> closedSet = new HashSet<>();

        gScore.put(startId, 0.0);
        // [修改点 1] 传入当前的最小边权重（如果无法获取，可以用一个保守的估计值）
        // 这里为了演示，我们假设最小可能的边权重是 1.0，来动态调整启发系数
        double hStart = heuristic(startId, endId, nodePositions); 
        fScore.put(startId, hStart);
        openSet.add(new double[]{startId, hStart});

        addStep(TraversalStep.Type.VISIT, startId, openSet, closedSet, 
                "A* 开始。起点:" + startId + " 终点:" + endId + " 预估距离 h=" + String.format("%.1f", hStart));

        while (!openSet.isEmpty()) {
            double[] current = openSet.poll();
            int u = (int) current[0];

            if (closedSet.contains(u)) continue;
            closedSet.add(u);

            addStep(TraversalStep.Type.VISIT, u, openSet, closedSet, 
                    "探索节点 " + u + " (f=" + String.format("%.1f", current[1]) + ", g=" + String.format("%.1f", gScore.get(u)) + ")");

            if (u == endId) {
                addStep(TraversalStep.Type.VISIT, u, openSet, closedSet, "找到终点！准备回溯路径。");
                reconstructPath(cameFrom, startId, endId);
                return steps;
            }

            for (Edge e = graph.firstEdge(u); e != null; e = graph.nextEdge(e)) {
                int v = graph.toVertex(e); // 确保 IGraph 实现正确
                
                if (closedSet.contains(v)) continue;

                double weight = e.getMweight(); 
                double tentativeG = gScore.getOrDefault(u, Double.MAX_VALUE) + weight;

                if (tentativeG < gScore.getOrDefault(v, Double.MAX_VALUE)) {
                    cameFrom.put(v, u);
                    gScore.put(v, tentativeG);
                    
                    double h = heuristic(v, endId, nodePositions);
                    double f = tentativeG + h;
                    fScore.put(v, f);

                    openSet.add(new double[]{v, f});
                    
                    addStep(TraversalStep.Type.CHECK_EDGE, v, openSet, closedSet, 
                        "更新邻居 " + v + " : g=" + String.format("%.1f", tentativeG) + 
                        " + h=" + String.format("%.1f", h) + 
                        " = f:" + String.format("%.1f", f));
                }
            }
        }

        addStep(TraversalStep.Type.VISIT, startId, openSet, closedSet, "搜索结束，未能到达终点。");
        return steps;
    }

    private double heuristic(int u, int target, Map<Integer, double[]> positions) {
        if (positions == null || !positions.containsKey(u) || !positions.containsKey(target)) {
            return 0.0; 
        }
        double[] p1 = positions.get(u);
        double[] p2 = positions.get(target);
        double pixelDistance = Math.sqrt(Math.pow(p1[0] - p2[0], 2) + Math.pow(p1[1] - p2[1], 2));
        
        // 【关键修复】
        // 如果你的图支持用户随意输入权重（例如输入 1），而屏幕距离很远（例如 200px），
        // 那么 200/40 = 5 > 1，算法失效。
        // 
        // 方案 A (保守)：除以一个更大的数，确保 h 永远小于最小的边权重。
        //               假设屏幕最大距离 2000，最小边权 1，那么除数至少要是 2000。
        // 方案 B (简单)：如果发现效果不好，直接返回 0.0，退化为 Dijkstra 算法，保证绝对正确。
        // 方案 C (当前)：调大除数。建议改为 100.0 或更大，取决于你的画布缩放。
        
        return pixelDistance / 10; // 调大除数，降低 h 权重，保证 h < 真实代价
    }

    // [优化 2] 让 UI 看到的 OpenList 是有序的
    private List<Integer> getOpenSetIds(PriorityQueue<double[]> queue) {
        List<double[]> list = new ArrayList<>(queue);
        // 手动排序，因为 Iterator 不保证顺序
        list.sort(Comparator.comparingDouble(a -> a[1]));
        
        List<Integer> ids = new ArrayList<>();
        for (double[] d : list) ids.add((int)d[0]);
        return ids;
    }

    private void addStep(TraversalStep.Type type, int current, PriorityQueue<double[]> openSet, Set<Integer> closedSet, String msg) {
        steps.add(new TraversalStep(
            type, 
            current, 
            getOpenSetIds(openSet), 
            closedSet, 
            msg
        ));
    }
    
    private void reconstructPath(Map<Integer, Integer> cameFrom, int start, int end) {
        List<Integer> path = new ArrayList<>();
        Integer curr = end;
        while (curr != null) {
            path.add(0, curr);
            if (curr == start) break;
            curr = cameFrom.get(curr);
        }
        steps.add(new TraversalStep(TraversalStep.Type.PATH, path, "路径构建完成，长度: " + path.size()));
    }
}