/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.carlosefonseca.apache.commons.collections4;


import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.*;

/**
 *
 * SUB SET OF COMMONS
 *
 * Provides utility methods and decorators for {@link Collection} instances.
 * <p/>
 * NOTE: From 4.0, method parameters will take {@link Iterable} objects when possible.
 *
 * @version $Id: CollectionUtils.java 1540639 2013-11-11 08:54:12Z tn $
 * @since 1.0
 */
public class CollectionUtils {

    /**
     * Helper class to easily access cardinality properties of two collections.
     *
     * @param <O> the element type
     */
    private static class CardinalityHelper<O> {

        /** Contains the cardinality for each object in collection A. */
        final Map<O, Integer> cardinalityA;

        /** Contains the cardinality for each object in collection B. */
        final Map<O, Integer> cardinalityB;

        /**
         * Create a new CardinalityHelper for two collections.
         *
         * @param a the first collection
         * @param b the second collection
         */
        public CardinalityHelper(final Iterable<? extends O> a, final Iterable<? extends O> b) {
            cardinalityA = CollectionUtils.<O>getCardinalityMap(a);
            cardinalityB = CollectionUtils.<O>getCardinalityMap(b);
        }

        /**
         * Returns the maximum frequency of an object.
         *
         * @param obj the object
         * @return the maximum frequency of the object
         */
        public final int max(final Object obj) {
            return Math.max(freqA(obj), freqB(obj));
        }

        /**
         * Returns the minimum frequency of an object.
         *
         * @param obj the object
         * @return the minimum frequency of the object
         */
        public final int min(final Object obj) {
            return Math.min(freqA(obj), freqB(obj));
        }

        /**
         * Returns the frequency of this object in collection A.
         *
         * @param obj the object
         * @return the frequency of the object in collection A
         */
        public int freqA(final Object obj) {
            return getFreq(obj, cardinalityA);
        }

        /**
         * Returns the frequency of this object in collection B.
         *
         * @param obj the object
         * @return the frequency of the object in collection B
         */
        public int freqB(final Object obj) {
            return getFreq(obj, cardinalityB);
        }

        private final int getFreq(final Object obj, final Map<?, Integer> freqMap) {
            final Integer count = freqMap.get(obj);
            if (count != null) {
                return count.intValue();
            }
            return 0;
        }
    }

    /**
     * Helper class for set-related operations, e.g. union, subtract, intersection.
     *
     * @param <O> the element type
     */
    private static class SetOperationCardinalityHelper<O> extends CardinalityHelper<O> implements Iterable<O> {

        /** Contains the unique elements of the two collections. */
        private final Set<O> elements;

        /** Output collection. */
        private final List<O> newList;

        /**
         * Create a new set operation helper from the two collections.
         *
         * @param a the first collection
         * @param b the second collection
         */
        public SetOperationCardinalityHelper(final Iterable<? extends O> a, final Iterable<? extends O> b) {
            super(a, b);
            elements = new HashSet<O>();
            addAll(elements, a);
            addAll(elements, b);
            // the resulting list must contain at least each unique element, but may grow
            newList = new ArrayList<O>(elements.size());
        }

        public Iterator<O> iterator() {
            return elements.iterator();
        }

        /**
         * Add the object {@code count} times to the result collection.
         *
         * @param obj   the object to add
         * @param count the count
         */
        public void setCardinality(final O obj, final int count) {
            for (int i = 0; i < count; i++) {
                newList.add(obj);
            }
        }

        /**
         * Returns the resulting collection.
         *
         * @return the result
         */
        public Collection<O> list() {
            return newList;
        }

    }


    /**
     * Returns a {@link Collection} containing the union of the given
     * {@link Iterable}s.
     * <p/>
     * The cardinality of each element in the returned {@link Collection} will
     * be equal to the maximum of the cardinality of that element in the two
     * given {@link Iterable}s.
     *
     * @param a   the first collection, must not be null
     * @param b   the second collection, must not be null
     * @param <O> the generic type that is able to represent the types contained
     *            in both input collections.
     * @return the union of the two collections
     * @see Collection#addAll
     */
    public static <O> Collection<O> union(final Iterable<? extends O> a, final Iterable<? extends O> b) {
        final SetOperationCardinalityHelper<O> helper = new SetOperationCardinalityHelper<O>(a, b);
        for (final O obj : helper) {
            helper.setCardinality(obj, helper.max(obj));
        }
        return helper.list();
    }

