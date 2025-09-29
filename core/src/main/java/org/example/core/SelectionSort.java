package org.example.core;

public class SelectionSort {
    public int[][] sort(int[] arr) {
        int n = arr.length;
        int[][] steps = new int[n * n][n]; // 最多 n*n 步
        int stepCount = 0;

        int[] copy = arr.clone();
        for (int i = 0; i < n - 1; i++) {
            int minIdx = i;
            for (int j = i + 1; j < n; j++) {
                if (copy[j] < copy[minIdx]) minIdx = j;
            }
            int temp = copy[i];
            copy[i] = copy[minIdx];
            copy[minIdx] = temp;

            steps[stepCount++] = copy.clone(); // 每次交换记录
        }

        int[][] result = new int[stepCount][n];
        for (int i = 0; i < stepCount; i++) result[i] = steps[i];
        return result;
    }
}