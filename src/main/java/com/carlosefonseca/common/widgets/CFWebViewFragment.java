package com.carlosefonseca.common.widgets;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import com.carlosefonseca.common.utils.CodeUtils;
import org.jetbrains.annotations.Nullable;

/**
 * A fragment that displays a WebView.
 * <p/>
 * The WebView is automatically paused or resumed when the Fragment is paused or resumed.
 * <p/>
 * The WebView is configured to have JS, HTML5 Stuff and all that goodness.
 */
public class CFWebViewFragment extends Fragment {
    protected static final String URL_KEY = "URL";

    private WebView mWebView;
    private boolean mIsWebViewAvailable;

    public CFWebViewFragment() {
    }

    /**
     * Called to instantiate the view. Creates and returns the WebView.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        if (mWebView != null) {
//            mWebView.destroy();
//        }
        if (mWebView != null) return mWebView;

        mWebView = new WebView(getActivity());
        CodeUtils.setupWebView(mWebView);
        String string = getArguments().getString(URL_KEY);
        if (string != null) mWebView.loadUrl(string);
        mIsWebViewAvailable = true;
        return mWebView;
    }

    /**
     * Called when the fragment is visible to the user and actively running. Resumes the WebView.
     */
    @Override
    public void onPause() {
        super.onPause();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) mWebView.onPause();
    }

    /**
     * Called when the fragment is no longer resumed. Pauses the WebView.
     */
    @Override
    public void onResume() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) mWebView.onResume();
        super.onResume();
    }

    /**
     * Called when the WebView has been detached from the fragment.
     * The WebView is no longer available after this time.
     */
    @Override
    public void onDestroyView() {
        mIsWebViewAvailable = false;
        super.onDestroyView();
    }

    /**
     * Called when the fragment is no longer in use. Destroys the internal state of the WebView.
     */
    @Override
    public void onDestroy() {
        if (mWebView != null) {
            mWebView.destroy();
            mWebView = null;
        }
        super.onDestroy();
    }

    /**
     * Gets the WebView.
     */
    public WebView getWebView() {
        return mIsWebViewAvailable ? mWebView : null;
    }

    public static Bundle getBundleForUrl(@Nullable String url) {
        Bundle bundle = new Bundle(1);
        bundle.putString(URL_KEY, url);
        return bundle;
    }
}
