package org.example.core;

public class UnionFind {
    private int[] parent;
    private int[] high;

    public UnionFind(int n)
    {
        parent = new int[n];
        high = new int[n];
        for(int i =0;i < n;i ++)
        {
            parent[i] = i;
            high[i] = 0;
        }
    }

    public int find(int x)
    {
        if(parent[x] == x)
        {
            return x;
        }
        else
            return parent[x] = find(parent[x]);
    }

    public void union(int x, int y)
    {
        int rootx = find(x);
        int rooty = find(y);
        if(rootx == rooty)
        {
            return;
        }
        if (high[rootx] > high[rooty])
        {
            parent[rooty] = rootx;
        }
        else if(high[rooty] > high[rootx])
        {
            parent[rootx] = rooty;
        }
        else{
            parent[rooty] = rootx;
            high[rootx]++;
        }
    }

    public boolean isConnected(int x,int y)
    {
        return find(x) == find(y);
    }
}
