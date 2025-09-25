package org.example.core;

public class SelectionSort {
    // 返回排序步骤，用于可视化
    public int[][] sort(int[] arr) {
        int n = arr.length;
        int[][] steps = new int[n][n]; // 简单记录每一步（可视化用）
        int stepCount = 0;

        int[] copy = arr.clone();
        for (int i = 0; i < n - 1; i++) {
            int minIdx = i;
            for (int j = i + 1; j < n; j++) {
                if (copy[j] < copy[minIdx]) {
                    minIdx = j;
                }
            }
            // 交换
            int temp = copy[i];
            copy[i] = copy[minIdx];
            copy[minIdx] = temp;

            // 记录步骤
            steps[stepCount++] = copy.clone();
        }

        return steps;
    }
}