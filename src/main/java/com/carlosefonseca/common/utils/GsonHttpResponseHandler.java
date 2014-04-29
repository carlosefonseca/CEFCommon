package com.carlosefonseca.common.utils;

import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.TextHttpResponseHandler;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpStatus;

import static com.carlosefonseca.common.utils.CodeUtils.isMainThread;

/**
 * Used to intercept and handle the responses from requests made using {@link com.loopj.android.http.AsyncHttpClient}, with
 * automatic parsing into a {@link org.json.JSONObject} or {@link org.json.JSONArray}. <p>&nbsp;</p> This class is
 * designed to be passed to get, post, put and delete requests with the {@link
 * #onSuccess(Object)} method anonymously overridden.
 * <p>&nbsp;</p> Additionally, you can override the other event methods from the parent class.
 */
public abstract class GsonHttpResponseHandler<T> extends TextHttpResponseHandler {
    private static final String TAG = CodeUtils.getTag(GsonHttpResponseHandler.class);
    protected final Class<T> clazz;
    protected final Gson gson;

    /**
     * Creates a new JsonHttpResponseHandler
     */

    public GsonHttpResponseHandler(Class<T> clazz) {
        this(clazz, null);
    }

    /**
     * Creates a new JsonHttpResponseHandler
     */

    public GsonHttpResponseHandler(Class<T> clazz, Gson gson) {
        super(AsyncHttpResponseHandler.DEFAULT_CHARSET);
        this.clazz = clazz;
        this.gson = gson != null ? gson : new Gson();
    }


    //
    // Callbacks to be overridden, typically anonymously
    //

    /**
     * Fired when a request returns successfully and contains a json object at the base of the
     * response string. Override to handle in your own code.
     *
     * @param response the parsed json object found in the server response (if any)
     */
    public void onSuccess(T response) {
        throw new RuntimeException("uh?");
    }


    /**
     * Fired when a request returns successfully and contains a json object at the base of the
     * response string. Override to handle in your own code.
     *
     * @param statusCode the status code of the response
     * @param headers    the headers of the HTTP response
     * @param response   the parsed json object found in the server response (if any)
     */
    public void onSuccess(int statusCode, Header[] headers, T response) {
        onSuccess(response);
    }

    @Override
    public void onSuccess(final int statusCode, final Header[] headers, final String responseBody) {
        if (statusCode != HttpStatus.SC_NO_CONTENT) {
            if (StringUtils.isBlank(responseBody)) {
                onFailure(statusCode, headers, responseBody, new RuntimeException("JSON is empty."));
                return;
            }
            final Runnable runnable = new Runnable() {
                @Override
                public void run() {
//                    Log.i(TAG, clazz.getSimpleName() + " RUNNING ON MAIN THREAD? " + (getMainLooper() == myLooper()));
                    try {
                        onSuccess(statusCode, headers, gson.fromJson(responseBody, clazz));
                    } catch (Throwable e) {
                        onFailure(statusCode, headers, responseBody, e);
                    }
                }
            };
//            Log.i(TAG, clazz.getSimpleName() + " INVOKED FROM MAIN THREAD? " + (Looper.getMainLooper() == Looper.myLooper()));
            if (getUseSynchronousMode() || !isMainThread()) {
                runnable.run();
            } else {
                CodeUtils.runOnBackground(runnable);
            }

        } else {
            onSuccess(statusCode, headers, (T) null);
        }
    }

    @Override
    public void onProgress(int bytesWritten, int totalSize) { }
}