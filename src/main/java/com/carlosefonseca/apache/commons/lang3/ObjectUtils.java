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
package com.carlosefonseca.apache.commons.lang3;


import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.Serializable;

/**
 * <p>Operations on {@code Object}.</p>
 *
 * <p>This class tries to handle {@code null} input gracefully.
 * An exception will generally not be thrown for a {@code null} input.
 * Each method documents its behaviour in more detail.</p>
 *
 * <p>#ThreadSafe#</p>
 * @since 1.0
 * @version $Id: ObjectUtils.java 1583781 2014-04-01 20:54:53Z niallp $
 */
//@Immutable
@SuppressWarnings({"SimplifiableIfStatement",
                   "PointlessBooleanExpression",
                   "UnusedDeclaration",
                   "ObjectEquality",
                   "deprecation"})
public class ObjectUtils {

    /**
     * <p>Singleton used as a {@code null} placeholder where
     * {@code null} has another meaning.</p>
     *
     * <p>For example, in a {@code HashMap} the
     * {@link java.util.HashMap#get(java.lang.Object)} method returns
     * {@code null} if the {@code Map} contains {@code null} or if there
     * is no matching key. The {@code Null} placeholder can be used to
     * distinguish between these two cases.</p>
     *
     * <p>Another example is {@code Hashtable}, where {@code null}
     * cannot be stored.</p>
     *
     * <p>This instance is Serializable.</p>
     */
    public static final Null NULL = new Null();

    /**
     * <p>{@code ObjectUtils} instances should NOT be constructed in
     * standard programming. Instead, the static methods on the class should
     * be used, such as {@code ObjectUtils.defaultIfNull("a","b");}.</p>
     *
     * <p>This constructor is public to permit tools that require a JavaBean
     * instance to operate.</p>
     */
    public ObjectUtils() {
        super();
    }

    // Defaulting
    //-----------------------------------------------------------------------
    /**
     * <p>Returns a default value if the object passed is {@code null}.</p>
     *
     * <pre>
     * ObjectUtils.defaultIfNull(null, null)      = null
     * ObjectUtils.defaultIfNull(null, "")        = ""
     * ObjectUtils.defaultIfNull(null, "zz")      = "zz"
     * ObjectUtils.defaultIfNull("abc", *)        = "abc"
     * ObjectUtils.defaultIfNull(Boolean.TRUE, *) = Boolean.TRUE
     * </pre>
     *
     * @param <T> the type of the object
     * @param object  the {@code Object} to test, may be {@code null}
     * @param defaultValue  the default value to return, may be {@code null}
     * @return {@code object} if it is not {@code null}, defaultValue otherwise
     */
    public static <T> T defaultIfNull(final T object, final T defaultValue) {
        return object != null ? object : defaultValue;
    }

    /**
     * <p>Returns the first value in the array which is not {@code null}.
     * If all the values are {@code null} or the array is {@code null}
     * or empty then {@code null} is returned.</p>
     *
     * <pre>
     * ObjectUtils.firstNonNull(null, null)      = null
     * ObjectUtils.firstNonNull(null, "")        = ""
     * ObjectUtils.firstNonNull(null, null, "")  = ""
     * ObjectUtils.firstNonNull(null, "zz")      = "zz"
     * ObjectUtils.firstNonNull("abc", *)        = "abc"
     * ObjectUtils.firstNonNull(null, "xyz", *)  = "xyz"
     * ObjectUtils.firstNonNull(Boolean.TRUE, *) = Boolean.TRUE
     * ObjectUtils.firstNonNull()                = null
     * </pre>
     *
     * @param <T> the component type of the array
     * @param values  the values to test, may be {@code null} or empty
     * @return the first value from {@code values} which is not {@code null},
     *  or {@code null} if there are no non-null values
     * @since 3.0
     */
    @SafeVarargs
    @Nullable
    public static <T> T firstNonNull(final T... values) {
        if (values != null) {
            for (final T val : values) {
                if (val != null) {
                    return val;
                }
            }
        }
        return null;
    }

    // Null-safe equals/hashCode
    //-----------------------------------------------------------------------
    /**
     * <p>Compares two objects for equality, where either one or both
     * objects may be {@code null}.</p>
     *
     * <pre>
     * ObjectUtils.equals(null, null)                  = true
     * ObjectUtils.equals(null, "")                    = false
     * ObjectUtils.equals("", null)                    = false
     * ObjectUtils.equals("", "")                      = true
     * ObjectUtils.equals(Boolean.TRUE, null)          = false
     * ObjectUtils.equals(Boolean.TRUE, "true")        = false
     * ObjectUtils.equals(Boolean.TRUE, Boolean.TRUE)  = true
     * ObjectUtils.equals(Boolean.TRUE, Boolean.FALSE) = false
     * </pre>
     *
     * @param object1  the first object, may be {@code null}
     * @param object2  the second object, may be {@code null}
     * @return {@code true} if the values of both objects are the same
     * @deprecated this method has been replaced by {@code java.util.Objects.equals(Object, Object)} in Java 7 and will
     * be removed from future releases.
     */
    @Deprecated
    public static boolean equals(final Object object1, final Object object2) {
        if (object1 == object2) {
            return true;
        }
        if (object1 == null || object2 == null) {
            return false;
        }
        return object1.equals(object2);
    }

