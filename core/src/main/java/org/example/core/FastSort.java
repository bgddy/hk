package org.example.core;

import java.util.ArrayList;
import java.util.List;

public class FastSort {
    
    private List<SortFrame> steps = new ArrayList<>();
    private int[] arr;

    // 伪代码行号:
    // 0: function partition(arr, low, high)
    // 1:   pivot = arr[high]
    // 2:   i = low - 1
    // 3:   for j from low to high - 1
    // 4:     if arr[j] <= pivot
    // 5:       i = i + 1
    // 6:       swap(arr[i], arr[j])
    // 7:   swap(arr[i + 1], arr[high])
    // 8:   return i + 1
    // 9: 
    // 10: function quick_sort(arr, low, high)
    // 11:   if low < high
    // 12:     pi = partition(arr, low, high)
    // 13:     quick_sort(arr, low, pi - 1)
    // 14:     quick_sort(arr, pi + 1, high)

    public List<SortFrame> sort(int[] array) {
        this.arr = array.clone();
        this.steps.clear();

        // 初始帧
        steps.add(new SortFrame(arr, -1, -1, -1, -1, -1, -1, "快速排序开始。这是一个分治算法。"));

        quickSort(0, arr.length - 1);
        
        // 结束帧
        steps.add(new SortFrame(arr, -1, -1, -1, -1, -1, -1, "排序完成！数组已完全有序。"));
        
        return steps;
    }

    private void quickSort(int low, int high) {
        // quick_sort function (line 10)
        steps.add(new SortFrame(arr, 10, -1, -1, -1, low, high, 
            "进入快速排序函数，处理子数组 [" + low + ", " + high + "]。"));
        
        // if low < high (line 11)
        if (low < high) {
            steps.add(new SortFrame(arr, 11, -1, -1, -1, low, high, 
                "子数组长度大于 1 (" + low + " < " + high + ")，需要分区。"));
            
            // pi = partition(...) (line 12)
            int pi = partition(low, high);
            steps.add(new SortFrame(arr, 12, pi, -1, arr[pi], low, high, 
                "分区完成。基准值 (" + arr[pi] + ") 已归位到索引 " + pi + "。"));
            
            // quick_sort(low, pi - 1) (line 13)
            quickSort(low, pi - 1);
            
            // quick_sort(pi + 1, high) (line 14)
            quickSort(pi + 1, high);
        } else {
            // if low < high (line 11 - 失败)
            steps.add(new SortFrame(arr, 11, -1, -1, -1, low, high, 
                "子数组长度小于等于 1 (" + low + " >= " + high + ")，无需分区，递归返回。"));
        }
    }

    private int partition(int low, int high) {
        // partition function (line 0)
        steps.add(new SortFrame(arr, 0, -1, high, -1, low, high, 
            "进入分区函数 (Partition)。子数组范围 [" + low + ", " + high + "]。"));
        
        int pivot = arr[high];
        // pivot = arr[high] (line 1)
        steps.add(new SortFrame(arr, 1, -1, high, pivot, low, high, 
            "选择最右侧元素 (" + pivot + ") 作为基准值 (Pivot)。"));
        
        int i = low - 1;
        // i = low - 1 (line 2)
        steps.add(new SortFrame(arr, 2, i, high, pivot, low, high, 
            "初始化指针 i = " + i + "，i 指向小于 Pivot 区域的边界。"));

        for (int j = low; j < high; j++) {
            // for j from low to high - 1 (line 3)
            steps.add(new SortFrame(arr, 3, i, j, pivot, low, high, 
                "检查元素 arr[" + j + "] = " + arr[j] + "。"));
            
            // if arr[j] <= pivot (line 4)
            if (arr[j] <= pivot) {
                steps.add(new SortFrame(arr, 4, i, j, pivot, low, high, 
                    "arr[" + j + "] (" + arr[j] + ") <= Pivot (" + pivot + ")。条件满足。"));
                
                i++;
                // i = i + 1 (line 5)
                steps.add(new SortFrame(arr, 5, i, j, pivot, low, high, 
                    "i 递增到 " + i + "，扩展小于 Pivot 的区域。"));
                
                // swap(arr[i], arr[j]) (line 6)
                // 仅当 i 和 j 不同时才需要交换
                if (i != j) {
                    steps.add(new SortFrame(arr, 6, i, j, pivot, low, high, 
                        "交换 arr[" + i + "] (" + arr[i] + ") 和 arr[" + j + "] (" + arr[j] + ")。将较小元素移到左侧。"));
                    int temp = arr[i];
                    arr[i] = arr[j];
                    arr[j] = temp;
                } else {
                    steps.add(new SortFrame(arr, 6, i, j, pivot, low, high, 
                        "i 和 j 相同，无需交换。"));
                }
            } else {
                // if arr[j] <= pivot (line 4 - 失败)
                steps.add(new SortFrame(arr, 4, i, j, pivot, low, high, 
                    "arr[" + j + "] (" + arr[j] + ") > Pivot (" + pivot + ")。保留在右侧（大于 Pivot 区域）。"));
            }
        }
        
        // swap(arr[i + 1], arr[high]) (line 7)
        int finalPos = i + 1;
        steps.add(new SortFrame(arr, 7, finalPos, high, pivot, low, high, 
            "最后一步：将 Pivot (" + pivot + ") 插入到位置 i+1 (" + finalPos + ")。"));
        
        int temp = arr[finalPos];
        arr[finalPos] = arr[high];
        arr[high] = temp;
        
        // return i + 1 (line 8)
        steps.add(new SortFrame(arr, 8, finalPos, high, pivot, low, high, 
            "Pivot 已归位。返回 Pivot 的最终索引 " + finalPos + "。"));
        
        return finalPos;
    }
}