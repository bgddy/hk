package org.example.core;

public class SelectionSort {
    public int[][] sort(int[] arr) {
        int n = arr.length;
        // 修改1: 扩大数组容量，因为我们要记录内层循环的每一步
        // N=100时，可能需要约5000步，所以用 n*n 足够
        int[][] steps = new int[n * n][n]; 
        int stepCount = 0;

        int[] copy = arr.clone();
        for (int i = 0; i < n - 1; i++) {
            int minIdx = i;
            for (int j = i + 1; j < n; j++) {
                // 修改2: 【关键】记录“比较”的过程
                // 虽然数组此时没有变化，但这代表了CPU正在进行一次比较运算
                // 这会让动画“停顿”一下，模拟出扫描的耗时
                if (stepCount < steps.length) {
                    steps[stepCount++] = copy.clone();
                }

                if (copy[j] < copy[minIdx]) minIdx = j;
            }
            int temp = copy[i];
            copy[i] = copy[minIdx];
            copy[minIdx] = temp;

            // 记录交换后的结果
            if (stepCount < steps.length) {
                steps[stepCount++] = copy.clone(); 
            }
        }

        // 整理结果数组，去除多余的空位
        int[][] result = new int[stepCount][n];
        for (int i = 0; i < stepCount; i++) result[i] = steps[i];
        return result;
    }
}