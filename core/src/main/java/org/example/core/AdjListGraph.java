package org.example.core;

public class AdjListGraph extends Graph{
    private LinkedList[] mGraphList;

    public AdjListGraph(int numvertex) {
        super(numvertex);
        mGraphList = new LinkedList[numvertex];
        for(int i = 0; i < numvertex; i ++)
        {
            mGraphList[i] = new LinkedList() ;
        }
    }

    @Override
    public Edge firstEdge(int onevertex) {
        Edge edge = new Edge();
        edge.setMfrom(onevertex);
        Link temp = this.mGraphList[onevertex].getHead();
        if(temp.getNext() !=  null)
        {
            edge.setMto(temp.getNext().getElement().getVertex());
            edge.setMweight(temp.getNext().getElement().getWeight());
        }
        return edge;
    }

    @Override
    public Edge nextEdge(Edge pre) {
        Edge edge = new Edge();
        edge.setMfrom(pre.getMfrom());
        Link temp = mGraphList[pre.getMfrom()].getHead();
        while(temp.getNext() != null && temp.getNext().getElement().getVertex() <= pre.getMto())
        {
            temp = temp.getNext();
        }
        if(temp.getNext() != null)
        {
            edge.setMto(temp.getNext().getElement().getVertex());
            edge.setMweight(temp.getNext().getElement().getWeight());
        }

        return edge;
    }

    @Override
    public void setEdge(int from, int to, int weight) {
        Link temp = mGraphList[from].getHead();
        while(temp.getNext() != null && temp.getNext().getElement().getVertex() <to)
        {
            temp = temp.getNext();
        }
        if(temp.getNext() == null)
        {
            temp.setNext(new Link());
            temp.getNext().getElement().setVertex(to);
            temp.getNext().getElement().setWeight(weight);
            incEdgeNumber();
            incIndegree(to);
            return;
        }
        if(temp.getNext().getElement().getVertex() == to)
        {
            temp.getNext().getElement().setWeight(weight);
            return;
        }
        if(temp.getNext().getElement().getVertex() > to)
        {
            Link other = temp.getNext();
            temp.setNext(new Link());
            temp.getNext().getElement().setVertex(to);
            temp.getNext().getElement().setWeight(weight);
            temp.getNext().setNext(other);
            incEdgeNumber();
            incIndegree(to);
            return;
        }
    }

    @Override
    public void delEdge(int from, int to) {
        Link temp = mGraphList[from].getHead();
        while(temp.getNext() != null && temp.getNext().getElement().getVertex() < to)
        {
            temp = temp.getNext();
        }
        if(temp.getNext().getElement().getVertex() == 0)
        {
            return;
        }
        if(temp.getNext().getElement().getVertex() > to)
        {
            return;
        }
        if(temp.getNext().getElement().getVertex() == to )
        {
            Link other = temp.getNext().getNext();
            temp.setNext(other);
            decEdgeNumber();
            decIndegree(to);
            return;
        }
    }

    @Override
    public boolean isEdge(Edge edge) {
        int from = edge.getMfrom();
        int to = edge.getMto();
        Link temp = mGraphList[from].getHead();
        while (temp.getNext() != null) {
            if (temp.getNext().getElement().getVertex() == to) {
                return true;
            }
            temp = temp.getNext();
        }
        return false;
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
