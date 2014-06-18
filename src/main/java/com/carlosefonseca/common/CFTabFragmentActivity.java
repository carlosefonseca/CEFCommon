package com.carlosefonseca.common;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.TabHost;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

/**
 * Based on http://thepseudocoder.wordpress.com/2011/10/04/android-tabs-the-fragment-way/
 */

public class CFTabFragmentActivity extends CFActivity implements TabHost.OnTabChangeListener {

    private int realtabcontent;
    protected TabHost mTabHost;
    private HashMap<String, TabInfo> mapTabInfo = new HashMap<String, TabInfo>();
    @Nullable private TabInfo mLastTab = null;

    public static class TabInfo {
        private String tag;
        private Class clss;
        private Bundle args;
        public Fragment fragment;

        public TabInfo(String tag, Class clazz, Bundle args) {
            this.tag = tag;
            this.clss = clazz;
            this.args = args;
        }

        public String getTag() {
            return tag;
        }
    }

    static class TabFactory implements TabHost.TabContentFactory {

        private final Context mContext;

        public TabFactory(Context context) {
            mContext = context;
        }

        /**
         * @see android.widget.TabHost.TabContentFactory#createTabContent(java.lang.String)
         */
        @Override
        public View createTabContent(String tag) {
            View v = new View(mContext);
            v.setMinimumWidth(0);
            v.setMinimumHeight(0);
            return v;
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("tab", mTabHost.getCurrentTabTag()); //save the tab selected
        super.onSaveInstanceState(outState);
    }

    protected void initialiseTabHost(int realTabContent) {
        this.realtabcontent = realTabContent;
        mTabHost = (TabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup();
        mTabHost.setOnTabChangedListener(this);
    }

    protected void addTab(CFTabFragmentActivity activity, TabHost tabHost, TabHost.TabSpec tabSpec, TabInfo tabInfo) {
        // Attach a Tab view factory to the spec
        tabSpec.setContent(new TabFactory(activity));
        String tag = tabSpec.getTag();

        // Check to see if we already have a fragment for this tab, probably
        // from a previously saved state.  If so, deactivate it, because our
        // initial state is that a tab isn't shown.
        tabInfo.fragment = activity.getSupportFragmentManager().findFragmentByTag(tag);
        if (tabInfo.fragment != null && !tabInfo.fragment.isDetached()) {
            FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
            ft.detach(tabInfo.fragment);
            ft.commit();
            activity.getSupportFragmentManager().executePendingTransactions();
        }

        tabHost.addTab(tabSpec);
        mapTabInfo.put(tabInfo.tag, tabInfo);
    }

    @Override
    public void onTabChanged(String tag) {
        TabInfo newTab = this.mapTabInfo.get(tag);
        if (mLastTab != newTab) {
            FragmentTransaction ft = this.getSupportFragmentManager().beginTransaction();
            if (mLastTab != null) {
                if (mLastTab.fragment != null) {
//                    ft.detach(mLastTab.fragment);
                    ft.hide(mLastTab.fragment);
                }
            }
            if (newTab != null) {
                if (newTab.fragment == null) {
                    newTab.fragment = Fragment.instantiate(this, newTab.clss.getName(), newTab.args);
                    ft.add(realtabcontent, newTab.fragment, newTab.tag);
                } else {
                    ft.show(newTab.fragment);
//                    ft.attach(newTab.fragment);
                }
            }

            onChangingTab(mLastTab, newTab);
            ft.commit();
            this.getSupportFragmentManager().executePendingTransactions();
            if (mLastTab != null) mLastTab.fragment.setUserVisibleHint(false);
            mLastTab = newTab;
            if (newTab != null && newTab.fragment.getView() != null) newTab.fragment.setUserVisibleHint(true);
        }
    }

    /**
     * Override to be notified when the user changes tabs.
     *
     * @param oldTab The tab that will be hidden.
     * @param newTab The tab that will be displayed.
     */
    protected void onChangingTab(@Nullable TabInfo oldTab, TabInfo newTab) {}

    protected TabInfo getTabByTag(String tag) {
        return this.mapTabInfo.get(tag);
    }

    protected TabInfo getCurrentTabInfo() {
        return mLastTab;
    }
}
