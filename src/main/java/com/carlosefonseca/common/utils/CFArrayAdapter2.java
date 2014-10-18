package com.carlosefonseca.common.utils;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import org.apache.commons.lang3.NotImplementedException;

import java.util.List;

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
     *
     * @param parent
     */
    protected V instantiateView(ViewGroup parent) {
        throw new NotImplementedException("Implement instantiateView");
    }

    /**
     * Override the setup your view with the item
     *
     * @param view
     * @param item
     */
    protected void setupView(V view, T item, int position) {
        setupView(view, item);
    }

    /**
     * Override the setup your view with the item
     *
     * @param view
     * @param item
     */
    protected void setupView(V view, T item) {
    }

}
