package com.carlosefonseca.common.utils;

import android.os.Build;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.carlosefonseca.common.utils.CodeUtils.LONG_L;
import static com.carlosefonseca.common.utils.CodeUtils.SIDE_T;

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

    @SafeVarargs
    public static <T> ArrayList<T> arrayListWithObjects(T... objects) {
        ArrayList<T> list = new ArrayList<>();
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

    public static <T> List<T> defaultIfNull(List<T> list) {
        return list == null ? new ArrayList<T>() : list;
    }

    public static class ListComparator<T> {
        public final List<T> newObjects;  // to create
        public final List<T> sameObjects; // to do nothing
        public final List<T> oldObjects;  // to delete

        ListComparator(List<T> newObjects, List<T> sameObjects, List<T> oldObjects) {
            this.newObjects = newObjects;
            this.sameObjects = sameObjects;
            this.oldObjects = oldObjects;
        }

        public static <T> ListComparator<T> compute(Collection<T> existingStuff, Collection<T> newStuff) {
            ArrayList<T> oldObjects = new ArrayList<>(existingStuff);
            ArrayList<T> newObjects = new ArrayList<>();
            ArrayList<T> sameObjects = new ArrayList<>();

            for (T t : newStuff) {
                final boolean removed = oldObjects.remove(t);
                if (removed) {
                    sameObjects.add(t);
                } else {
                    newObjects.add(t);
                }
            }
            return new ListComparator<>(newObjects, sameObjects, oldObjects);
        }

        /**
         * Same as {@link #compute(java.util.Collection, java.util.Collection)} but on "sameObjects" the ones on the "existing
         * stuff" list are returned.
         */
        public static <T> ListComparator<T> compute2(Collection<T> existingStuff, Collection<T> newStuff) {
            ArrayList<T> oldObjects = new ArrayList<>(existingStuff);
            ArrayList<T> newObjects = new ArrayList<>();
            ArrayList<T> sameObjects = new ArrayList<>();

            for (T t : newStuff) {
                final int index = oldObjects.indexOf(t);
                if (index >= 0) {
                    sameObjects.add(oldObjects.remove(index));
                } else {
                    newObjects.add(t);
                }
            }
            return new ListComparator<>(newObjects, sameObjects, oldObjects);
        }

        @Override
        public String toString() {
            return "ListComparator of " + getTypeName() + "\n" +
                   SIDE_T + " NEW:  " + newObjects + "\n" +
                   SIDE_T + " SAME: " + sameObjects + "\n" +
                   LONG_L + " OLD:  " + oldObjects;
        }

        protected String getTypeName() {
            final T instance = !newObjects.isEmpty()
                               ? newObjects.get(0)
                               : !oldObjects.isEmpty() ? oldObjects.get(0) : !sameObjects.isEmpty() ? sameObjects.get(0) : null;
            return instance != null ? instance.getClass().getSimpleName() : "<Empty List?>";
        }
    }
}
