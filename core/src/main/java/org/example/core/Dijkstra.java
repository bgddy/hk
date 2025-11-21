package org.example.core;

import java.util.*;

public class Dijkstra {
    private Graph graph;
    private int[] dist;
    private int[] prev;
    private boolean[] visited;
    private List<String> logSteps; // 用于记录计算过程的日志

    public Dijkstra(Graph graph) {
        this.graph = graph;
        this.logSteps = new ArrayList<>();
    }

    /**
     * 计算从 start 到 end 的最短路径
     * @param start 起始顶点
     * @param end 结束顶点
     * @return 最短路径的顶点列表（包含 start 和 end），如果不可达则返回空列表
     */
    public List<Integer> findShortestPath(int start, int end) {
        int n = graph.verticesNumber();
        if (start < 0 || start >= n || end < 0 || end >= n) {
            logSteps.add("错误: 顶点索引越界");
            return new ArrayList<>();
        }

        dist = new int[n];
        prev = new int[n];
        visited = new boolean[n];
        
        Arrays.fill(dist, Integer.MAX_VALUE);
        Arrays.fill(prev, -1);
        
        dist[start] = 0;
        
        // 优先队列，存储 [顶点, 当前距离]
        PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> Integer.compare(a[1], b[1]));
        pq.offer(new int[]{start, 0});
        
        logSteps.clear();
        logSteps.add("初始化: 起点 " + start + " 距离设为 0，其他无穷大");

        while (!pq.isEmpty()) {
            int[] current = pq.poll();
            int u = current[0];
            int d = current[1];
            
            if (d > dist[u]) continue;
            if (u == end) {
                logSteps.add("已到达目标点 " + end + "，当前最短距离: " + d);
                break; // 找到目标，对于求单对最短路径可以提前结束
            }
            
            visited[u] = true;
            logSteps.add("访问顶点 " + u + " (距离: " + d + ")");
            
            for (Edge e = graph.firstEdge(u); e != null; e = graph.nextEdge(e)) {
                int v = e.getMto();
                int weight = e.getMweight();
                
                if (!visited[v] && dist[u] != Integer.MAX_VALUE && dist[u] + weight < dist[v]) {
                    dist[v] = dist[u] + weight;
                    prev[v] = u;
                    pq.offer(new int[]{v, dist[v]});
                    logSteps.add("  -> 更新邻居 " + v + " : 新距离 " + dist[v] + " (通过 " + u + ")");
                }
            }
        }
        
        List<Integer> path = new ArrayList<>();
        if (dist[end] == Integer.MAX_VALUE) {
            logSteps.add("无法到达终点 " + end);
            return path; // 无路径
        }
        
        // 重建路径
        for (int at = end; at != -1; at = prev[at]) {
            path.add(at);
        }
        Collections.reverse(path);
        
        return path;
    }
    
    public int getShortestDistance(int end) {
        if (end >= 0 && end < dist.length) {
            return dist[end];
        }
        return -1;
    }

    public String getProcessLog() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Dijkstra 计算过程 ===\n");
        for (String step : logSteps) {
            sb.append(step).append("\n");
        }
        return sb.toString();
    }
}