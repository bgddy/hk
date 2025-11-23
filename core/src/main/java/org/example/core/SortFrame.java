package org.example.core;

public class SortFrame {
    private int[] arrayState;
    private int lineIndex; // 代码行号
    
    // 高亮索引
    public int i = -1;     // 扫描指针 i
    public int j = -1;     // 扫描指针 j
    public int extra = -1; // 额外变量 (Pivot, MinIdx 等)
    
    // 新增：递归范围 (用于快排)
    public int l = -1;     // 当前递归范围左边界
    public int r = -1;     // 当前递归范围右边界

    // 兼容旧代码的构造函数 (SelectionSort 使用)
    public SortFrame(int[] array, int lineIndex, int i, int j, int extra) {
        this(array, lineIndex, i, j, extra, -1, -1);
    }

    // 全参数构造函数 (FastSort 使用)
    public SortFrame(int[] array, int lineIndex, int i, int j, int extra, int l, int r) {
        this.arrayState = array.clone();
        this.lineIndex = lineIndex;
        this.i = i;
        this.j = j;
        this.extra = extra;
        this.l = l;
        this.r = r;
    }

    public int[] getArrayState() { return arrayState; }
    public int getLineIndex() { return lineIndex; }
}