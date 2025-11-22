package org.example.core;

public class TraversalStep {
    public enum Type {
        VISIT,          // 正式访问节点 (处理)
        VISIT_EDGE,     // 访问/经过边 (BFS扩散效果)
        BACKTRACK       // 递归回溯 (DFS专用效果)
    }

    private Type type;
    private int vertexId;
    private Edge edge;

    // 用于节点相关操作 (VISIT, BACKTRACK)
    public TraversalStep(Type type, int vertexId) {
        this.type = type;
        this.vertexId = vertexId;
    }

    // 用于边相关操作 (VISIT_EDGE)
    public TraversalStep(Type type, Edge edge) {
        this.type = type;
        this.edge = edge;
    }

    public Type getType() { return type; }
    public int getVertexId() { return vertexId; }
    public Edge getEdge() { return edge; }
}