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
        Link temp = mGraphList[onevertex].getHead();
        if (temp.getNext() != null) {
            Edge edge = new Edge(onevertex,
                    temp.getNext().getElement().getVertex(),
                    temp.getNext().getElement().getWeight());
            return edge;
        }
        return null; // 没有边
    }

    @Override
    public Edge nextEdge(Edge pre) {
        if (pre == null) return null;

        Link temp = mGraphList[pre.getMfrom()].getHead();
        // 找到上一次 Edge 对应的节点
        while (temp.getNext() != null && temp.getNext().getElement().getVertex() <= pre.getMto()) {
            temp = temp.getNext();
        }

        if (temp.getNext() != null) {
            Edge edge = new Edge(pre.getMfrom(),
                    temp.getNext().getElement().getVertex(),
                    temp.getNext().getElement().getWeight());
            return edge;
        } else {
            return null; // 到链表末尾
        }
    }

    @Override
    public void setEdge(int from, int to, int weight) {
        addSingleEdge(from,to,weight);
        if(from != to)
        {
            addSingleEdge(to,from,weight);
        }
    }

    @Override
    public void delEdge(int from, int to) {
        delSingleEdge(from,to);
        if(from != to)
        {
            delSingleEdge(to,from);
        }
    }


    public void addSingleEdge(int from, int to, int weight) {
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


    public void delSingleEdge(int from, int to) {
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
        if (edge == null) return false; // 空对象直接 false

        int from = edge.getMfrom();
        int to = edge.getMto();
        Link temp = mGraphList[from].getHead();

        while (temp.getNext() != null) {
            if (temp.getNext().getElement().getVertex() == to) return true;
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

    public int edgeNumbers(){
        return super.edgesNumber() / 2;
    }

    public Edge[] getAllEdge(){
        int n = vecticesNumber();
        int totalEdges = edgeNumbers();
        Edge[] allEdges = new Edge[totalEdges];
        int index = 0;

        for(int i = 0;i < n;i++)
        {
            for(Edge e = firstEdge(i);isEdge(e);e = nextEdge(e))
            {
                int from = fromVertex(e);
                int to = toVertex(e);
                if(from < to){
                    allEdges[index++] = e;
                }
            }
        }

        if(index < totalEdges)
        {
            Edge[] trimmed = new Edge[index];
            System.arraycopy(allEdges,0,trimmed,0,index);
            return trimmed;
        }
        return  allEdges;
    }
}
