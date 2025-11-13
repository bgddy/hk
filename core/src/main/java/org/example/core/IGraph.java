package org.example.core;

interface IGraph{
    int verticesNumber();
    int edgesNumber();
    Edge firstEdge(int onevertex);
    Edge nextEdge(Edge pre);
    void setEdge(int from,int to,int weight);
    void delEdge(int from,int to);
    boolean isEdge(Edge edge);
    int fromVertex(Edge edge);
    int toVertex(Edge edge);
    int weight(Edge edge);
    void addVertex();  // 添加动态添加顶点的方法
    void clearAllEdges();  // 清空所有边
    void generateRandomGraph();  // 随机生成图
}
