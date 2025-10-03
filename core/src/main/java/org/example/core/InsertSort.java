package org.example.core;

public class InsertSort {
    public int[][] sort(int[] arr) {
        int n = arr.length;
        int[][] steps = new int[n * n][n]; // 最多 n*n 步
        int stepCount = 0;
        int[] copy = arr.clone();

        for (int i = 1; i < n; i++) {
            int key = copy[i];
            int j = i - 1;

            while (j >= 0 && copy[j] > key) {
                copy[j + 1] = copy[j];
                j--;
            }
            copy[j + 1] = key;

            steps[stepCount++] = copy.clone(); // 保存当前步骤
        }

        int[][] result = new int[stepCount][n];
        for (int i = 0; i < stepCount; i++) result[i] = steps[i];
        return result;
    }
}