package org.example.core;

import java.util.ArrayList;
import java.util.List;

public class SelectionSort {
    
    public List<SortFrame> sort(int[] arr) {
        List<SortFrame> steps = new ArrayList<>();
        int n = arr.length;
        int[] copy = arr.clone();

        // 伪代码行号 (用于 SortFrame 的 lineIndex):
        // 0: for i from 0 to n-1
        // 1:   min_idx = i
        // 2:   for j from i+1 to n
        // 3:     if arr[j] < arr[min_idx]
        // 4:       min_idx = j
        // 5:   swap(arr[i], arr[min_idx])

        // 初始帧
        steps.add(new SortFrame(copy, -1, -1, -1, -1, "选择排序开始。准备从未排序区域中找到最小值。"));

        for (int i = 0; i < n - 1; i++) {
            // 外层循环 (line 0)
            steps.add(new SortFrame(copy, 0, i, -1, -1, 
                "第 " + (i + 1) + " 轮：目标是将最小值放到位置 " + i + "。"));
            
            int minIdx = i;
            // 设定初始最小值 (line 1)
            steps.add(new SortFrame(copy, 1, i, -1, minIdx, 
                "假设当前位置 " + i + " 的元素 (" + copy[i] + ") 为未排序区域的最小值，索引 minIdx = " + i + "。"));

            for (int j = i + 1; j < n; j++) {
                // 内层循环 (line 2)
                steps.add(new SortFrame(copy, 2, i, j, minIdx, 
                    "开始搜索：检查位置 " + j + " 的元素。"));
                
                // 比较 (line 3)
                steps.add(new SortFrame(copy, 3, i, j, minIdx, 
                    "比较：当前元素 (" + copy[j] + ") 是否小于最小值 (" + copy[minIdx] + ")？"));
                
                if (copy[j] < copy[minIdx]) {
                    // 发现更小值 (line 4)
                    int oldMin = copy[minIdx];
                    minIdx = j;
                    steps.add(new SortFrame(copy, 4, i, j, minIdx, 
                        "发现新最小值！" + copy[minIdx] + " 比 " + oldMin + " 更小，更新 minIdx = " + j + "。"));
                }
            }

            // 交换 (line 5)
            if (minIdx != i) {
                // 交换前
                 steps.add(new SortFrame(copy, 5, i, -1, minIdx, 
                    "本轮搜索结束。将找到的最小值 (" + copy[minIdx] + ") 与位置 " + i + " 的元素 (" + copy[i] + ") 交换。"));
            } else {
                // minIdx == i
                 steps.add(new SortFrame(copy, 5, i, -1, minIdx, 
                    "本轮搜索结束。位置 " + i + " 的元素 (" + copy[i] + ") 已经是最小值，无需交换。"));
            }
           
            int temp = copy[i];
            copy[i] = copy[minIdx];
            copy[minIdx] = temp;
            
            // 交换完成后的状态展示 (无行号)
            steps.add(new SortFrame(copy, -1, -1, -1, -1, "交换完成。元素 " + copy[i] + " 已归位，成为已排序部分的一部分。"));
        }
        
        // 结束帧
        steps.add(new SortFrame(copy, -1, -1, -1, -1, "排序完成！数组已完全有序。"));
        
        return steps;
    }
}