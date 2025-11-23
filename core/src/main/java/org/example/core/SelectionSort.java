package org.example.core;

import java.util.ArrayList;
import java.util.List;

public class SelectionSort {
    
    public List<SortFrame> sort(int[] arr) {
        List<SortFrame> steps = new ArrayList<>();
        int n = arr.length;
        int[] copy = arr.clone();

        // 伪代码行号映射:
        // 0: for i from 0 to n-1
        // 1:   min_idx = i
        // 2:   for j from i+1 to n
        // 3:     if arr[j] < arr[min_idx]
        // 4:       min_idx = j
        // 5:   swap(arr[i], arr[min_idx])

        // 初始帧
        steps.add(new SortFrame(copy, 0, -1, -1, -1));

        for (int i = 0; i < n - 1; i++) {
            // 外层循环开始，高亮 i
            steps.add(new SortFrame(copy, 0, i, -1, -1)); 
            
            int minIdx = i;
            // 设定初始最小值，高亮 i 和 minIdx
            steps.add(new SortFrame(copy, 1, i, -1, minIdx)); 

            for (int j = i + 1; j < n; j++) {
                // 内层循环开始，高亮 i, j, minIdx
                steps.add(new SortFrame(copy, 2, i, j, minIdx)); 
                
                // 比较操作
                steps.add(new SortFrame(copy, 3, i, j, minIdx)); 
                
                if (copy[j] < copy[minIdx]) {
                    minIdx = j;
                    // 更新最小值，j 变成了新的 minIdx
                    steps.add(new SortFrame(copy, 4, i, j, minIdx)); 
                }
            }

            // 交换，高亮涉及交换的两个点 i 和 minIdx
            int temp = copy[i];
            copy[i] = copy[minIdx];
            copy[minIdx] = temp;
            
            steps.add(new SortFrame(copy, 5, i, -1, minIdx)); 
        }
        
        // 结束
        steps.add(new SortFrame(copy, 0, -1, -1, -1)); 
        
        return steps;
    }
}