package org.example.core;

import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashSet;

public class TraversalStep {
    public enum Type {
        VISIT,          // 访问节点
        VISIT_EDGE,     // 访问边
        BACKTRACK,      // 回溯 (DFS用)
        CHECK_EDGE,     // 检查边
        ADD_EDGE,       // 添加边 (生成树用)
        REJECT_EDGE,    // 拒绝边
        RELAX_SUCCESS,  // 松弛成功 (Dijkstra用)
        PATH            // [新增] 最终找到的路径
    }

    private Type type;
    private int vertexId;
    private Edge edge;
    private int lineIndex; 

    // [新增] 用于 A* 展示详情的数据
    private String description = "";
    private List<Integer> openListSnapshot;
    private Set<Integer> closedListSnapshot;

    // 原有构造函数 1
    public TraversalStep(Type type, int vertexId, int lineIndex) {
        this.type = type;
        this.vertexId = vertexId;
        this.lineIndex = lineIndex;
    }

    // 原有构造函数 2
    public TraversalStep(Type type, Edge edge, int lineIndex) {
        this.type = type;
        this.edge = edge;
        this.lineIndex = lineIndex;
    }

    // [新增] 全能构造函数 (A* 专用)
    public TraversalStep(Type type, int vertexId, List<Integer> openList, Set<Integer> closedList, String description) {
        this.type = type;
        this.vertexId = vertexId;
        this.lineIndex = -1; // A* 暂时不对应伪代码行号，或者你自己定义
        this.openListSnapshot = openList != null ? new ArrayList<>(openList) : new ArrayList<>();
        this.closedListSnapshot = closedList != null ? new HashSet<>(closedList) : new HashSet<>();
        this.description = description;
    }
    
    // [新增] 路径构造函数
    public TraversalStep(Type type, List<Integer> pathIds, String description) {
        this.type = type;
        this.vertexId = -1; 
        this.description = description;
        // 这里的 openListSnapshot 借用来存路径，方便 UI 读取
        this.openListSnapshot = new ArrayList<>(pathIds);
    }

    public Type getType() { return type; }
    public int getVertexId() { return vertexId; }
    public Edge getEdge() { return edge; }
    public int getLineIndex() { return lineIndex; }
    
    // [新增 Getter]
    public String getDescription() { return description; }
    public List<Integer> getOpenListSnapshot() { return openListSnapshot; }
    public Set<Integer> getClosedListSnapshot() { return closedListSnapshot; }
}