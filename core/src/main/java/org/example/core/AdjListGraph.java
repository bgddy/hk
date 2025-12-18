package org.example.core;

public class AdjListGraph extends Graph {
    private LinkedList[] mGraphList;
    // [新增] 标记顶点是否存在的数组
    private boolean[] vertexExists;

    public AdjListGraph(int numvertex) {
        super(numvertex);
        mGraphList = new LinkedList[numvertex];
        vertexExists = new boolean[numvertex]; // [新增] 初始化
        for(int i = 0; i < numvertex; i++) {
            mGraphList[i] = new LinkedList();
            vertexExists[i] = true; // [新增] 默认为存在
        }
    }

    @Override
    public Edge firstEdge(int onevertex) {
        // 如果点不存在，逻辑上它没有边 (可选保护)
        if (!isVertexExists(onevertex)) return null; 

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
        if(from != to) {
            addSingleEdge(to,from,weight);
        }
    }

    @Override
    public void delEdge(int from, int to) {
        delSingleEdge(from,to);
        if(from != to) {
            delSingleEdge(to,from);
        }
    }

    public void addSingleEdge(int from, int to, int weight) {
        Link temp = mGraphList[from].getHead();
        while(temp.getNext() != null && temp.getNext().getElement().getVertex() < to) {
            temp = temp.getNext();
        }
        if(temp.getNext() == null) {
            temp.setNext(new Link());
            temp.getNext().getElement().setVertex(to);
            temp.getNext().getElement().setWeight(weight);
            incEdgeNumber();
            incIndegree(to);
            return;
        }
        if(temp.getNext().getElement().getVertex() == to) {
            temp.getNext().getElement().setWeight(weight);
            return;
        }
        if(temp.getNext().getElement().getVertex() > to) {
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
        while(temp.getNext() != null && temp.getNext().getElement().getVertex() < to) {
            temp = temp.getNext();
        }
        if(temp.getNext() == null) {
            return;
        }
        if(temp.getNext().getElement().getVertex() > to) {
            return;
        }
        if(temp.getNext().getElement().getVertex() == to ) {
            Link other = temp.getNext().getNext();
            temp.setNext(other);
            decEdgeNumber();
            decIndegree(to);
            return;
        }
    }

    @Override
    public boolean isEdge(Edge edge) {
        if (edge == null) return false;

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

        for(int i = 0;i < n;i++) {
            // [新增] 跳过不存在的点
            if (!isVertexExists(i)) continue;

            for(Edge e = firstEdge(i);e != null;e = nextEdge(e)) {
                int from = fromVertex(e);
                int to = toVertex(e);
                // [新增] 确保目标点也存在
                if (!isVertexExists(to)) continue;

                if(from < to){
                    allEdges[index++] = e;
                }
            }
        }

        if(index < totalEdges) {
            Edge[] trimmed = new Edge[index];
            System.arraycopy(allEdges,0,trimmed,0,index);
            return trimmed;
        }
        return allEdges;
    }
    
    /** 重写addVertex方法以扩展邻接表数组 */
    @Override
    public void addVertex() {
        super.addVertex(); // 调用父类方法更新顶点数
        
        int newSize = verticesNumber();
        LinkedList[] newGraphList = new LinkedList[newSize];
        boolean[] newVertexExists = new boolean[newSize]; // [新增]
        
        // 复制原有邻接表和状态
        for (int i = 0; i < mGraphList.length; i++) {
            newGraphList[i] = mGraphList[i];
            newVertexExists[i] = vertexExists[i]; // [新增]
        }
        
        // 为新顶点创建新的链表
        newGraphList[newSize - 1] = new LinkedList();
        newVertexExists[newSize - 1] = true; // [新增] 新点默认存在
        
        mGraphList = newGraphList;
        vertexExists = newVertexExists; // [新增]
    }

    // [新增] 设置顶点存在状态
    public void setVertexExists(int v, boolean exists) {
        if (v >= 0 && v < vertexExists.length) {
            vertexExists[v] = exists;
        }
    }

    // [新增] 检查顶点是否存在
    public boolean isVertexExists(int v) {
        return v >= 0 && v < vertexExists.length && vertexExists[v];
    }
    
    /** 获取邻接表的字符串表示 */
    public String getAdjListString() {
        StringBuilder sb = new StringBuilder();
        int n = verticesNumber();
        
        for (int i = 0; i < n; i++) {
            // [新增] 如果顶点被标记为删除，则跳过不显示
            if (!vertexExists[i]) continue;

            sb.append(i).append(": ");
            Link current = mGraphList[i].getHead().getNext();
            
            while (current != null) {
                // [可选] 也可以在这里判断目标点是否存在，如果不需要显示指向已删除点的悬空边
                // if (isVertexExists(current.getElement().getVertex())) { ... }
                
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
        while (edgesNumber() > 0) {
            decEdgeNumber();
        }
        // [新增] 重置所有入度为0 (可选，虽然Graph里没有直接resetIndegree的方法，但逻辑上清空边后入度应为0)
        // 这里的逻辑保持原样，因为入度是在添加/删除边时维护的
    }
    
    /** 随机生成连通图 */
    public void generateRandomGraph() {
        clearAllEdges(); // 先清空所有边
        
        // [新增] 生成随机图时，假设操作的是当前所有存在的点（这里简化处理，假设所有点都有效）
        // 如果要支持在部分删除的情况下生成随机图，逻辑会更复杂。
        // 这里为了简单，我们重新把所有点标记为存在
        for(int i=0; i<vertexExists.length; i++) vertexExists[i] = true;

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