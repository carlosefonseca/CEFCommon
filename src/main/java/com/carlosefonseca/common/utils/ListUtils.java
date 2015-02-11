package com.carlosefonseca.common.utils;

import android.os.Build;
import android.util.SparseArray;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.carlosefonseca.common.utils.CodeUtils.LONG_L;
import static com.carlosefonseca.common.utils.CodeUtils.SIDE_T;

@SuppressWarnings("UnusedDeclaration")
public final class ListUtils {

    private ListUtils() {}

    @Contract("null -> null")
    public static <T> T first(@Nullable List<T> list) {
        //noinspection ConstantConditions
        return list == null || list.isEmpty() ? null : list.get(0);
    }

    @Contract("null -> null")
    public static <T> T last(@Nullable List<T> list) {
        //noinspection ConstantConditions
        return list == null || list.isEmpty() ? null : list.get(list.size() - 1);
    }

    @Contract("null,_ -> null")
    public static <T> List<T> firsts(List<T> list, int length) {
        //noinspection ConstantConditions
        return list == null ? null : list.subList(0, Math.min(list.size(), length));
    }

    @Contract("null,_ -> null")
    public static <T> List<T> lasts(List<T> list, int length) {
        //noinspection ConstantConditions
        return list == null ? null : list.subList(Math.max(list.size() - length, 0), list.size());
    }

    /**
     * Moves an element to the index 0. If the element doesn't exist or already is at index 0,
     * nothing is done to the list.
     * Rotates right a subset of the list from index 0 to the element.
     */
    public static <T> void moveToTop(List<T> list, T element) {
        final int i = list.indexOf(element);
        if (i > 0) Collections.rotate(list.subList(0, i + 1), 1);
    }

    /**
     * Returns a list containing all objects.
     */
    @SafeVarargs
    public static <T> ArrayList<T> list(T... objects) {
        ArrayList<T> list = new ArrayList<>();
        Collections.addAll(list, objects);
        return list;
    }

    /**
     * Merges elements of all lists into a single list.
     * @see #list(Object[])
     */
    @SafeVarargs
    public static <T> ArrayList<T> merge(Collection<T>... lists) {
        ArrayList<T> list = new ArrayList<>();
        for (Collection<T> ts : lists) list.addAll(ts);
        return list;
    }

