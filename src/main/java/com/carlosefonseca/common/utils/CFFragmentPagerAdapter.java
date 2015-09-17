package com.carlosefonseca.common.utils;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

public abstract class CFFragmentPagerAdapter<T> extends FragmentPagerAdapter {
    private final List<T> mItems;

    public CFFragmentPagerAdapter(FragmentManager fm, List<T> items) {
        super(fm);
        mItems = items;
    }

    @Override
    public Fragment getItem(int position) {
        return getFragmentForItem(mItems.get(position));
    }

    public abstract Fragment getFragmentForItem(T item);

    @Override
    public int getCount() {
        return mItems.size();
    }
}
