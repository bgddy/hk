package org.example.core;

public class TraversalStep {
    public enum Type {
        VISIT,          // 访问节点 (处理/打印)
        VISIT_EDGE,     // 访问边 (发现邻居)
        BACKTRACK       // 递归回溯
    }

    private Type type;
    private int vertexId;
    private Edge edge;
    private int lineIndex; // 新增：对应的伪代码行号

    // 用于节点相关操作 (VISIT, BACKTRACK)
    public TraversalStep(Type type, int vertexId, int lineIndex) {
        this.type = type;
        this.vertexId = vertexId;
        this.lineIndex = lineIndex;
    }

    // 用于边相关操作 (VISIT_EDGE)
    public TraversalStep(Type type, Edge edge, int lineIndex) {
        this.type = type;
        this.edge = edge;
        this.lineIndex = lineIndex;
    }

    public Type getType() { return type; }
    public int getVertexId() { return vertexId; }
    public Edge getEdge() { return edge; }
    public int getLineIndex() { return lineIndex; } // 新增 getter
}