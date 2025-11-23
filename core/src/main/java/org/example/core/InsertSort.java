package org.example.core;

public class InsertSort {
    public int[][] sort(int[] arr) {
        int n = arr.length;
        int[][] steps = new int[n * n][n]; 
        int stepCount = 0;
        int[] copy = arr.clone();

        for (int i = 1; i < n; i++) {
            int key = copy[i];
            int j = i - 1;

            // 修改: 在内层循环中记录每一步“移动”
            while (j >= 0 && copy[j] > key) {
                copy[j + 1] = copy[j];
                if (stepCount < steps.length) {
                    steps[stepCount++] = copy.clone(); // 记录移动动作
                }
                j--;
            }
            copy[j + 1] = key;

            if (stepCount < steps.length) {
                steps[stepCount++] = copy.clone(); 
            }
        }

        int[][] result = new int[stepCount][n];
        for (int i = 0; i < stepCount; i++) result[i] = steps[i];
        return result;
    }
}