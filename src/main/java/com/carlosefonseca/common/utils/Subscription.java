package com.carlosefonseca.common.utils;

import java.util.ArrayList;

public class Subscription<I, M> {
    public Subscription() {
    }

    public Subscription(SubscriberDelegate<I, M> delegate) {
        this.delegate = delegate;
    }

    public interface SubscriberDelegate<I, M> {
        public void start();

        public void stop();

        public void send(I subscriber, M message);
    }

    ArrayList<I> listeners = new ArrayList<>();
    SubscriberDelegate<I, M> delegate;

    public synchronized void register(I listener) {
        listeners.add(listener);
        if (listeners.size() == 1) {
            start();
        }
    }

    public synchronized void unregister(I listener) {
        listeners.remove(listener);
        if (listeners.isEmpty()) {
            stop();
        }
    }

    private void start() {
        delegate.start();
    }

    private void stop() {
        delegate.stop();
    }

    public SubscriberDelegate<I, M> getDelegate() {
        return delegate;
    }

    public void setDelegate(SubscriberDelegate<I, M> delegate) {
        this.delegate = delegate;
    }

    public synchronized ArrayList<I> getListeners() {
        return listeners;
    }

    public void send(M message) {
        for (I listener : listeners) {
            delegate.send(listener, message);
        }
    }
}
