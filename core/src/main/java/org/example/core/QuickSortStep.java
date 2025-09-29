package org.example.core;

public class QuickSortStep {
    public int[] arrayState;
    public int pivotIndex;
    public int swapIndex1;
    public int swapIndex2;
    public int leftBound;
    public int rightBound;

    public QuickSortStep(int[] arrayState, int pivotIndex, int swapIndex1, int swapIndex2, int leftBound, int rightBound) {
        this.arrayState = arrayState;
        this.pivotIndex = pivotIndex;
        this.swapIndex1 = swapIndex1;
        this.swapIndex2 = swapIndex2;
        this.leftBound = leftBound;
        this.rightBound = rightBound;
    }
}