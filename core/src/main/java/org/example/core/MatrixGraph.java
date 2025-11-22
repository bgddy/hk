package org.example.core;

import java.util.ArrayList;
import java.util.List;

public class MatrixGraph extends Graph{
    private int[][] mMatrix;
    private int maxVertices;
    private boolean[] vertexExists; // 跟踪哪些顶点存在

    public MatrixGraph(int numvertex) {
        super(numvertex);
        this.maxVertices = numvertex;
        mMatrix = new int[numvertex][numvertex];
        vertexExists = new boolean[numvertex];
        for(int i = 0;i < numvertex;i ++ )
        {
            for(int j = 0;j < numvertex;j ++)
            {
                mMatrix[i][j] = 0;
            }
            vertexExists[i] = true; // 初始顶点都存在
        }
    }

    /** 动态添加顶点 */
    public void addVertex() {
        // 如果当前顶点数已经达到最大容量，需要扩展矩阵
        if (verticesNumber() >= maxVertices) {
            expandMatrix();
        }
        // 调用基类的addVertex方法增加顶点数
        super.addVertex();
    }

    /** 扩展矩阵大小 */
    private void expandMatrix() {
        int newSize = maxVertices * 2;
        int[][] newMatrix = new int[newSize][newSize];
        boolean[] newVertexExists = new boolean[newSize];
        
        // 复制原有数据
        for (int i = 0; i < maxVertices; i++) {
            for (int j = 0; j < maxVertices; j++) {
                newMatrix[i][j] = mMatrix[i][j];
            }
            newVertexExists[i] = vertexExists[i];
        }
        
        // 初始化新区域
        for (int i = maxVertices; i < newSize; i++) {
            for (int j = 0; j < newSize; j++) {
                newMatrix[i][j] = 0;
            }
            newVertexExists[i] = false; // 新顶点初始不存在
        }
        
        mMatrix = newMatrix;
        vertexExists = newVertexExists;
        maxVertices = newSize;
    }

    @Override
    public Edge firstEdge(int onevertex) {
        Edge medge = new Edge();
        medge.setMfrom(onevertex);
        for(int i = 0;i < verticesNumber();i ++)
        {
            if(mMatrix[onevertex][i] != 0 )
            {
                medge.setMto(i);
                medge.setMweight(mMatrix[onevertex][i]);
                return medge; // 找到第一个边立即返回
            }
        }
        return null; // 没有边
    }


    @Override
    public Edge nextEdge(Edge pre) {
        int from = pre.getMfrom();
        int start = pre.getMto() + 1;
        for(int i = start;i < verticesNumber();i ++)
            if(mMatrix[from][i] != 0 ){
                Edge mEdge = new Edge(from ,i , mMatrix[from][i]);
                return mEdge;
            }
        return null;
    }

    @Override
    public void setEdge(int from, int to, int weight) {
        // 设置正向边
        if(mMatrix[from][to] == 0 ) {
            incEdgeNumber();
            incIndegree(to);
        }
        mMatrix[from][to] = weight;

        // 设置反向边 (实现无向图)
        if (from != to) {
            if (mMatrix[to][from] == 0) {
                incEdgeNumber();
                incIndegree(from);
            }
            mMatrix[to][from] = weight;
        }
    }

    @Override
    public void delEdge(int from, int to) {
        // 删除正向边
        if(mMatrix[from][to] > 0) {
            decEdgeNumber();
            decIndegree(to);
        }
        mMatrix[from][to] = 0;

        // 删除反向边 (实现无向图)
        if (from != to) {
            if (mMatrix[to][from] > 0) {
                decEdgeNumber();
                decIndegree(from);
            }
            mMatrix[to][from] = 0;
        }
    }
    
    /** 覆盖基类方法，因为每条无向边实际上存储了2条有向边 */
    @Override
    public int edgesNumber() {
        return super.edgesNumber() / 2;
    }

    @Override
    public boolean isEdge(Edge edge) {
        return mMatrix[edge.getMfrom()][edge.getMto()] != 0 ;
    }

    @Override
    public int fromVertex(Edge edge) {
        return edge.getMfrom();
    }

    @Override
    public int toVertex(Edge edge) {
        return edge.getMto();
    }

    @Override
    public int weight(Edge edge) {
        return edge.getMweight();
    }

    /** 标记顶点存在状态 */
    public void setVertexExists(int vertexId, boolean exists) {
        if (vertexId >= 0 && vertexId < maxVertices) {
            vertexExists[vertexId] = exists;
        }
    }
    
    /** 检查顶点是否存在 */
    public boolean isVertexExists(int vertexId) {
        return vertexId >= 0 && vertexId < maxVertices && vertexExists[vertexId];
    }
    
    /** 获取当前存在的顶点数量 */
    public int getExistingVerticesCount() {
        int count = 0;
        for (int i = 0; i < maxVertices; i++) {
            if (vertexExists[i]) {
                count++;
            }
        }
        return count;
    }
    
    /** 获取边的权重 */
    public int getEdge(int from, int to) {
        if (from >= 0 && from < maxVertices && to >= 0 && to < maxVertices) {
            return mMatrix[from][to];
        }
        return 0;
    }
    
    /** 获取邻接矩阵的字符串表示 - 只显示存在的顶点 */
    public String getMatrixString() {
        StringBuilder sb = new StringBuilder();
        
        // 收集存在的顶点ID
        List<Integer> existingVertices = new ArrayList<>();
        for (int i = 0; i < maxVertices; i++) {
            if (vertexExists[i]) {
                existingVertices.add(i);
            }
        }
        
        int n = existingVertices.size();
        if (n == 0) {
            return "图为空";
        }
        
        // 添加表头
        sb.append("   ");
        for (int vertexId : existingVertices) {
            sb.append(String.format("%3d", vertexId));
        }
        sb.append("\n");
        
        // 添加分隔线
        sb.append("   ");
        for (int i = 0; i < n; i++) {
            sb.append("---");
        }
        sb.append("\n");
        
        // 添加矩阵内容
        for (int vertexId : existingVertices) {
            sb.append(String.format("%2d|", vertexId));
            for (int otherVertexId : existingVertices) {
                sb.append(String.format("%3d", mMatrix[vertexId][otherVertexId]));
            }
            sb.append("\n");
        }
        
        return sb.toString();
    }
    
    /** 清空所有边 */
    public void clearAllEdges() {
        int n = verticesNumber();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (mMatrix[i][j] != 0) {
                    mMatrix[i][j] = 0;
                }
            }
        }
        // 重置边数
        while (super.edgesNumber() > 0) {
            decEdgeNumber();
        }
    }
    
    /** 随机生成连通图 */
    public void generateRandomGraph() {
        clearAllEdges(); // 先清空所有边
        
        int n = verticesNumber();
        if (n <= 1) return;
        
        // 确保图连通：生成一个生成树
        for (int i = 1; i < n; i++) {
            int from = (int)(Math.random() * i);
            int weight = (int)(Math.random() * 10) + 1; // 权重1-10
            setEdge(from, i, weight);
        }
        
        // 随机添加一些额外边
        int extraEdges = (int)(Math.random() * (n * 2)) + n; // 额外边数：n到3n之间
        for (int i = 0; i < extraEdges; i++) {
            int from = (int)(Math.random() * n);
            int to = (int)(Math.random() * n);
            if (from != to) {
                int weight = (int)(Math.random() * 10) + 1;
                setEdge(from, to, weight);
            }
        }
    }
}