package org.example.core;

public class ListUnit {
    private int vertex;
    private int weight;

    public ListUnit()
    {

    }

    public ListUnit(int vertex,int weight)
    {
        this.vertex = vertex;
        this.weight = weight;
    }

    public void setVertex(int vertex) {
        this.vertex = vertex;
    }

    public int getVertex() {
        return vertex;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getWeight() {
        return weight;
    }
}
