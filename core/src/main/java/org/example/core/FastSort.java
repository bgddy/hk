package org.example.core;

public class FastSort {
    public int selectPivot(int left, int right)
    {
        return ( left + right) / 2;
    }

    public int partition(int[] arr,int left,int right)
    {
        int l = left;
        int r = right;
        int temp = arr[r];
        while( l != r)
        {
            while( arr[l] <= temp && r > 1){ l++; }
            if( l < r){ arr[r] = arr[l]; r--;}
            while( arr[r] >= temp && r > 1){ r--;}
            if( l < r){ arr[l] = arr[r]; l++;}
        }
        arr[l] = temp;
        return l;
    }

    public void sort(int[] arr,int left,int right){
        int n = arr.length;
        int[][] steps = new int[n][n];
        int stepCount = 0 ;
        int[] copy = arr.clone();

        //算法实现
        if( left < right){ return; }














    }


}