    /**
     * <p>Compares two objects for inequality, where either one or both
     * objects may be {@code null}.</p>
     *
     * <pre>
     * ObjectUtils.notEqual(null, null)                  = false
     * ObjectUtils.notEqual(null, "")                    = true
     * ObjectUtils.notEqual("", null)                    = true
     * ObjectUtils.notEqual("", "")                      = false
     * ObjectUtils.notEqual(Boolean.TRUE, null)          = true
     * ObjectUtils.notEqual(Boolean.TRUE, "true")        = true
     * ObjectUtils.notEqual(Boolean.TRUE, Boolean.TRUE)  = false
     * ObjectUtils.notEqual(Boolean.TRUE, Boolean.FALSE) = true
     * </pre>
     *
     * @param object1  the first object, may be {@code null}
     * @param object2  the second object, may be {@code null}
     * @return {@code false} if the values of both objects are the same
     */
    public static boolean notEqual(final Object object1, final Object object2) {
        return ObjectUtils.equals(object1, object2) == false;
    }

    /**
     * <p>Gets the hash code of an object returning zero when the
     * object is {@code null}.</p>
     *
     * <pre>
     * ObjectUtils.hashCode(null)   = 0
     * ObjectUtils.hashCode(obj)    = obj.hashCode()
     * </pre>
     *
     * @param obj  the object to obtain the hash code of, may be {@code null}
     * @return the hash code of the object, or zero if null
     * @since 2.1
     * @deprecated this method has been replaced by {@code java.util.Objects.hashCode(Object)} in Java 7 and will be
     * removed in future releases
     */
    @Deprecated
    public static int hashCode(final Object obj) {
        // hashCode(Object) retained for performance, as hash code is often critical
        return obj == null ? 0 : obj.hashCode();
    }

    @Override
    public String toString() {
        return super.toString();
    }

    /**
     * <p>Gets the hash code for multiple objects.</p>
     * 
     * <p>This allows a hash code to be rapidly calculated for a number of objects.
     * The hash code for a single object is the <em>not</em> same as {@link #hashCode(Object)}.
     * The hash code for multiple objects is the same as that calculated by an
     * {@code ArrayList} containing the specified objects.</p>
     *
     * <pre>
     * ObjectUtils.hashCodeMulti()                 = 1
     * ObjectUtils.hashCodeMulti((Object[]) null)  = 1
     * ObjectUtils.hashCodeMulti(a)                = 31 + a.hashCode()
     * ObjectUtils.hashCodeMulti(a,b)              = (31 + a.hashCode()) * 31 + b.hashCode()
     * ObjectUtils.hashCodeMulti(a,b,c)            = ((31 + a.hashCode()) * 31 + b.hashCode()) * 31 + c.hashCode()
     * </pre>
     *
     * @param objects  the objects to obtain the hash code of, may be {@code null}
     * @return the hash code of the objects, or zero if null
     * @since 3.0
     * @deprecated this method has been replaced by {@code java.util.Objects.hash(Object...)} in Java 7 an will be
     * removed in future releases.
     */
    @Deprecated
    public static int hashCodeMulti(final Object... objects) {
        int hash = 1;
        if (objects != null) {
            for (final Object object : objects) {
                int tmpHash = ObjectUtils.hashCode(object);
                hash = hash * 31 + tmpHash;
            }
        }
        return hash;
    }

    // Identity ToString
    //-----------------------------------------------------------------------
    /**
     * <p>Gets the toString that would be produced by {@code Object}
     * if a class did not override toString itself. {@code null}
     * will return {@code null}.</p>
     *
     * <pre>
     * ObjectUtils.identityToString(null)         = null
     * ObjectUtils.identityToString("")           = "java.lang.String@1e23"
     * ObjectUtils.identityToString(Boolean.TRUE) = "java.lang.Boolean@7fa"
     * </pre>
     *
     * @param object  the object to create a toString for, may be
     *  {@code null}
     * @return the default toString text, or {@code null} if
     *  {@code null} passed in
     */
    @Nullable
    public static String identityToString(final Object object) {
        if (object == null) {
            return null;
        }
        final StringBuilder builder = new StringBuilder();
        identityToString(builder, object);
        return builder.toString();
    }