    /**
     * Returns a {@link Collection} containing the intersection of the given
     * {@link Iterable}s.
     * <p/>
     * The cardinality of each element in the returned {@link Collection} will
     * be equal to the minimum of the cardinality of that element in the two
     * given {@link Iterable}s.
     *
     * @param a   the first collection, must not be null
     * @param b   the second collection, must not be null
     * @param <O> the generic type that is able to represent the types contained
     *            in both input collections.
     * @return the intersection of the two collections
     * @see Collection#retainAll
     * @see #containsAny
     */
    public static <O> Collection<O> intersection(final Iterable<? extends O> a, final Iterable<? extends O> b) {
        final SetOperationCardinalityHelper<O> helper = new SetOperationCardinalityHelper<O>(a, b);
        for (final O obj : helper) {
            helper.setCardinality(obj, helper.min(obj));
        }
        return helper.list();
    }

    /**
     * Returns a {@link Collection} containing the exclusive disjunction
     * (symmetric difference) of the given {@link Iterable}s.
     * <p/>
     * The cardinality of each element <i>e</i> in the returned
     * {@link Collection} will be equal to
     * <tt>max(cardinality(<i>e</i>,<i>a</i>),cardinality(<i>e</i>,<i>b</i>)) - min(cardinality(<i>e</i>,<i>a</i>),
     * cardinality(<i>e</i>,<i>b</i>))</tt>.
     * <p/>
     * This is equivalent to
     * <tt>{@link #subtract subtract}({@link #union union(a,b)},{@link #intersection intersection(a,b)})</tt>
     * or
     * <tt>{@link #union union}({@link #subtract subtract(a,b)},{@link #subtract subtract(b,a)})</tt>.
     *
     * @param a   the first collection, must not be null
     * @param b   the second collection, must not be null
     * @param <O> the generic type that is able to represent the types contained
     *            in both input collections.
     * @return the symmetric difference of the two collections
     */
    public static <O> Collection<O> disjunction(final Iterable<? extends O> a, final Iterable<? extends O> b) {
        final SetOperationCardinalityHelper<O> helper = new SetOperationCardinalityHelper<O>(a, b);
        for (final O obj : helper) {
            helper.setCardinality(obj, helper.max(obj) - helper.min(obj));
        }
        return helper.list();
    }


