package org.example.core;

public class Edge {
    private  int mfrom;
    private  int mto;
    private  int mweight;

    public Edge(){}
    public Edge(int from,int to,int weight)
    {
        this.mfrom = from;
        this.mto = to;
        this.mweight =weight;
    }
    public int getMfrom(){return mfrom; }
    public void setMfrom(int mfrom){this.mfrom = mfrom;}
    public int getMto(){return mto;}
    public void setMto(int mto){this.mto = mto;}
    public int getMweight(){return  mweight;}
    public  void  setMweight(int mweight){this.mweight = mweight;}
}
