package com.carlosefonseca.common.utils;

import android.support.annotation.NonNull;

import java.util.*;

public class ObservableList<T> extends Observable implements List<T> {

    List<T> delegate;

    public ObservableList() {
        this.delegate = new ArrayList<>();
    }

    public ObservableList(List<T> delegate) {
        this.delegate = delegate;
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

    public void forceNotify() {
        setChanged();
        notifyObservers();
    }

    @Override
    public boolean add(T obj) {
        boolean result = delegate.add(obj);
        setChanged();
        notifyObservers();
        return result;
    }

    @Override
    public void add(int position, T obj) {
        delegate.add(position, obj);
        setChanged();
        notifyObservers();
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        boolean result = delegate.addAll(c);
        if (result) setChanged();
        notifyObservers();
        return result;
    }

    @Override
    public boolean addAll(int position, Collection<? extends T> c) {
        boolean result = delegate.addAll(position, c);
        if (result) {
            setChanged();
        }
        notifyObservers();
        return result;
    }

    @Override
    public void clear() {
        delegate.clear();
        setChanged();
        notifyObservers();
    }

    @Override
    public boolean contains(Object obj) {
        return delegate.contains(obj);
    }

    @Override
    public boolean containsAll(@NonNull Collection<?> c) {
        return delegate.containsAll(c);
    }

    @Override
    public T get(int position) {
        return delegate.get(position);
    }

    @Override
    public int indexOf(Object obj) {
        return delegate.indexOf(obj);
    }

    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Override
    public boolean equals(Object obj) {
        return delegate.equals(obj);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @NonNull
    @Override
    public Iterator<T> iterator() {
        return delegate.iterator();
    }

    @Override
    public int lastIndexOf(Object obj) {
        return delegate.lastIndexOf(obj);
    }

    @NonNull
    @Override
    public ListIterator<T> listIterator() {
        return delegate.listIterator();
    }

    @NonNull
    @Override
    public ListIterator<T> listIterator(int position) {
        return delegate.listIterator(position);
    }

    @Override
    public T remove(int position) {
        T result = delegate.remove(position);
        setChanged();
        notifyObservers();
        return result;
    }

    @Override
    public boolean remove(Object obj) {
        boolean result = delegate.remove(obj);
        if (result) {
            setChanged();
        }
        notifyObservers();
        return result;
    }

    @Override
    public boolean removeAll(@NonNull Collection<?> c) {
        boolean result = delegate.removeAll(c);
        if (result) {
            setChanged();
        }
        notifyObservers();
        return result;
    }

    @Override
    public boolean retainAll(@NonNull Collection<?> c) {
        boolean result = delegate.retainAll(c);
        if (result) {
            setChanged();
        }
        notifyObservers();
        return result;
    }

    @Override
    public T set(int position, T obj) {
        T result = delegate.set(position, obj);
        setChanged();
        notifyObservers();
        return result;
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @NonNull
    @Override
    public List<T> subList(int fromPosition, int toPosition) {
        return delegate.subList(fromPosition, toPosition);
    }

    @NonNull
    @Override
    public Object[] toArray() {
        return delegate.toArray();
    }

    @NonNull
    @Override
    public <T> T[] toArray(@NonNull T[] a) {
        //noinspection SuspiciousToArrayCall
        return delegate.toArray(a);
    }
}