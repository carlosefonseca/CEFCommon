package com.carlosefonseca.common.utils;

import java.util.HashMap;
import java.util.List;

import static com.carlosefonseca.common.utils.ListUtils.list;

public class HashMapOfList<T, Q> extends HashMap<T, List<Q>> {

    public void putAppend(T key, Q value) {
        if (this.containsKey(key)) {
            this.get(key).add(value);
        } else {
            this.put(key, list(value));
        }
    }
}
