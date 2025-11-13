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

  /** 动态添加顶点 */
  public void addVertex() {
      // 扩展访问标记数组和入度数组
      int newSize = m_num_vertex + 1;
      boolean[] newVisited = new boolean[newSize];
      int[] newIndegree = new int[newSize];
      
      // 复制原有数据
      for (int i = 0; i < m_num_vertex; i++) {
          newVisited[i] = m_visited[i];
          newIndegree[i] = m_indegree[i];
      }
      
      // 初始化新顶点
      newVisited[m_num_vertex] = false;
      newIndegree[m_num_vertex] = 0;
      
      m_visited = newVisited;
      m_indegree = newIndegree;
      m_num_vertex = newSize;
  }
  public int verticesNumber(){return m_num_vertex;}
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
  public abstract void clearAllEdges();
  public abstract void generateRandomGraph();
}