    /**
     * Returns a copy of the specified list with object appended to it.
     * (Not a copy if original is a List and supports adding).
     */
    @SafeVarargs
    public static <T,L extends List<T>> L add(Collection<T> list, T... objects) {
        if (list instanceof List) {
            try {
                list.addAll(Arrays.asList(objects));
                return (L) list;
            } catch (UnsupportedOperationException ignored) {
            }
        }
        final ArrayList<T> list1 = new ArrayList<>(list);
        list1.addAll(Arrays.asList(objects));
        return (L) list1;
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

    /**
     * Iterates through the sparse array and adds the contents to the list.
     *
     * @param list  The list that will receive the items.
     * @param array The sparse array that contains the elements.
     */
    public static <T> ArrayList<Integer> sparseArrayKeysToList(SparseArray<? extends T> array) {
        ArrayList<Integer> keys = new ArrayList<>(array.size());
        for (int i = 0; i < array.size(); i++) {
            keys.add(array.keyAt(i));
        }
        return keys;
    }

    public static void removeNullElements(ArrayList<?> arrayList) {
        for (int i = arrayList.size() - 1; i >= 0; i--) {
            if (arrayList.get(i) == null) {
                arrayList.remove(i);
            }
        }
    }

    /**
     * Returns an immutable empty list if the argument is <code>null</code>,
     * or the argument itself otherwise.
     *
     * @param <T> the element type
     * @param list the list, possibly <code>null</code>
     * @return an empty list if the argument is <code>null</code>
     */
    public static <T> List<T> emptyIfNull(final List<T> list) {
        return list == null ? Collections.<T>emptyList() : list;
    }

    /**
     * Returns either the passed in list, or if the list is {@code null},
     * the value of {@code defaultList}.
     *
     * @param <T> the element type
     * @param list  the list, possibly {@code null}
     * @param defaultList  the returned values if list is {@code null}
     * @return an empty list if the argument is <code>null</code>
     * @since 4.0
     */
    public static <T> List<T> defaultIfNull(final List<T> list, final List<T> defaultList) {
        return list == null ? defaultList : list;
    }

    public static <T> List<T> defaultIfNull(@Nullable List<T> list) {
        return list == null ? new ArrayList<T>() : list;
    }

    public static <T, C extends Collection<T>> ArrayList<T> flatten(Collection<C> listOfLists) {
        final ArrayList<T> list = new ArrayList<>();
        for (Collection<T> listOfT : listOfLists) list.addAll(listOfT);
        return list;
    }

    public static class ListComparator<T> {
        private static final java.lang.String TAG = CodeUtils.getTag(ListComparator.class);

        public final List<T> newObjects;  // to create
        public final List<T> sameObjects; // to do nothing
        @Nullable public final List<T> updatedObjects; // to do nothing
        public final List<T> oldObjects;  // to delete

        protected ListComparator(List<T> newObjects, List<T> sameObjects, List<T> oldObjects, @Nullable List<T> updatedObjects) {
            this.newObjects = newObjects;
            this.sameObjects = sameObjects;
            this.updatedObjects = updatedObjects;
            this.oldObjects = oldObjects;
        }

        /**
         * Processes two lists and separates the items only on the first, the items only on the second and items on both. If an
         * item is on both, uses {@link java.lang.Comparable#compareTo(Object)} to check if the item on the second is greater than
         * the one on the first list.
         *
         * @param <T> Should implement {@link java.lang.Object#equals(Object)}
         */
        public static <T extends Comparable<T>> ListComparator<T> computeWithUpdates(Collection<T> existingStuff,
                                                                                     Collection<T> newStuff) {
            ArrayList<T> oldObjects = new ArrayList<>(existingStuff);
            ArrayList<T> newObjects = new ArrayList<>();
            ArrayList<T> updatedObjects = new ArrayList<>();
            ArrayList<T> sameObjects = new ArrayList<>();

            for (T t : newStuff) {
                final int i = oldObjects.indexOf(t);
                if (i < 0) {
                    newObjects.add(t);
                } else {
                    final T oldT = oldObjects.remove(i);
                    final int compareTo = oldT.compareTo(t);
                    if (compareTo == 0) {
                        sameObjects.add(t);
                    } else if (compareTo > 0) {
                        updatedObjects.add(t);
                    } else {
                        Log.w(TAG, "Existing object " + oldT + " is newer than 'new' object " + t);
                    }
                }
            }
            return new ListComparator<>(newObjects, sameObjects, oldObjects, updatedObjects);
        }

        protected void handleEqualObjects(T oldT, T t) {
            sameObjects.add(t);
        }

        /**
         * Processes two lists and separates the items only on the first, the items only on the second and items on both.
         *
         * @param <T> Should implement {@link java.lang.Object#equals(Object)}
         * @see {@link #computeWithUpdates(java.util.Collection, java.util.Collection)}
         */
        public static <T> ListComparator<T> compute(Collection<T> existingStuff, Collection<T> newStuff) {
            ArrayList<T> oldObjects = new ArrayList<>(existingStuff);
            ArrayList<T> newObjects = new ArrayList<>();
            ArrayList<T> sameObjects = new ArrayList<>();

            for (T t : newStuff) {
                if (oldObjects.remove(t)) {
                    sameObjects.add(t);
                } else {
                    newObjects.add(t);
                }
            }
            return new ListComparator<>(newObjects, sameObjects, oldObjects, null);
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
            return new ListComparator<>(newObjects, sameObjects, oldObjects, null);
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

        public boolean hasChanges() {
            return !newObjects.isEmpty() || !oldObjects.isEmpty() ||
                   (updatedObjects != null && !updatedObjects.isEmpty());
        }
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public static class ListComparator2<T extends Comparable<T>> {
        private static final java.lang.String TAG = CodeUtils.getTag(ListComparator.class);

        public final List<T> newObjects = new ArrayList<>();  // to create
        public final List<T> sameObjects = new ArrayList<>(); // to do nothing
        public final List<T> updatedObjects = new ArrayList<>(); // to do nothing
        public final List<T> oldObjects;  // to delete

        public ListComparator2(Collection<T> oldItems) {
            oldObjects = new ArrayList<>(oldItems);
        }

        /**
         * Processes two lists and separates the items only on the first, the items only on the second and items on both. If an
         * item is on both, uses {@link java.lang.Comparable#compareTo(Object)} to check if the item on the second is greater
         * than
         * the one on the first list.
         */
        public ListComparator2<T> compare(Collection<T> newStuff) {
            for (T t : newStuff) {
                final int i = oldObjects.indexOf(t);
                if (i < 0) {
                    newObjects.add(t);
                } else {
                    // object already exists by equals()
                    final T oldT = oldObjects.remove(i);
                    final int compareTo = oldT.compareTo(t);
                    if (compareTo == 0) {
                        handleEqualObjects(oldT, t);
                    } else if (compareTo < 0) { // oldT is less than (new) t
                        updatedObjects.add(t);
                    } else {
                        Log.w(TAG, "Existing object " + oldT + " is newer than 'new' object " + t);
                    }
                }
            }
            return this;
        }

        protected void handleEqualObjects(T oldT, T t) {
            sameObjects.add(t);
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

    public interface Id<T> {
        T getId();
    }

    public static <T> ArrayList<T> getIds(Iterable<? extends Id<T>> list) {
        final ArrayList<T> integers = new ArrayList<>();
        for (Id item : list) //noinspection unchecked
            integers.add((T) item.getId());
        return integers;
    }

    public static boolean containsIgnoreCase(Collection<String> list, @Nullable String string) {
        for (String s : list) {
            if (s.equalsIgnoreCase(string)) {
                return true;
            }
        }
        return false;
    }
}
