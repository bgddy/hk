package org.example.core;

public class MatrixGraph extends Graph{
    private int[][] mMatrix;

    public MatrixGraph(int numvertex) {
        super(numvertex);
        mMatrix = new int[numvertex][numvertex];
        for(int i = 0;i < numvertex;i ++ )
        {
            for(int j = 0;j < numvertex;j ++)
            {
                mMatrix[i][j] = 0;
            }
        }
    }

    @Override
    public Edge firstEdge(int onevertex) {
        Edge medge = new Edge();
        medge.setMfrom(onevertex);
        for(int i = 0;i < vecticesNumber();i ++)
        {
            if(mMatrix[onevertex][i] != 0 )
            {
                medge.setMto(i);
                medge.setMweight(mMatrix[onevertex][i]);
                break;
            }
        }
        if (medge.getMto() == 0 && mMatrix[onevertex][0] == 0) {
            return null;
        }
        return medge;
    }


    @Override
    public Edge nextEdge(Edge pre) {
        int from = pre.getMfrom();
        int start = pre.getMto() + 1;
        for(int i = start;i < vecticesNumber();i ++)
            if(mMatrix[from][i] != 0 ){
                Edge mEdge = new Edge(from ,i , mMatrix[from][i]);
                return mEdge;
            }
        return null;
    }

    @Override
    public void setEdge(int from, int to, int weight) {
        if(mMatrix[from][to] == 0 )
        {
            incEdgeNumber();
            incIndegree(to);
        }
        mMatrix[from][to] = weight;
    }

    @Override
    public void delEdge(int from, int to) {
        if(mMatrix[from][to] > 0)
        {
            decEdgeNumber();
            decIndegree(to);
        }
        mMatrix[from][to] = 0;
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
}
