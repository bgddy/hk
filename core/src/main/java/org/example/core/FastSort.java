package org.example.core;

import java.util.ArrayList;
import java.util.List;

public class FastSort {

    private List<SortFrame> steps;

    // 伪代码映射:
    // 0: quickSort(arr, low, high)
    // 1:   if (low < high)
    // 2:     pivot = arr[high]; i = low - 1
    // 3:     for (j = low; j < high; j++)
    // 4:       if (arr[j] < pivot)
    // 5:         i++; swap(arr[i], arr[j])
    // 6:     swap(arr[i + 1], arr[high])
    // 7:     pi = i + 1
    // 8:     quickSort(arr, low, pi - 1)
    // 9:     quickSort(arr, pi + 1, high)

    public List<SortFrame> sort(int[] arr) {
        steps = new ArrayList<>();
        int n = arr.length;
        int[] copy = arr.clone();
        
        // 初始状态
        addStep(copy, 0, -1, -1, -1, 0, n-1);
        
        quickSort(copy, 0, n - 1);
        
        // 结束状态
        addStep(copy, 0, -1, -1, -1, -1, -1);
        
        return steps;
    }

    private void quickSort(int[] arr, int low, int high) {
        addStep(arr, 0, -1, -1, -1, low, high); // 函数入口

        addStep(arr, 1, -1, -1, -1, low, high); // 检查递归条件
        if (low < high) {
            int pi = partition(arr, low, high);

            // 递归左半部分
            addStep(arr, 8, -1, -1, pi, low, high);
            quickSort(arr, low, pi - 1);

            // 递归右半部分
            addStep(arr, 9, -1, -1, pi, low, high);
            quickSort(arr, pi + 1, high);
        }
    }

    private int partition(int[] arr, int low, int high) {
        int pivot = arr[high];
        int i = (low - 1);
        
        // 初始化 pivot 和 i
        addStep(arr, 2, i, -1, high, low, high); // extra=high (pivot位置)

        for (int j = low; j < high; j++) {
            // 循环开始
            addStep(arr, 3, i, j, high, low, high);

            // 比较
            addStep(arr, 4, i, j, high, low, high);
            if (arr[j] < pivot) {
                i++;
                // 交换 arr[i] 和 arr[j]
                swap(arr, i, j);
                addStep(arr, 5, i, j, high, low, high);
            }
        }

        // 把 pivot 放到正确的位置
        swap(arr, i + 1, high);
        addStep(arr, 6, i + 1, high, high, low, high);
        
        int pi = i + 1;
        addStep(arr, 7, -1, -1, pi, low, high); // 分区完成，pi 确定
        
        return pi;
    }

    private void swap(int[] arr, int i, int j) {
        int temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }
    
    private void addStep(int[] arr, int line, int i, int j, int extra, int l, int r) {
        steps.add(new SortFrame(arr, line, i, j, extra, l, r));
    }
}