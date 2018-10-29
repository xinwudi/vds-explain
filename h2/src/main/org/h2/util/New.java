/*
 * Copyright 2004-2018 H2 Group. Multiple-Licensed under the MPL 2.0,
 * and the EPL 1.0 (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package org.h2.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * This class contains static methods to construct commonly used generic objects
 * such as ArrayList.
 */
public class New {

    public static <T> ArrayList<T> arrayList(Collection<T> c) {
        return new ArrayList<>(c);
    }
    /**
     * Create a new ArrayList.
     *
     * @param <T> the type
     * @return the object
     */
    public static <T> ArrayList<T> arrayList() {
        return new ArrayList<>(4);
    }

    public static <K, V> HashMap<K, V> hashMap() {
        return new HashMap<>();
    }
}
