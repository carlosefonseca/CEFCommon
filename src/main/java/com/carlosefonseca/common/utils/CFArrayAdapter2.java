package com.carlosefonseca.common.utils;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * ArrayAdapter where you override {@link #instantiateView(ViewGroup)} and {@link #setupView(View, Object)} and you
 * can use your own class for the views.
 *
 * @param <T> The model object
 * @param <V> The class for the view
 */
public class CFArrayAdapter2<T, V extends View> extends CFArrayAdapter<T> {

    public CFArrayAdapter2(Context context, List<T> objects) {
        super(context, objects);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //noinspection unchecked
        V view = (V) convertView;
        if (view == null) view = instantiateView(parent);

        setupView(view, getItem(position), position);

        return view;
    }

    /**
     * Override to implement the creation of your view.
     */
    protected V instantiateView(ViewGroup parent) {
        throw new UnsupportedOperationException("Implement instantiateView");
    }

    /**
     * Override the setup your view with the item
     */
    protected void setupView(V view, T item, int position) {
        setupView(view, item);
    }

    /**
     * Override the setup your view with the item
     */
    protected void setupView(V view, T item) {
    }


    /**
     * Don't use these if you're using CFArrayAdapter2. Use {@link #setupView(View, Object)}.
     */
    @Override
    protected final void setOnView(View view, T item) {
        super.setOnView(view, item);
    }

    /**
     * Don't use these if you're using CFArrayAdapter2. Use {@link #setupView(View, Object)}.
     */
    @Override
    protected final void setOnDropdownView(View view, T item) {
        super.setOnDropdownView(view, item);
    }
}
