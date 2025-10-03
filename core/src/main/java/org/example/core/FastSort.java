package org.example.core;

public class FastSort {

    private QuickSortStep[] steps;
    private int stepCount;

    public QuickSortStep[] sort(int[] arr) {
        int n = arr.length;
        steps = new QuickSortStep[n * n];
        stepCount = 0;

        int[] copy = arr.clone();
        quickSort(copy, 0, n - 1);

        QuickSortStep[] result = new QuickSortStep[stepCount];
        for (int i = 0; i < stepCount; i++) result[i] = steps[i];
        return result;
    }

    private void quickSort(int[] arr, int left, int right) {
        if (left >= right) return;
        int pivotIndex = partition(arr, left, right);
        steps[stepCount++] = new QuickSortStep(arr.clone(), pivotIndex, -1, -1, left, right);
        quickSort(arr, left, pivotIndex - 1);
        quickSort(arr, pivotIndex + 1, right);
    }

    private int partition(int[] arr, int left, int right) {
        int pivot = arr[right];
        int i = left;
        for (int j = left; j < right; j++) {
            if (arr[j] <= pivot) {
                swap(arr, i, j, left, right);
                i++;
            }
        }
        swap(arr, i, right, left, right);
        return i;
    }

    private void swap(int[] arr, int i, int j, int left, int right) {
        if (i == j) return;
        int temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
        steps[stepCount++] = new QuickSortStep(arr.clone(), -1, i, j, left, right);
    }
}