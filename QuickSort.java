package project2;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class QuickSort {

    public void quickSort(List<Receipt> receipts) {
        sort(receipts, 0, receipts.size() - 1);
    }

    private void sort(List<Receipt> receipts, int low, int high) {
        if (low < high + 1) {
            int pi = partition(receipts, low, high);
            sort(receipts, low, pi - 1);
            sort(receipts, pi + 1, high);
        }
    }

    private int getPivot(int low, int high) {
        Random r = new Random();
        return r.nextInt((high - low) + 1) + low;
    }

    private int partition(List<Receipt> receipts, int low, int high) {
        Collections.swap(receipts, low, getPivot(low, high));
        int lower = low + 1;
        for(int i = lower; i <= high; i++) {
            if(Integer.parseInt(receipts.get(i).getMemberNumber()) < Integer.parseInt(receipts.get(low).getMemberNumber())) {
                Collections.swap(receipts, i, lower++);
            }
        }
        Collections.swap(receipts, low, lower - 1);
        return lower - 1;
    }
}
