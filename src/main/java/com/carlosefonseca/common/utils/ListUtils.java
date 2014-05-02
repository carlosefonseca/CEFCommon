package com.carlosefonseca.common.utils;

import android.os.Build;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("UnusedDeclaration")
public final class ListUtils {
    private ListUtils() {}

    public static <T> T first(List<T> list) {
        return list.get(0);
    }

    public static <T> T last(List<T> list) {
        return list.get(list.size() - 1);
    }

    public static <T> List<T> firsts(List<T> list, int length) {
        return list.subList(0, Math.min(list.size(), length));
    }

    public static <T> List<T> lasts(List<T> list, int length) {
        return list.subList(Math.max(list.size() - length, 0), list.size());
    }

    public static <T> ArrayList<T> arrayListWithObjects(T... objects) {
        ArrayList<T> list = new ArrayList<T>();
        Collections.addAll(list, objects);
        return list;
    }

    public static <T> SparseArray<T> copySparseArray(SparseArray<T> original) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            return original.clone();
        } else {
            SparseArray<T> newArray = new SparseArray<T>(original.size());
            for (int i = 0; i < original.size(); i++) {
                newArray.put(original.keyAt(i), original.valueAt(i));
            }
            return newArray;
        }
    }

    /**
     * Iterates through the sparse array and adds the contents to the list.
     *
     * @param list  The list that will receive the items.
     * @param array The sparse array that contains the elements.
     */
    public static <T> List<T> sparseArrayToList(List<T> list, SparseArray<? extends T> array) {
        for (int i = 0; i < array.size(); i++) {
            list.add(array.valueAt(i));
        }
        return list;
    }

    public static void removeNullElements(ArrayList<?> arrayList) {
        for (int i = arrayList.size() - 1; i >= 0; i--) {
            if (arrayList.get(i) == null) {
                arrayList.remove(i);
            }
        }
    }
}
