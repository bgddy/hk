package org.example.core;

public class SortFrame {
    private int[] arrayState;
    private int lineIndex; // 代码行号
    
    // 高亮索引
    public int i = -1;     
    public int j = -1;     
    public int extra = -1; 
    
    public int l = -1;     
    public int r = -1;     

    // [新增] 步骤描述文本
    private String description = "";

    // 兼容旧代码的构造函数 (SelectionSort/InsertSort 使用)，增加了 description 参数
    public SortFrame(int[] array, int lineIndex, int i, int j, int extra, String description) {
        this(array, lineIndex, i, j, extra, -1, -1, description);
    }

    // 全参数构造函数 (FastSort 使用)，增加了 description 参数
    public SortFrame(int[] array, int lineIndex, int i, int j, int extra, int l, int r, String description) {
        this.arrayState = array.clone();
        this.lineIndex = lineIndex;
        this.i = i;
        this.j = j;
        this.extra = extra;
        this.l = l;
        this.r = r;
        this.description = description; // <-- 新增
    }

    public int[] getArrayState() { return arrayState; }
    public int getLineIndex() { return lineIndex; }
    
    // [新增] Getter for description
    public String getDescription() { return description; }

    public int getI() { return i; }
    public int getJ() { return j; }
    public int getExtra() { return extra; }
    public int getL() { return l; }
    public int getR() { return r; }
}