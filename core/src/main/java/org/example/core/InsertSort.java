package org.example.core;

import java.util.ArrayList;
import java.util.List;

public class InsertSort {
    
    // 伪代码映射:
    // 0: for i from 1 to n-1
    // 1:   key = arr[i]; j = i - 1
    // 2:   while j >= 0 && arr[j] > key
    // 3:     arr[j + 1] = arr[j]
    // 4:     j = j - 1
    // 5:   arr[j + 1] = key

    public List<SortFrame> sort(int[] arr) {
        List<SortFrame> steps = new ArrayList<>();
        int n = arr.length;
        int[] copy = arr.clone();

        // 初始状态
        steps.add(new SortFrame(copy, 0, -1, -1, -1));

        for (int i = 1; i < n; i++) {
            // 外层循环开始，高亮 i (待插入元素)
            steps.add(new SortFrame(copy, 0, i, -1, -1));

            int key = copy[i];
            int j = i - 1;
            
            // 初始化 key 和 j
            // extra = i (标记 key 的来源位置)
            steps.add(new SortFrame(copy, 1, i, j, i));

            // 内层循环条件检查
            // 高亮 j (正在比较的元素) 和 extra (key 的来源)
            steps.add(new SortFrame(copy, 2, i, j, i));

            while (j >= 0 && copy[j] > key) {
                // 移动元素
                copy[j + 1] = copy[j];
                // 记录移动动作，高亮 j+1 (目标) 和 j (来源)
                steps.add(new SortFrame(copy, 3, i, j, j + 1));
                
                j--;
                // j 递减
                steps.add(new SortFrame(copy, 4, i, j, i));
                
                // 再次检查循环条件
                steps.add(new SortFrame(copy, 2, i, j, i));
            }
            
            copy[j + 1] = key;
            // 插入 key 到正确位置
            // extra = j + 1 (最终插入位置)
            steps.add(new SortFrame(copy, 5, i, j, j + 1));
        }
        
        // 结束
        steps.add(new SortFrame(copy, 0, -1, -1, -1));
        
        return steps;
    }
}