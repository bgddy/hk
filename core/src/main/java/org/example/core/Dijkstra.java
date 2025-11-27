package org.example.core;

import java.util.*;

public class Dijkstra {
    private Graph graph;
    private int[] dist;
    private int[] prev;
    private boolean[] visited;
    private List<TraversalStep> steps; 

    public Dijkstra(Graph graph) {
        this.graph = graph;
        this.steps = new ArrayList<>(); 
    }

    /**
     * 计算从 start 到 end 的最短路径
     * @param start 起始顶点
     * @param end 结束顶点. 如果为 -1，则计算到所有点的路径。
     * @return 最短路径的顶点列表（包含 start 和 end），如果不可达或计算所有点则返回空列表
     */
    public List<Integer> findShortestPath(int start, int end) {
        int n = graph.verticesNumber();
        if (start < 0 || start >= n || (end != -1 && (end < 0 || end >= n))) {
            steps.add(new TraversalStep(TraversalStep.Type.VISIT, -1, 0)); // 错误标志
            return new ArrayList<>();
        }

        dist = new int[n];
        prev = new int[n];
        visited = new boolean[n];
        
        Arrays.fill(dist, Integer.MAX_VALUE);
        Arrays.fill(prev, -1);
        
        dist[start] = 0;
        
        PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> Integer.compare(a[1], b[1]));
        pq.offer(new int[]{start, 0});
        
        steps.clear();
        steps.add(new TraversalStep(TraversalStep.Type.VISIT, start, 0)); // 0: 初始化

        while (!pq.isEmpty()) {
            steps.add(new TraversalStep(TraversalStep.Type.VISIT, -1, 1)); // 1: while loop check

            int[] current = pq.poll();
            int u = current[0];
            int d = current[1];
            
            steps.add(new TraversalStep(TraversalStep.Type.VISIT, u, 2)); // 2: u = PQ.poll()

            if (d > dist[u]) continue;
            
            if (end != -1 && u == end) {
                steps.add(new TraversalStep(TraversalStep.Type.VISIT, u, 3)); 
                break; 
            }
            
            if (visited[u]) {
                 steps.add(new TraversalStep(TraversalStep.Type.VISIT, u, 3)); // 3: 剪枝 (已访问)
                 continue;
            }
            
            visited[u] = true;
            steps.add(new TraversalStep(TraversalStep.Type.VISIT, u, 4)); // 4: visited[u] = true

            for (Edge e = graph.firstEdge(u); e != null; e = graph.nextEdge(e)) {
                steps.add(new TraversalStep(TraversalStep.Type.VISIT_EDGE, e, 5)); // 5: 检查边

                int v = e.getMto();
                int weight = e.getMweight();
                
                if (dist[u] != Integer.MAX_VALUE && !visited[v]) {
                     if (dist[u] + weight < dist[v]) {
                        dist[v] = dist[u] + weight;
                        prev[v] = u;
                        pq.offer(new int[]{v, dist[v]});
                        
                        // 7: 松弛成功
                        steps.add(new TraversalStep(TraversalStep.Type.RELAX_SUCCESS, e, 7)); 
                        // 8: PQ.offer(v)
                        steps.add(new TraversalStep(TraversalStep.Type.VISIT, v, 8));
                    } else {
                        // 6: 松弛失败
                        steps.add(new TraversalStep(TraversalStep.Type.VISIT_EDGE, e, 6)); 
                    }
                } else if (!visited[v] && dist[u] != Integer.MAX_VALUE) {
                     steps.add(new TraversalStep(TraversalStep.Type.VISIT_EDGE, e, 6)); 
                }
            }
        }
        
        if (end == -1) return new ArrayList<>();

        List<Integer> path = new ArrayList<>();
        if (dist[end] == Integer.MAX_VALUE) return path;
        
        for (int at = end; at != -1; at = prev[at]) {
            path.add(at);
        }
        Collections.reverse(path);
        return path;
    }
    
    public List<TraversalStep> getSteps() { return steps; }

    public int getShortestDistance(int end) {
        if (dist != null && end >= 0 && end < dist.length) return dist[end];
        return -1;
    }

    public String getProcessLog() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Dijkstra 计算过程 (动画模式) ===\n");
        sb.append("请观察图形和伪代码。\n");
        sb.append("最短距离（Dist[]）数组状态：\n");
        if (dist != null) {
             for (int i = 0; i < dist.length; i++) {
                 sb.append("Dist[").append(i).append("]: ");
                 if (dist[i] == Integer.MAX_VALUE) sb.append("∞");
                 else sb.append(dist[i]).append(" (Prev: ").append(prev[i]).append(")");
                 sb.append("\n");
             }
        } else { sb.append("请先运行算法。\n"); }
        return sb.toString();
    }

    public String getAllPathsResult(int start) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Dijkstra 单源全路径结果 (起点: ").append(start).append(") ===\n");
        sb.append(String.format("%-8s | %-8s | %s\n", "终点", "距离", "路径"));
        sb.append("----------------------------------------\n");

        for (int i = 0; i < dist.length; i++) {
            if (i == start) continue;
            sb.append(String.format("%-10d | ", i));
            if (dist[i] == Integer.MAX_VALUE) {
                sb.append(String.format("%-10s | %s\n", "不可达", "-"));
            } else {
                sb.append(String.format("%-10d | ", dist[i]));
                StringBuilder pathBuilder = new StringBuilder();
                List<Integer> pathStack = new ArrayList<>();
                for (int curr = i; curr != -1; curr = prev[curr]) pathStack.add(curr);
                Collections.reverse(pathStack);
                for (int k = 0; k < pathStack.size(); k++) {
                    pathBuilder.append(pathStack.get(k));
                    if (k < pathStack.size() - 1) pathBuilder.append("->");
                }
                sb.append(pathBuilder.toString()).append("\n");
            }
        }
        return sb.toString();
    }
}