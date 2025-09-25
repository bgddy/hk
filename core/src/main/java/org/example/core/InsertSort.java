package org.example.core;

public class InsertSort {
    public int[][] sort(int[] arr)
    {
        int n = arr.length;
        int[][] steps = new int[n][n];
        int stepCount = 0 ;
        int[] copy = arr.clone();

        //排序算法实现
        for(int i = 1; i < n ; i++)
        {
         int tempecord = copy[i];
         int j = i - 1;
         while( j >= 0 && tempecord  < copy[j])
         {
                copy[j+1] = copy[j];
                j = j - 1;
         }
            copy[j+1] = tempecord ;
            steps[stepCount] = copy.clone();
            stepCount++;
        }
       return steps;
    }
}