    /**
     * <p>Appends the toString that would be produced by {@code Object}
     * if a class did not override toString itself. {@code null}
     * will throw a NullPointerException for either of the two parameters. </p>
     *
     * <pre>
     * ObjectUtils.identityToString(appendable, "")            = appendable.append("java.lang.String@1e23"
     * ObjectUtils.identityToString(appendable, Boolean.TRUE)  = appendable.append("java.lang.Boolean@7fa"
     * ObjectUtils.identityToString(appendable, Boolean.TRUE)  = appendable.append("java.lang.Boolean@7fa")
     * </pre>
     *
     * @param appendable  the appendable to append to
     * @param object  the object to create a toString for
     * @throws IOException if an I/O error occurs
     * @since 3.2
     */
    public static void identityToString(final Appendable appendable, final Object object) throws IOException {
        if (object == null) {
            throw new NullPointerException("Cannot get the toString of a null identity");
        }
        appendable.append(object.getClass().getName())
              .append('@')
              .append(Integer.toHexString(System.identityHashCode(object)));
    }



    /**
     * <p>Appends the toString that would be produced by {@code Object}
     * if a class did not override toString itself. {@code null}
     * will throw a NullPointerException for either of the two parameters. </p>
     *
     * <pre>
     * ObjectUtils.identityToString(buf, "")            = buf.append("java.lang.String@1e23"
     * ObjectUtils.identityToString(buf, Boolean.TRUE)  = buf.append("java.lang.Boolean@7fa"
     * ObjectUtils.identityToString(buf, Boolean.TRUE)  = buf.append("java.lang.Boolean@7fa")
     * </pre>
     *
     * @param buffer  the buffer to append to
     * @param object  the object to create a toString for
     * @since 2.4
     */
    public static void identityToString(final StringBuffer buffer, final Object object) {
        if (object == null) {
            throw new NullPointerException("Cannot get the toString of a null identity");
        }
        buffer.append(object.getClass().getName())
              .append('@')
              .append(Integer.toHexString(System.identityHashCode(object)));
    }

    /**
     * <p>Appends the toString that would be produced by {@code Object}
     * if a class did not override toString itself. {@code null}
     * will throw a NullPointerException for either of the two parameters. </p>
     *
     * <pre>
     * ObjectUtils.identityToString(builder, "")            = builder.append("java.lang.String@1e23"
     * ObjectUtils.identityToString(builder, Boolean.TRUE)  = builder.append("java.lang.Boolean@7fa"
     * ObjectUtils.identityToString(builder, Boolean.TRUE)  = builder.append("java.lang.Boolean@7fa")
     * </pre>
     *
     * @param builder  the builder to append to
     * @param object  the object to create a toString for
     * @since 3.2
     */
    public static void identityToString(final StringBuilder builder, final Object object) {
        if (object == null) {
            throw new NullPointerException("Cannot get the toString of a null identity");
        }
        builder.append(object.getClass().getName())
              .append('@')
              .append(Integer.toHexString(System.identityHashCode(object)));
    }

    // ToString
    //-----------------------------------------------------------------------
    /**
     * <p>Gets the {@code toString} of an {@code Object} returning
     * an empty string ("") if {@code null} input.</p>
     *
     * <pre>
     * ObjectUtils.toString(null)         = ""
     * ObjectUtils.toString("")           = ""
     * ObjectUtils.toString("bat")        = "bat"
     * ObjectUtils.toString(Boolean.TRUE) = "true"
     * </pre>
     *
     * @see StringUtils#defaultString(String)
     * @see String#valueOf(Object)
     * @param obj  the Object to {@code toString}, may be null
     * @return the passed in Object's toString, or {@code ""} if {@code null} input
     * @since 2.0
     * @deprecated this method has been replaced by {@code java.util.Objects.toString(Object)} in Java 7 and will be
     * removed in future releases. Note however that said method will return "null" for null references, while this
     * method returns and empty String. To preserve behavior use {@code java.util.Objects.toString(myObject, "")}
     */
    @Deprecated
    public static String toString(final Object obj) {
        return obj == null ? "" : obj.toString();
    }

