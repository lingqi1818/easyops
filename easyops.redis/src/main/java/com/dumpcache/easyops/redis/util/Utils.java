package com.dumpcache.easyops.redis.util;

public class Utils {
    public static int[] formatToSlotsArray(int start, int end) {
        int size = end - start + 1;
        int[] slots = new int[size];
        for (int i = 0; i < size; i++) {
            slots[i] = start++;
        }
        return slots;
    }

    public static void main(String args[]) {
        int[] slots = Utils.formatToSlotsArray(1, 100);
        for (int slot : slots) {
            System.out.println(slot);
        }
    }
}
