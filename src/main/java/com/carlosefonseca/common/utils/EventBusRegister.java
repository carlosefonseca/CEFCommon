package com.carlosefonseca.common.utils;

import de.greenrobot.event.EventBus;
import org.apache.commons.lang3.ArrayUtils;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Helps with the process of register and unregister for cases where one should not keep active registrations, like activities.
 * The subscriber is stored in a WeakReference to avoid cyclic retentions.
 * It also tracks whether or not it is already registered, so there's no problem in calling (un)register twice.
 */
public class EventBusRegister implements ActivityStateListener.Interface {
    private final Class<?>[] eventTypes;
    WeakReference<Object> subscriber;
    AtomicBoolean registered = new AtomicBoolean(false);

    /**
     * Creates the helper and registers.
     */
    public EventBusRegister(Object subscriber, Class<?>... eventTypes) {
        this.subscriber = new WeakReference<>(subscriber);
        this.eventTypes = eventTypes;
        register();
    }

    public void register() {
        if (registered.compareAndSet(false, true)) {
            if (eventTypes.length == 1) {
                EventBus.getDefault().register(this.subscriber.get(), eventTypes[0]);
            } else {
                EventBus.getDefault().register(this.subscriber.get(), eventTypes[0], ArrayUtils.subarray(eventTypes, 1, eventTypes.length));
            }
        }
    }

    public void unregister() {
        if (registered.compareAndSet(true, false)) {
            EventBus.getDefault().unregister(this.subscriber.get(), eventTypes);
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
