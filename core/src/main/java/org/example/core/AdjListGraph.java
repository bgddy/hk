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
        if(temp.getNext() == null)
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
        int n = verticesNumber();
        int totalEdges = edgeNumbers();
        Edge[] allEdges = new Edge[totalEdges];
        int index = 0;

        for(int i = 0;i < n;i++)
        {
            for(Edge e = firstEdge(i);e != null;e = nextEdge(e))
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
    
    /** 重写addVertex方法以扩展邻接表数组 */
    @Override
    public void addVertex() {
        super.addVertex(); // 调用父类方法更新顶点数
        
        int newSize = verticesNumber();
        LinkedList[] newGraphList = new LinkedList[newSize];
        
        // 复制原有邻接表
        for (int i = 0; i < mGraphList.length; i++) {
            newGraphList[i] = mGraphList[i];
        }
        
        // 为新顶点创建新的链表
        newGraphList[newSize - 1] = new LinkedList();
        
        mGraphList = newGraphList;
    }
    
    /** 获取邻接表的字符串表示 */
    public String getAdjListString() {
        StringBuilder sb = new StringBuilder();
        int n = verticesNumber();
        
        for (int i = 0; i < n; i++) {
            sb.append(i).append(": ");
            Link current = mGraphList[i].getHead().getNext();
            
            while (current != null) {
                sb.append("-> ").append(current.getElement().getVertex())
                  .append("(").append(current.getElement().getWeight()).append(")");
                current = current.getNext();
            }
            sb.append("\n");
        }
        
        return sb.toString();
    }
    
    /** 清空所有边 */
    public void clearAllEdges() {
        int n = verticesNumber();
        for (int i = 0; i < n; i++) {
            mGraphList[i] = new LinkedList();
        }
        // 重置边数
        while (edgesNumber() > 0) {
            decEdgeNumber();
        }
        // 重置入度 - 通过重新初始化图来实现
        for (int i = 0; i < n; i++) {
            // 入度会在添加边时自动更新，清空后所有入度都为0
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