    /**
     * Returns <code>true</code> iff all elements of {@code coll2} are also contained
     * in {@code coll1}. The cardinality of values in {@code coll2} is not taken into account,
     * which is the same behavior as {@link Collection#containsAll(Collection)}.
     * <p/>
     * In other words, this method returns <code>true</code> iff the
     * {@link #intersection} of <i>coll1</i> and <i>coll2</i> has the same cardinality as
     * the set of unique values from {@code coll2}. In case {@code coll2} is empty, {@code true}
     * will be returned.
     * <p/>
     * This method is intended as a replacement for {@link Collection#containsAll(Collection)}
     * with a guaranteed runtime complexity of {@code O(n + m)}. Depending on the type of
     * {@link Collection} provided, this method will be much faster than calling
     * {@link Collection#containsAll(Collection)} instead, though this will come at the
     * cost of an additional space complexity O(n).
     *
     * @param coll1 the first collection, must not be null
     * @param coll2 the second collection, must not be null
     * @return <code>true</code> iff the intersection of the collections has the same cardinality
     * as the set of unique elements from the second collection
     * @since 4.0
     */
    public static boolean containsAll(final Collection<?> coll1, final Collection<?> coll2) {
        if (coll2.isEmpty()) {
            return true;
        } else {
            final Iterator<?> it = coll1.iterator();
            final Set<Object> elementsAlreadySeen = new HashSet<Object>();
            for (final Object nextElement : coll2) {
                if (elementsAlreadySeen.contains(nextElement)) {
                    continue;
                }

                boolean foundCurrentElement = false;
                while (it.hasNext()) {
                    final Object p = it.next();
                    elementsAlreadySeen.add(p);
                    if (nextElement == null ? p == null : nextElement.equals(p)) {
                        foundCurrentElement = true;
                        break;
                    }
                }

                if (foundCurrentElement) {
                    continue;
                } else {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * Returns <code>true</code> iff at least one element is in both collections.
     * <p/>
     * In other words, this method returns <code>true</code> iff the
     * {@link #intersection} of <i>coll1</i> and <i>coll2</i> is not empty.
     *
     * @param coll1 the first collection, must not be null
     * @param coll2 the second collection, must not be null
     * @return <code>true</code> iff the intersection of the collections is non-empty
     * @see #intersection
     * @since 2.1
     */
    public static boolean containsAny(final Collection<?> coll1, final Collection<?> coll2) {
        if (coll1.size() < coll2.size()) {
            for (final Object aColl1 : coll1) {
                if (coll2.contains(aColl1)) {
                    return true;
                }
            }
        } else {
            for (final Object aColl2 : coll2) {
                if (coll1.contains(aColl2)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns a {@link Map} mapping each unique element in the given
     * {@link Collection} to an {@link Integer} representing the number
     * of occurrences of that element in the {@link Collection}.
     * <p/>
     * Only those elements present in the collection will appear as
     * keys in the map.
     *
     * @param <O>  the type of object in the returned {@link Map}. This is a super type of <I>.
     * @param coll the collection to get the cardinality map for, must not be null
     * @return the populated cardinality map
     */
    public static <O> Map<O, Integer> getCardinalityMap(final Iterable<? extends O> coll) {
        final Map<O, Integer> count = new HashMap<O, Integer>();
        for (final O obj : coll) {
            final Integer c = count.get(obj);
            if (c == null) {
                count.put(obj, Integer.valueOf(1));
            } else {
                count.put(obj, Integer.valueOf(c.intValue() + 1));
            }
        }
        return count;
    }

    /**
     * Returns <tt>true</tt> iff <i>a</i> is a sub-collection of <i>b</i>,
     * that is, iff the cardinality of <i>e</i> in <i>a</i> is less than or
     * equal to the cardinality of <i>e</i> in <i>b</i>, for each element <i>e</i>
     * in <i>a</i>.
     *
     * @param a the first (sub?) collection, must not be null
     * @param b the second (super?) collection, must not be null
     * @return <code>true</code> iff <i>a</i> is a sub-collection of <i>b</i>
     * @see #isProperSubCollection
     * @see Collection#containsAll
     */
    public static boolean isSubCollection(final Collection<?> a, final Collection<?> b) {
        final CardinalityHelper<Object> helper = new CardinalityHelper<Object>(a, b);
        for (final Object obj : a) {
            if (helper.freqA(obj) > helper.freqB(obj)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns <tt>true</tt> iff <i>a</i> is a <i>proper</i> sub-collection of <i>b</i>,
     * that is, iff the cardinality of <i>e</i> in <i>a</i> is less
     * than or equal to the cardinality of <i>e</i> in <i>b</i>,
     * for each element <i>e</i> in <i>a</i>, and there is at least one
     * element <i>f</i> such that the cardinality of <i>f</i> in <i>b</i>
     * is strictly greater than the cardinality of <i>f</i> in <i>a</i>.
     * <p/>
     * The implementation assumes
     * <ul>
     * <li><code>a.size()</code> and <code>b.size()</code> represent the
     * total cardinality of <i>a</i> and <i>b</i>, resp. </li>
     * <li><code>a.size() < Integer.MAXVALUE</code></li>
     * </ul>
     *
     * @param a the first (sub?) collection, must not be null
     * @param b the second (super?) collection, must not be null
     * @return <code>true</code> iff <i>a</i> is a <i>proper</i> sub-collection of <i>b</i>
     * @see #isSubCollection
     * @see Collection#containsAll
     */
    public static boolean isProperSubCollection(final Collection<?> a, final Collection<?> b) {
        return a.size() < b.size() && CollectionUtils.isSubCollection(a, b);
    }

    /**
     * Returns <tt>true</tt> iff the given {@link Collection}s contain
     * exactly the same elements with exactly the same cardinalities.
     * <p/>
     * That is, iff the cardinality of <i>e</i> in <i>a</i> is
     * equal to the cardinality of <i>e</i> in <i>b</i>,
     * for each element <i>e</i> in <i>a</i> or <i>b</i>.
     *
     * @param a the first collection, must not be null
     * @param b the second collection, must not be null
     * @return <code>true</code> iff the collections contain the same elements with the same cardinalities.
     */
    public static boolean isEqualCollection(final Collection<?> a, final Collection<?> b) {
        if (a.size() != b.size()) {
            return false;
        }
        final CardinalityHelper<Object> helper = new CardinalityHelper<Object>(a, b);
        if (helper.cardinalityA.size() != helper.cardinalityB.size()) {
            return false;
        }
        for (final Object obj : helper.cardinalityA.keySet()) {
            if (helper.freqA(obj) != helper.freqB(obj)) {
                return false;
            }
        }
        return true;
    }


    /**
     * Finds the first element in the given collection which matches the given predicate.
     * <p/>
     * If the input collection or predicate is null, or no element of the collection
     * matches the predicate, null is returned.
     *
     * @param <T>        the type of object the {@link Iterable} contains
     * @param collection the collection to search, may be null
     * @param predicate  the predicate to use, may be null
     * @return the first element of the collection which matches the predicate or null if none could be found
     */
    public static <T> T find(final Iterable<T> collection, final Predicate<? super T> predicate) {
        if (collection != null && predicate != null) {
            for (final T item : collection) {
                if (predicate.evaluate(item)) {
                    return item;
                }
            }
        }
        return null;
    }

    /**
     * Executes the given closure on each element in the collection.
     * <p/>
     * If the input collection or closure is null, there is no change made.
     *
     * @param <T>        the type of object the {@link Iterable} contains
     * @param <C>        the closure type
     * @param collection the collection to get the input from, may be null
     * @param closure    the closure to perform, may be null
     * @return closure
     */
    public static <T, C extends Closure<? super T>> C forAllDo(final Iterable<T> collection, final C closure) {
        if (collection != null && closure != null) {
            for (final T element : collection) {
                closure.execute(element);
            }
        }
        return closure;
    }

    /**
     * Executes the given closure on each element in the collection.
     * <p/>
     * If the input collection or closure is null, there is no change made.
     *
     * @param <T>      the type of object the {@link Iterator} contains
     * @param <C>      the closure type
     * @param iterator the iterator to get the input from, may be null
     * @param closure  the closure to perform, may be null
     * @return closure
     * @since 4.0
     */
    public static <T, C extends Closure<? super T>> C forAllDo(final Iterator<T> iterator, final C closure) {
        if (iterator != null && closure != null) {
            while (iterator.hasNext()) {
                closure.execute(iterator.next());
            }
        }
        return closure;
    }

    /**
     * Executes the given closure on each but the last element in the collection.
     * <p/>
     * If the input collection or closure is null, there is no change made.
     *
     * @param <T>        the type of object the {@link Iterable} contains
     * @param <C>        the closure type
     * @param collection the collection to get the input from, may be null
     * @param closure    the closure to perform, may be null
     * @return the last element in the collection, or null if either collection or closure is null
     * @since 4.0
     */
    public static <T, C extends Closure<? super T>> T forAllButLastDo(final Iterable<T> collection, final C closure) {
        return collection != null && closure != null ? forAllButLastDo(collection.iterator(), closure) : null;
    }

    /**
     * Executes the given closure on each but the last element in the collection.
     * <p/>
     * If the input collection or closure is null, there is no change made.
     *
     * @param <T>      the type of object the {@link Collection} contains
     * @param <C>      the closure type
     * @param iterator the iterator to get the input from, may be null
     * @param closure  the closure to perform, may be null
     * @return the last element in the collection, or null if either iterator or closure is null
     * @since 4.0
     */
    public static <T, C extends Closure<? super T>> T forAllButLastDo(final Iterator<T> iterator, final C closure) {
        if (iterator != null && closure != null) {
            while (iterator.hasNext()) {
                final T element = iterator.next();
                if (iterator.hasNext()) {
                    closure.execute(element);
                } else {
                    return element;
                }
            }
        }
        return null;
    }

    /**
     * Filter the collection by applying a Predicate to each element. If the
     * predicate returns false, remove the element.
     * <p/>
     * If the input collection or predicate is null, there is no change made.
     *
     * @param <T>        the type of object the {@link Iterable} contains
     * @param collection the collection to get the input from, may be null
     * @param predicate  the predicate to use as a filter, may be null
     * @return true if the collection is modified by this call, false otherwise.
     */
    public static <T> boolean filter(final Iterable<T> collection, final Predicate<? super T> predicate) {
        boolean result = false;
        if (collection != null && predicate != null) {
            for (final Iterator<T> it = collection.iterator(); it.hasNext(); ) {
                if (!predicate.evaluate(it.next())) {
                    it.remove();
                    result = true;
                }
            }
        }
        return result;
    }


    /**
     * Counts the number of elements in the input collection that match the
     * predicate.
     * <p/>
     * A <code>null</code> collection or predicate matches no elements.
     *
     * @param <C>       the type of object the {@link Iterable} contains
     * @param input     the {@link Iterable} to get the input from, may be null
     * @param predicate the predicate to use, may be null
     * @return the number of matches for the predicate in the collection
     */
    public static <C> int countMatches(final Iterable<C> input, final Predicate<? super C> predicate) {
        int count = 0;
        if (input != null && predicate != null) {
            for (final C o : input) {
                if (predicate.evaluate(o)) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Answers true if a predicate is true for at least one element of a
     * collection.
     * <p/>
     * A <code>null</code> collection or predicate returns false.
     *
     * @param <C>       the type of object the {@link Iterable} contains
     * @param input     the {@link Iterable} to get the input from, may be null
     * @param predicate the predicate to use, may be null
     * @return true if at least one element of the collection matches the predicate
     */
    public static <C> boolean exists(final Iterable<C> input, final Predicate<? super C> predicate) {
        if (input != null && predicate != null) {
            for (final C o : input) {
                if (predicate.evaluate(o)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Answers true if a predicate is true for every element of a
     * collection.
     * <p/>
     * A <code>null</code> predicate returns false.<br/>
     * A <code>null</code> or empty collection returns true.
     *
     * @param <C>       the type of object the {@link Iterable} contains
     * @param input     the {@link Iterable} to get the input from, may be null
     * @param predicate the predicate to use, may be null
     * @return true if every element of the collection matches the predicate or if the
     * collection is empty, false otherwise
     * @since 4.0
     */
    public static <C> boolean matchesAll(final Iterable<C> input, final Predicate<? super C> predicate) {
        if (predicate == null) {
            return false;
        }

        if (input != null) {
            for (final C o : input) {
                if (!predicate.evaluate(o)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Selects all elements from input collection which match the given
     * predicate into an output collection.
     * <p/>
     * A <code>null</code> predicate matches no elements.
     *
     * @param <O>             the type of object the {@link Iterable} contains
     * @param inputCollection the collection to get the input from, may not be null
     * @param predicate       the predicate to use, may be null
     * @return the elements matching the predicate (new list)
     * @throws NullPointerException if the input collection is null
     */
    public static <O> Collection<O> select(final Iterable<? extends O> inputCollection,
                                           final Predicate<? super O> predicate) {
        final Collection<O> answer = inputCollection instanceof Collection<?>
                                     ? new ArrayList<O>(((Collection<?>) inputCollection).size())
                                     : new ArrayList<O>();
        return select(inputCollection, predicate, answer);
    }

    /**
     * Selects all elements from input collection which match the given
     * predicate and adds them to outputCollection.
     * <p/>
     * If the input collection or predicate is null, there is no change to the
     * output collection.
     *
     * @param <O>              the type of object the {@link Iterable} contains
     * @param <R>              the type of the output {@link Collection}
     * @param inputCollection  the collection to get the input from, may be null
     * @param predicate        the predicate to use, may be null
     * @param outputCollection the collection to output into, may not be null if the inputCollection
     *                         and predicate or not null
     * @return the outputCollection
     */
    public static <O, R extends Collection<? super O>> R select(final Iterable<? extends O> inputCollection,
                                                                final Predicate<? super O> predicate,
                                                                final R outputCollection) {

        if (inputCollection != null && predicate != null) {
            for (final O item : inputCollection) {
                if (predicate.evaluate(item)) {
                    outputCollection.add(item);
                }
            }
        }
        return outputCollection;
    }

    /**
     * Selects all elements from inputCollection which don't match the given
     * predicate into an output collection.
     * <p/>
     * If the input predicate is <code>null</code>, the result is an empty
     * list.
     *
     * @param <O>             the type of object the {@link Iterable} contains
     * @param inputCollection the collection to get the input from, may not be null
     * @param predicate       the predicate to use, may be null
     * @return the elements <b>not</b> matching the predicate (new list)
     * @throws NullPointerException if the input collection is null
     */
    public static <O> Collection<O> selectRejected(final Iterable<? extends O> inputCollection,
                                                   final Predicate<? super O> predicate) {
        final Collection<O> answer = inputCollection instanceof Collection<?>
                                     ? new ArrayList<O>(((Collection<?>) inputCollection).size())
                                     : new ArrayList<O>();
        return selectRejected(inputCollection, predicate, answer);
    }

    /**
     * Selects all elements from inputCollection which don't match the given
     * predicate and adds them to outputCollection.
     * <p/>
     * If the input predicate is <code>null</code>, no elements are added to
     * <code>outputCollection</code>.
     *
     * @param <O>              the type of object the {@link Iterable} contains
     * @param <R>              the type of the output {@link Collection}
     * @param inputCollection  the collection to get the input from, may be null
     * @param predicate        the predicate to use, may be null
     * @param outputCollection the collection to output into, may not be null if the inputCollection
     *                         and predicate or not null
     * @return outputCollection
     */
    public static <O, R extends Collection<? super O>> R selectRejected(final Iterable<? extends O> inputCollection,
                                                                        final Predicate<? super O> predicate,
                                                                        final R outputCollection) {

        if (inputCollection != null && predicate != null) {
            for (final O item : inputCollection) {
                if (!predicate.evaluate(item)) {
                    outputCollection.add(item);
                }
            }
        }
        return outputCollection;
    }


    //-----------------------------------------------------------------------

    /**
     * Adds an element to the collection unless the element is null.
     *
     * @param <T>        the type of object the {@link Collection} contains
     * @param collection the collection to add to, must not be null
     * @param object     the object to add, if null it will not be added
     * @return true if the collection changed
     * @throws NullPointerException if the collection is null
     * @since 3.2
     */
    public static <T> boolean addIgnoreNull(final Collection<T> collection, final T object) {
        if (collection == null) {
            throw new NullPointerException("The collection must not be null");
        }
        return object != null && collection.add(object);
    }

    /**
     * Adds all elements in the {@link Iterable} to the given collection. If the
     * {@link Iterable} is a {@link Collection} then it is cast and will be
     * added using {@link Collection#addAll(Collection)} instead of iterating.
     *
     * @param <C>        the type of object the {@link Collection} contains
     * @param collection the collection to add to, must not be null
     * @param iterable   the iterable of elements to add, must not be null
     * @return a boolean indicating whether the collection has changed or not.
     * @throws NullPointerException if the collection or iterator is null
     */
    public static <C> boolean addAll(final Collection<C> collection, final Iterable<? extends C> iterable) {
        if (iterable instanceof Collection<?>) {
            return collection.addAll((Collection<? extends C>) iterable);
        }
        return addAll(collection, iterable.iterator());
    }

    /**
     * Adds all elements in the iteration to the given collection.
     *
     * @param <C>        the type of object the {@link Collection} contains
     * @param collection the collection to add to, must not be null
     * @param iterator   the iterator of elements to add, must not be null
     * @return a boolean indicating whether the collection has changed or not.
     * @throws NullPointerException if the collection or iterator is null
     */
    public static <C> boolean addAll(final Collection<C> collection, final Iterator<? extends C> iterator) {
        boolean changed = false;
        while (iterator.hasNext()) {
            changed |= collection.add(iterator.next());
        }
        return changed;
    }

    /**
     * Adds all elements in the enumeration to the given collection.
     *
     * @param <C>         the type of object the {@link Collection} contains
     * @param collection  the collection to add to, must not be null
     * @param enumeration the enumeration of elements to add, must not be null
     * @return {@code true} if the collections was changed, {@code false} otherwise
     * @throws NullPointerException if the collection or enumeration is null
     */
    public static <C> boolean addAll(final Collection<C> collection, final Enumeration<? extends C> enumeration) {
        boolean changed = false;
        while (enumeration.hasMoreElements()) {
            changed |= collection.add(enumeration.nextElement());
        }
        return changed;
    }

    /**
     * Adds all elements in the array to the given collection.
     *
     * @param <C>        the type of object the {@link Collection} contains
     * @param collection the collection to add to, must not be null
     * @param elements   the array of elements to add, must not be null
     * @return {@code true} if the collection was changed, {@code false} otherwise
     * @throws NullPointerException if the collection or array is null
     */
    public static <C> boolean addAll(final Collection<C> collection, final C[] elements) {
        boolean changed = false;
        for (final C element : elements) {
            changed |= collection.add(element);
        }
        return changed;
    }

    /**
     * Returns the <code>index</code>-th value in {@link Iterator}, throwing
     * <code>IndexOutOfBoundsException</code> if there is no such element.
     * <p/>
     * The Iterator is advanced to <code>index</code> (or to the end, if
     * <code>index</code> exceeds the number of entries) as a side effect of this method.
     *
     * @param iterator the iterator to get a value from
     * @param index    the index to get
     * @param <T>      the type of object in the {@link Iterator}
     * @return the object at the specified index
     * @throws IndexOutOfBoundsException if the index is invalid
     * @throws IllegalArgumentException  if the object type is invalid
     */
    public static <T> T get(final Iterator<T> iterator, final int index) {
        int i = index;
        checkIndexBounds(i);
        while (iterator.hasNext()) {
            i--;
            if (i == -1) {
                return iterator.next();
            }
            iterator.next();
        }
        throw new IndexOutOfBoundsException("Entry does not exist: " + i);
    }

    /**
     * Ensures an index is not negative.
     *
     * @param index the index to check.
     * @throws IndexOutOfBoundsException if the index is negative.
     */
    private static void checkIndexBounds(final int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException("Index cannot be negative: " + index);
        }
    }

    /**
     * Returns the <code>index</code>-th value in the <code>iterable</code>'s {@link Iterator}, throwing
     * <code>IndexOutOfBoundsException</code> if there is no such element.
     * <p/>
     * If the {@link Iterable} is a {@link List}, then it will use {@link List#get(int)}.
     *
     * @param iterable the {@link Iterable} to get a value from
     * @param index    the index to get
     * @param <T>      the type of object in the {@link Iterable}.
     * @return the object at the specified index
     * @throws IndexOutOfBoundsException if the index is invalid
     */
    public static <T> T get(final Iterable<T> iterable, final int index) {
        checkIndexBounds(index);
        if (iterable instanceof List<?>) {
            return ((List<T>) iterable).get(index);
        }
        return get(iterable.iterator(), index);
    }

    /**
     * Returns the <code>index</code>-th value in <code>object</code>, throwing
     * <code>IndexOutOfBoundsException</code> if there is no such element or
     * <code>IllegalArgumentException</code> if <code>object</code> is not an
     * instance of one of the supported types.
     * <p/>
     * The supported types, and associated semantics are:
     * <ul>
     * <li> Map -- the value returned is the <code>Map.Entry</code> in position
     * <code>index</code> in the map's <code>entrySet</code> iterator,
     * if there is such an entry.</li>
     * <li> List -- this method is equivalent to the list's get method.</li>
     * <li> Array -- the <code>index</code>-th array entry is returned,
     * if there is such an entry; otherwise an <code>IndexOutOfBoundsException</code>
     * is thrown.</li>
     * <li> Collection -- the value returned is the <code>index</code>-th object
     * returned by the collection's default iterator, if there is such an element.</li>
     * <li> Iterator or Enumeration -- the value returned is the
     * <code>index</code>-th object in the Iterator/Enumeration, if there
     * is such an element.  The Iterator/Enumeration is advanced to
     * <code>index</code> (or to the end, if <code>index</code> exceeds the
     * number of entries) as a side effect of this method.</li>
     * </ul>
     *
     * @param object the object to get a value from
     * @param index  the index to get
     * @return the object at the specified index
     * @throws IndexOutOfBoundsException if the index is invalid
     * @throws IllegalArgumentException  if the object type is invalid
     */
    public static Object get(final Object object, final int index) {
        int i = index;
        if (i < 0) {
            throw new IndexOutOfBoundsException("Index cannot be negative: " + i);
        }
        if (object instanceof Map<?, ?>) {
            final Map<?, ?> map = (Map<?, ?>) object;
            final Iterator<?> iterator = map.entrySet().iterator();
            return get(iterator, i);
        } else if (object instanceof Object[]) {
            return ((Object[]) object)[i];
        } else if (object instanceof Iterator<?>) {
            final Iterator<?> it = (Iterator<?>) object;
            while (it.hasNext()) {
                i--;
                if (i == -1) {
                    return it.next();
                }
                it.next();
            }
            throw new IndexOutOfBoundsException("Entry does not exist: " + i);
        } else if (object instanceof Collection<?>) {
            final Iterator<?> iterator = ((Collection<?>) object).iterator();
            return get(iterator, i);
        } else if (object instanceof Enumeration<?>) {
            final Enumeration<?> it = (Enumeration<?>) object;
            while (it.hasMoreElements()) {
                i--;
                if (i == -1) {
                    return it.nextElement();
                } else {
                    it.nextElement();
                }
            }
            throw new IndexOutOfBoundsException("Entry does not exist: " + i);
        } else if (object == null) {
            throw new IllegalArgumentException("Unsupported object type: null");
        } else {
            try {
                return Array.get(object, i);
            } catch (final IllegalArgumentException ex) {
                throw new IllegalArgumentException("Unsupported object type: " + object.getClass().getName());
            }
        }
    }

    /**
     * Returns the <code>index</code>-th <code>Map.Entry</code> in the <code>map</code>'s <code>entrySet</code>,
     * throwing <code>IndexOutOfBoundsException</code> if there is no such element.
     *
     * @param <K>   the key type in the {@link Map}
     * @param <V>   the key type in the {@link Map}
     * @param map   the object to get a value from
     * @param index the index to get
     * @return the object at the specified index
     * @throws IndexOutOfBoundsException if the index is invalid
     */
    public static <K, V> Map.Entry<K, V> get(final Map<K, V> map, final int index) {
        checkIndexBounds(index);
        return get(map.entrySet(), index);
    }

    /**
     * Gets the size of the collection/iterator specified.
     * <p/>
     * This method can handles objects as follows
     * <ul>
     * <li>Collection - the collection size
     * <li>Map - the map size
     * <li>Array - the array size
     * <li>Iterator - the number of elements remaining in the iterator
     * <li>Enumeration - the number of elements remaining in the enumeration
     * </ul>
     *
     * @param object the object to get the size of, may be null
     * @return the size of the specified collection or 0 if the object was null
     * @throws IllegalArgumentException thrown if object is not recognised
     * @since 3.1
     */
    public static int size(final Object object) {
        if (object == null) {
            return 0;
        }
        int total = 0;
        if (object instanceof Map<?, ?>) {
            total = ((Map<?, ?>) object).size();
        } else if (object instanceof Collection<?>) {
            total = ((Collection<?>) object).size();
        } else if (object instanceof Object[]) {
            total = ((Object[]) object).length;
        } else if (object instanceof Iterator<?>) {
            final Iterator<?> it = (Iterator<?>) object;
            while (it.hasNext()) {
                total++;
                it.next();
            }
        } else if (object instanceof Enumeration<?>) {
            final Enumeration<?> it = (Enumeration<?>) object;
            while (it.hasMoreElements()) {
                total++;
                it.nextElement();
            }
        } else {
            try {
                total = Array.getLength(object);
            } catch (final IllegalArgumentException ex) {
                throw new IllegalArgumentException("Unsupported object type: " + object.getClass().getName());
            }
        }
        return total;
    }

    /**
     * Checks if the specified collection/array/iterator is empty.
     * <p/>
     * This method can handles objects as follows
     * <ul>
     * <li>Collection - via collection isEmpty
     * <li>Map - via map isEmpty
     * <li>Array - using array size
     * <li>Iterator - via hasNext
     * <li>Enumeration - via hasMoreElements
     * </ul>
     * <p/>
     * Note: This method is named to avoid clashing with
     * {@link #isEmpty(Collection)}.
     *
     * @param object the object to get the size of, may be null
     * @return true if empty or null
     * @throws IllegalArgumentException thrown if object is not recognised
     * @since 3.2
     */
    public static boolean sizeIsEmpty(final Object object) {
        if (object == null) {
            return true;
        } else if (object instanceof Collection<?>) {
            return ((Collection<?>) object).isEmpty();
        } else if (object instanceof Map<?, ?>) {
            return ((Map<?, ?>) object).isEmpty();
        } else if (object instanceof Object[]) {
            return ((Object[]) object).length == 0;
        } else if (object instanceof Iterator<?>) {
            return ((Iterator<?>) object).hasNext() == false;
        } else if (object instanceof Enumeration<?>) {
            return ((Enumeration<?>) object).hasMoreElements() == false;
        } else {
            try {
                return Array.getLength(object) == 0;
            } catch (final IllegalArgumentException ex) {
                throw new IllegalArgumentException("Unsupported object type: " + object.getClass().getName());
            }
        }
    }

    //-----------------------------------------------------------------------

    /**
     * Null-safe check if the specified collection is empty.
     * <p/>
     * Null returns true.
     *
     * @param coll the collection to check, may be null
     * @return true if empty or null
     * @since 3.2
     */
    public static boolean isEmpty(@Nullable final Collection<?> coll) {
        return coll == null || coll.isEmpty();
    }

    /**
     * Null-safe check if the specified collection is not empty.
     * <p/>
     * Null returns false.
     *
     * @param coll the collection to check, may be null
     * @return true if non-null and non-empty
     * @since 3.2
     */
    public static boolean isNotEmpty(@Nullable final Collection<?> coll) {
        return !isEmpty(coll);
    }

    //-----------------------------------------------------------------------

    /**
     * Reverses the order of the given array.
     *
     * @param array the array to reverse
     */
    public static void reverseArray(final Object[] array) {
        int i = 0;
        int j = array.length - 1;
        Object tmp;

        while (j > i) {
            tmp = array[j];
            array[j] = array[i];
            array[i] = tmp;
            j--;
            i++;
        }
    }

    /**
     * Extract the lone element of the specified Collection.
     *
     * @param <E>        collection type
     * @param collection to read
     * @return sole member of collection
     * @throws IllegalArgumentException if collection is null/empty or contains more than one element
     * @since 4.0
     */
    public static <E> E extractSingleton(final Collection<E> collection) {
        if (collection == null || collection.size() != 1) {
            throw new IllegalArgumentException("Can extract singleton only when collection size == 1");
        }
        return collection.iterator().next();
    }


    /**
     * Defines a functor interface implemented by classes that perform a predicate
     * test on an object.
     * <p/>
     * A <code>Predicate</code> is the object equivalent of an <code>if</code> statement.
     * It uses the input object to return a true or false value, and is often used in
     * validation or filtering.
     * <p/>
     * Standard implementations of common predicates are provided by
     * {@link PredicateUtils}. These include true, false, instanceof, equals, and,
     * or, not, method invokation and null testing.
     *
     * @param <T> the type that the predicate queries
     * @version $Id: Predicate.java 1543262 2013-11-19 00:47:45Z ggregory $
     * @since 1.0
     */
    public interface Predicate<T> {

        /**
         * Use the specified parameter to perform a test that returns true or false.
         *
         * @param object the object to evaluate, should not be changed
         * @return true or false
         * @throws ClassCastException       (runtime) if the input is the wrong class
         * @throws IllegalArgumentException (runtime) if the input is invalid
         * @throws FunctorException         (runtime) if the predicate encounters a problem
         */
        boolean evaluate(T object);

    }

    /**
     * Defines a functor interface implemented by classes that do something.
     * <p/>
     * A <code>Closure</code> represents a block of code which is executed from
     * inside some block, function or iteration. It operates an input object.
     * <p/>
     * Standard implementations of common closures are provided by
     * {@link ClosureUtils}. These include method invocation and for/while loops.
     *
     * @param <T> the type that the closure acts on
     * @version $Id: Closure.java 1543261 2013-11-19 00:47:34Z ggregory $
     * @since 1.0
     */
    public interface Closure<T> {

        /**
         * Performs an action on the specified input object.
         *
         * @param input the input to execute on
         * @throws ClassCastException       (runtime) if the input is the wrong class
         * @throws IllegalArgumentException (runtime) if the input is invalid
         * @throws FunctorException         (runtime) if any other error occurs
         */
        void execute(T input);

    }

}
