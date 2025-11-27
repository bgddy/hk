package org.example.core;

public class TraversalStep {
    public enum Type {
        VISIT,          
        VISIT_EDGE,     
        BACKTRACK,      
        CHECK_EDGE,     
        ADD_EDGE,      
        REJECT_EDGE,
        RELAX_SUCCESS
    }

    private Type type;
    private int vertexId;
    private Edge edge;
    private int lineIndex; 

    
    public TraversalStep(Type type, int vertexId, int lineIndex) {
        this.type = type;
        this.vertexId = vertexId;
        this.lineIndex = lineIndex;
    }

   
    public TraversalStep(Type type, Edge edge, int lineIndex) {
        this.type = type;
        this.edge = edge;
        this.lineIndex = lineIndex;
    }

    public Type getType() { return type; }
    public int getVertexId() { return vertexId; }
    public Edge getEdge() { return edge; }
    public int getLineIndex() { return lineIndex; }
}