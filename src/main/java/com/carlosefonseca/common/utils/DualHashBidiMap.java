package com.carlosefonseca.common.utils;

import java.util.HashMap;
import java.util.Map;

public class DualHashBidiMap<K, V> extends HashMap<K, V> {

    transient HashMap<V, K> reverseMap;

    public DualHashBidiMap() {
        reverseMap = new HashMap<>();
    }

    public DualHashBidiMap(int capacity) {
        super(capacity);
        reverseMap = new HashMap<>(capacity);
    }

    public DualHashBidiMap(int capacity, float loadFactor) {
        super(capacity, loadFactor);
        reverseMap = new HashMap<>(capacity, loadFactor);
    }

    public DualHashBidiMap(Map<? extends K, ? extends V> map) {
        this(map.size());
        putAll(map);
    }

    @Override
    public V put(final K key, final V value) {
        if (containsKey(key)) {
            reverseMap.remove(get(key));
        }
        if (reverseMap.containsKey(value)) {
            remove(reverseMap.get(value));
        }
        final V obj = super.put(key, value);
        reverseMap.put(value, key);
        return obj;
    }

    @Override
    public V remove(final Object key) {
        V value = null;
        if (containsKey(key)) {
            value = super.remove(key);
            reverseMap.remove(value);
        }
        return value;
    }

    @Override
    public void clear() {
        super.clear();
        reverseMap.clear();
    }

    @Override
    public boolean containsValue(final Object value) {
        return reverseMap.containsKey(value);
    }

    public K getKey(final Object value) {
        return reverseMap.get(value);
    }

    public K removeValue(final Object value) {
        K key = null;
        if (reverseMap.containsKey(value)) {
            key = reverseMap.remove(value);
            super.remove(key);
        }
        return key;
    }
}
