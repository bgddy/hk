package org.example.core;

import java.util.ArrayList;
import java.util.List;

public class InsertSort {
    

    public List<SortFrame> sort(int[] arr) {
        List<SortFrame> steps = new ArrayList<>();
        int n = arr.length;
        int[] copy = arr.clone();
        steps.add(new SortFrame(copy, 0, -1, -1, -1));
        for (int i = 1; i < n; i++) {
            steps.add(new SortFrame(copy, 0, i, -1, -1));

            int key = copy[i];
            int j = i - 1;
            steps.add(new SortFrame(copy, 1, i, j, i));
            steps.add(new SortFrame(copy, 2, i, j, i));

            while (j >= 0 && copy[j] > key) {
                copy[j + 1] = copy[j];
                steps.add(new SortFrame(copy, 3, i, j, j + 1));      
                j--;              
                steps.add(new SortFrame(copy, 4, i, j, i));
                steps.add(new SortFrame(copy, 2, i, j, i));
            }
            
            copy[j + 1] = key;
            steps.add(new SortFrame(copy, 5, i, j, j + 1));
        }
        steps.add(new SortFrame(copy, 0, -1, -1, -1));
        
        return steps;
    }
}