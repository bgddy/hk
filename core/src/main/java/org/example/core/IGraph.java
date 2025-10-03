package org.example.core;

interface IGraph{
    int vecticesNumber();
    int edgesNumber();
    Edge firstEdge(int onevertex);
    Edge nextEdge(Edge pre);
    void setEdge(int from,int to,int weight);
    void delEdge(int from,int to);
    boolean isEdge(Edge edge);
    int fromVertex(Edge edge);
    int toVertex(Edge edge);
    int weight(Edge edge);
}