package org.example.core;

public abstract class Graph implements IGraph {
  private   int m_num_vertex;	//顶点的个数
  private   int m_num_edge;	//边的条数
  private boolean[] m_visited; // 访问标记数组
  private int[] m_indegree;    // 入度数组
  public Graph(int numvertex)
  {
      this.m_num_edge = 0;
      this.m_num_vertex = numvertex;
      this.m_visited =  new boolean[numvertex];
      this.m_indegree = new int[numvertex];
      for(int i = 0;i <numvertex;i++)
      {
          this.m_indegree[i] = 0;
          this.m_visited[i] = false;
      }
  }
  public int vecticesNumber(){return m_num_vertex;}
  public int edgesNumber(){return m_num_edge;}
  protected void incEdgeNumber() {
        m_num_edge++;
    }
  protected void decEdgeNumber() {
        m_num_edge--;
    }
  protected void incIndegree(int v) {
        m_indegree[v]++;
    }
  protected void decIndegree(int v) {
        m_indegree[v]--;
    }
  public abstract Edge firstEdge(int onevertex);
  public abstract Edge nextEdge(Edge pre);
  public abstract void setEdge(int from, int to, int weight);
  public abstract void delEdge(int from, int to);
  public abstract boolean isEdge(Edge edge);
}