    /**
     * <p>Gets the {@code toString} of an {@code Object} returning
     * a specified text if {@code null} input.</p>
     *
     * <pre>
     * ObjectUtils.toString(null, null)           = null
     * ObjectUtils.toString(null, "null")         = "null"
     * ObjectUtils.toString("", "null")           = ""
     * ObjectUtils.toString("bat", "null")        = "bat"
     * ObjectUtils.toString(Boolean.TRUE, "null") = "true"
     * </pre>
     *
     * @see StringUtils#defaultString(String,String)
     * @see String#valueOf(Object)
     * @param obj  the Object to {@code toString}, may be null
     * @param nullStr  the String to return if {@code null} input, may be null
     * @return the passed in Object's toString, or {@code nullStr} if {@code null} input
     * @since 2.0
     * @deprecated this method has been replaced by {@code java.util.Objects.toString(Object, String)} in Java 7 and
     * will be removed in future releases.
     */
    @Deprecated
    public static String toString(final Object obj, final String nullStr) {
        return obj == null ? nullStr : obj.toString();
    }

    // Comparable
    //-----------------------------------------------------------------------
    /**
     * <p>Null safe comparison of Comparables.</p>
     *
     * @param <T> type of the values processed by this method
     * @param values the set of comparable values, may be null
     * @return
     *  <ul>
     *   <li>If any objects are non-null and unequal, the lesser object.
     *   <li>If all objects are non-null and equal, the first.
     *   <li>If any of the comparables are null, the lesser of the non-null objects.
     *   <li>If all the comparables are null, null is returned.
     *  </ul>
     */
    @SafeVarargs
    @Nullable
    public static <T extends Comparable<? super T>> T min(final T... values) {
        T result = null;
        if (values != null) {
            for (final T value : values) {
                if (compare(value, result, true) < 0) {
                    result = value;
                }
            }
        }
        return result;
    }

    /**
     * <p>Null safe comparison of Comparables.</p>
     *
     * @param <T> type of the values processed by this method
     * @param values the set of comparable values, may be null
     * @return
     *  <ul>
     *   <li>If any objects are non-null and unequal, the greater object.
     *   <li>If all objects are non-null and equal, the first.
     *   <li>If any of the comparables are null, the greater of the non-null objects.
     *   <li>If all the comparables are null, null is returned.
     *  </ul>
     */
    @SafeVarargs
    @Nullable
    public static <T extends Comparable<? super T>> T max(final T... values) {
        T result = null;
        if (values != null) {
            for (final T value : values) {
                if (compare(value, result, false) > 0) {
                    result = value;
                }
            }
        }
        return result;
    }

    /**
     * <p>Null safe comparison of Comparables.
     * {@code null} is assumed to be less than a non-{@code null} value.</p>
     *
     * @param <T> type of the values processed by this method
     * @param c1  the first comparable, may be null
     * @param c2  the second comparable, may be null
     * @return a negative value if c1 &lt; c2, zero if c1 = c2
     *  and a positive value if c1 &gt; c2
     */
    public static <T extends Comparable<? super T>> int compare(@Nullable final T c1, @Nullable final T c2) {
        return compare(c1, c2, false);
    }

    /**
     * <p>Null safe comparison of Comparables.</p>
     *
     * @param <T> type of the values processed by this method
     * @param c1  the first comparable, may be null
     * @param c2  the second comparable, may be null
     * @param nullGreater if true {@code null} is considered greater
     *  than a non-{@code null} value or if false {@code null} is
     *  considered less than a Non-{@code null} value
     * @return a negative value if c1 &lt; c2, zero if c1 = c2
     *  and a positive value if c1 &gt; c2
     * @see java.util.Comparator#compare(Object, Object)
     */
    public static <T extends Comparable<? super T>> int compare(@Nullable final T c1, @Nullable final T c2, final boolean nullGreater) {
        if (c1 == c2) {
            return 0;
        } else if (c1 == null) {
            return nullGreater ? 1 : -1;
        } else if (c2 == null) {
            return nullGreater ? -1 : 1;
        }
        return c1.compareTo(c2);
    }






    // Null
    //-----------------------------------------------------------------------
    /**
     * <p>Class used as a null placeholder where {@code null}
     * has another meaning.</p>
     *
     * <p>For example, in a {@code HashMap} the
     * {@link java.util.HashMap#get(java.lang.Object)} method returns
     * {@code null} if the {@code Map} contains {@code null} or if there is
     * no matching key. The {@code Null} placeholder can be used to distinguish
     * between these two cases.</p>
     *
     * <p>Another example is {@code Hashtable}, where {@code null}
     * cannot be stored.</p>
     */
    public static class Null implements Serializable {
        /**
         * Required for serialization support. Declare serialization compatibility with Commons Lang 1.0
         *
         * @see java.io.Serializable
         */
        private static final long serialVersionUID = 7092611880189329093L;

        /**
         * Restricted constructor - singleton.
         */
        Null() {
            super();
        }

        /**
         * <p>Ensure singleton.</p>
         *
         * @return the singleton value
         */
        private Object readResolve() {
            return ObjectUtils.NULL;
        }
    }




}
