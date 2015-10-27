package com.carlosefonseca.common.utils;

import de.greenrobot.event.EventBus;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Helps with the process of register and unregister for cases where one should not keep active registrations, like activities.
 * The subscriber is stored in a WeakReference to avoid cyclic retentions.
 * It also tracks whether or not it is already registered, so there's no problem in calling (un)register twice.
 */
public class EventBusRegister extends ActivityStateListener.SimpleInterface {
    WeakReference<Object> subscriber;
    AtomicBoolean registered = new AtomicBoolean(false);

    /**
     * Creates the helper and registers.
     */
    public EventBusRegister(Object subscriber) {
        this.subscriber = new WeakReference<>(subscriber);
        register();
    }

    public void register() {
        if (registered.compareAndSet(false, true)) {
            EventBus.getDefault().register(this.subscriber.get());
        }
    }

    public void unregister() {
        if (registered.compareAndSet(true, false)) {
            EventBus.getDefault().unregister(this.subscriber.get());
        }
    }

    @Override
    public void onStart() {
        register();
    }

    @Override
    public void onStop(boolean isFinishing) {
        unregister();
    }
}
