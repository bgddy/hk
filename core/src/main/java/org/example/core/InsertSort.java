package org.example.core;

import java.util.ArrayList;
import java.util.List;

public class InsertSort {

    public List<SortFrame> sort(int[] arr) {
        List<SortFrame> steps = new ArrayList<>();
        int n = arr.length;
        int[] copy = arr.clone();

        // 伪代码行号:
        // 0: for i from 1 to n-1
        // 1:   key = arr[i]
        // 2:   j = i - 1
        // 3:   while j >= 0 and arr[j] > key
        // 4:     arr[j + 1] = arr[j]
        // 5:     j = j - 1
        // 6:   arr[j + 1] = key

        // 初始帧
        steps.add(new SortFrame(copy, -1, -1, -1, -1, "插入排序开始。默认第一个元素已排序。"));

        for (int i = 1; i < n; i++) {
            // 外层循环 (line 0)
            steps.add(new SortFrame(copy, 0, i, -1, -1, 
                "第 " + i + " 轮：选取元素 " + copy[i] + " (位置 " + i + ") 作为待插入的关键字 (key)。已排序区域为 [0, " + (i-1) + "]。"));

            int key = copy[i];
            // 设定 key (line 1)
            steps.add(new SortFrame(copy, 1, i, -1, key, 
                "Key = " + key + "。它将向前扫描已排序部分，找到自己的正确位置。"));
            
            int j = i - 1;
            // 初始化 j (line 2)
            steps.add(new SortFrame(copy, 2, i, j, key, 
                "设置 j = " + j + "，从已排序部分的末尾开始向前比较。"));

            while (j >= 0 && copy[j] > key) {
                // while 循环条件判断 (line 3)
                steps.add(new SortFrame(copy, 3, i, j, key, 
                    "比较：位置 " + j + " 的元素 (" + copy[j] + ") 是否大于 Key (" + key + ")？条件满足。"));

                // 元素后移 (line 4)
                copy[j + 1] = copy[j];
                steps.add(new SortFrame(copy, 4, i, j + 1, key, 
                    "元素 " + copy[j + 1] + " 后移一位到位置 " + (j + 1) + "，为 Key 腾出空间。"));
                
                j = j - 1;
                // j 减一 (line 5)
                steps.add(new SortFrame(copy, 5, i, j, key, 
                    "指针 j 向前移动一位到位置 " + j + "，继续向前比较。"));
            }

            // while 循环条件判断 (line 3 - 失败)
            steps.add(new SortFrame(copy, 3, i, j, key, 
                "比较失败或到达边界 (j=" + j + ")。已找到 Key (" + key + ") 的正确插入位置 (" + (j + 1) + ")。"));
            
            // 插入 key (line 6)
            copy[j + 1] = key;
            steps.add(new SortFrame(copy, 6, i, j + 1, key, 
                "将 Key (" + key + ") 插入到位置 " + (j + 1) + "。现在已排序部分增加一位。"));
        }
        
        // 结束帧
        steps.add(new SortFrame(copy, -1, -1, -1, -1, "排序完成！数组已完全有序。"));
        
        return steps;
    }
}