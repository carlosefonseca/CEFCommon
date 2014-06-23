package com.carlosefonseca.common;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import com.carlosefonseca.common.utils.*;
import com.google.gson.*;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;
import junit.framework.Assert;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import static com.carlosefonseca.common.utils.CodeUtils.getTag;
import static com.carlosefonseca.common.utils.CodeUtils.isMainThread;

public class CFRestClient {
    protected static final String EMPTY_MESSAGE = "EMPTY";
    private static final String TAG = CodeUtils.getTag(CFRestClient.class);

    public static boolean offline = false;

    /**
     * Convenience method to invoke the HttpClient's GET by passing only the elements that usually change.
     *
     * @param url           The URL to be requested, either an absolute URL or a sufix for
     *                      {@link com.carlosefonseca.common.CFRestClient.CFAPIClient#getBaseUrl()}.
     * @param requestParams The request parameters specific to this request.
     * @param file          The file to use in case it's an offline request.
     * @param clazz         The class representing the JSON data to deserialize.
     * @param handler       The handler that will receive the deserialized object or the errors that might occur.
     */
    protected static <T> void get(String url, @Nullable RequestParams requestParams, @Nullable String file, Class<T> clazz, DataHandler<T> handler) {
        get(url, requestParams, file, clazz, handler, CFRestClient.offline);
    }

    /**
     * Convenience method to invoke the HttpClient's GET by passing only the elements that usually change.
     *
     * @param url           The URL to be requested, either an absolute URL or a sufix for
     *                      {@link com.carlosefonseca.common.CFRestClient.CFAPIClient#getBaseUrl()}.
     * @param requestParams The request parameters specific to this request.
     * @param clazz         The class representing the JSON data to deserialize.
     * @param handler       The handler that will receive the deserialized object or the errors that might occur.
     * @param offline       Set to true to force the use of the offline file. Set to false to obey global offline flag.
     *                      If true, everything will be synchronous so you can count on something being returned ASAP.
     */
    public static <T> void get(String url,
                               @Nullable RequestParams requestParams,
                               @Nullable String file,
                               Class<T> clazz,
                               DataHandler<T> handler,
                               boolean offline) {

        final ResponseHandler<T> responseHandler = new ResponseHandler<>(clazz, handler);

        if (offline) {
            // if caller forced the offline, we better do it synchronously since he probably needs the data right away.
            responseHandler.setUseSynchronousMode(true);
            CFAPIClient.getOffline(file, responseHandler, false);

        } else if (CFRestClient.offline) {
            CFAPIClient.getOffline(file, responseHandler);

        } else {

            url = CFAPIClient.getAbsoluteUrl(url);

            // ONLINE
            final String fullUrl = "GET: " + url + (requestParams == null ? "" : "?" + requestParams.toString());
            if (!NetworkingUtils.hasInternet()) {
                Log.w(TAG, "NO INTERNET! " + fullUrl);
                responseHandler.onFailure(0, null, (String)null, new NetworkingUtils.NotConnectedException());
            } else {
                Log.d(responseHandler.TAG, fullUrl);
                CFAPIClient.client.get(url, requestParams, responseHandler);
            }
        }
    }

    /**
     * Convenience method to invoke the HttpClient's GET by passing only the elements that usually change.
     *
     * @param url           The URL to be requested, either an absolute URL or a sufix for
     *                      {@link com.carlosefonseca.common.CFRestClient.CFAPIClient#getBaseUrl()}.
     * @param requestParams The request parameters specific to this request.
     * @param clazz         The class representing the JSON data to deserialize.
     * @param handler       The handler that will receive the deserialized object or the errors that might occur.
     * @param offline       Set to true to force the use of the offline file. Set to false to obey global offline flag.
     *                      If true, everything will be synchronous so you can count on something being returned ASAP.
     */
    public static <T> void post(@NotNull String url,
                                @Nullable RequestParams requestParams,
                                @Nullable String body,
                                @Nullable String file,
                                @NotNull Class<T> clazz,
                                @Nullable DataHandler<T> handler,
                                boolean offline) {

        final ResponseHandler<T> responseHandler = new ResponseHandler<>(clazz, handler);

        if (offline) {
            // if caller forced the offline, we better do it synchronously since he probably needs the data right away.
            responseHandler.setUseSynchronousMode(true);
            CFAPIClient.getOffline(file, responseHandler, false);

        } else if (CFRestClient.offline) {
            CFAPIClient.getOffline(file, responseHandler);

        } else {

            url = CFAPIClient.getAbsoluteUrl(url);

            // ONLINE
            String longUrl = url + (requestParams == null ? "" : ("?" + requestParams.toString()));
            if (!NetworkingUtils.hasInternet()) {
                Log.w(TAG, "NO INTERNET! GET: " + longUrl);
                responseHandler.onFailure(0, null, (String) null, new NetworkingUtils.NotConnectedException());
            } else {
                Log.v(responseHandler.TAG, "GET: " + longUrl);
                CFAPIClient.post(url, requestParams, body, responseHandler);
            }
        }
    }

    public static boolean isOffline() {
        return offline;
    }

    public static void setOffline(boolean offline) {
        CFRestClient.offline = offline;
    }

    public static abstract class DataHandler<T> {
        public void onData(@NotNull T data) { }

        public void onError(@NotNull Throwable error) { }

        public void onFinish() { }
    }

    /**
     * Actual communication logic
     */
    public static class CFAPIClient {
        public static String TAG = getTag(CFAPIClient.class);
        private static String URL;

        private static AsyncHttpClient client;
        private static Context context = CFApp.getContext();

        static {
            client = new AsyncHttpClient();
            client.setTimeout(30 * 1000);
            client.setMaxRetriesAndTimeout(5, 2000);
//            client.setMaxConnections(2);
        }

        private CFAPIClient() {}

        public static String getBaseUrl() {
            return URL;
        }

        public static void setBaseURL(String baseUrl) {
            if (baseUrl.startsWith("http")) {
                URL = baseUrl;
            } else {
                throw new IllegalArgumentException("URL doesn't start with http");
            }
        }

        public static String getAbsoluteUrl(String relativeUrl) {
            if (relativeUrl.startsWith("http")) return relativeUrl;
            if (BuildConfig.DEBUG) Assert.assertNotNull(URL);
            return URL + relativeUrl;
        }

        public static void getOffline(@Nullable final String file, final TextHttpResponseHandler responseHandler) {
            getOffline(file, responseHandler, true);
        }

        public static void getOffline(@Nullable final String file, final TextHttpResponseHandler responseHandler, boolean async) {
            if (file == null) {
                responseHandler.onFailure(404, new Header[0], "", new IOException("Offline file not specified"));
                return;
            }
            try {
                Log.v(TAG, "Reading local file " + file);
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        String response = FileUtils.StrFromFileInAssets(file);
                        responseHandler.onSuccess(200, new Header[0], response.getBytes());
                    }
                };
                if (async) {
                    new Handler().post(r);
                } else {
                    r.run();
                }
            } catch (Exception e) {
                responseHandler.onFailure(500, new Header[0], "Offline Fail!", e);
            }
        }

        public static void post(String url, RequestParams params, TextHttpResponseHandler responseHandler) {
            Log.v(TAG, "POST " + getAbsoluteUrl(url) + " + " + params.toString());
            client.post(getAbsoluteUrl(url), params, responseHandler);
        }

        public static void post(final String url, @Nullable final RequestParams params,
                                @Nullable String jsonBody, final AsyncHttpResponseHandler responseHandler) {
            StringEntity se = null;
            try {
//                se = new StringEntity(StringEscapeUtils.escapeJava(jsonBody));
                se = new StringEntity(jsonBody, HTTP.UTF_8);
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, "Exception", e);
            }
            final StringEntity s = se;
            final String url1 = getAbsoluteUrl(url) + (params != null ? "?" + params : "");
            Log.d(TAG, "POST: " + url1);
            Log.d(TAG, "POST: " + jsonBody);
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    client.post(context, url1, s, "application/json", responseHandler);
                }
            };
            if (!isMainThread()) {
                new Handler(Looper.getMainLooper()).post(runnable);
            } else {
                runnable.run();
            }
        }

        public static void post(@NotNull final String url,
                                final File file,
                                final TextHttpResponseHandler responseHandler) {

            new AsyncTask<String, Void, Void>() {

                Exception error;
                String status;

                @Override
                protected Void doInBackground(String... params) {
                    String absurl = getAbsoluteUrl(url);

                    HttpParams httpParameters = new BasicHttpParams();

                    DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);
                    HttpPost httpPost = new HttpPost(absurl);
                    HttpResponse execute;

                    try {
                        InputStreamEntity reqEntity = new InputStreamEntity(new FileInputStream(file), -1);
                        reqEntity.setContentType("binary/octet-stream");
                        reqEntity.setChunked(false);
                        httpPost.setEntity(reqEntity);

                        Log.i(TAG, "Sending File " + file.getName());
                        execute = httpClient.execute(httpPost);
                        status = execute.toString();
                        Log.i(TAG, "File Sent");

                    } catch (Exception e) {
                        error = e;
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void result) {
                    super.onPostExecute(result);
                    if (error == null) {
                        responseHandler.onSuccess(200, new Header[0], "");
                    } else {
                        responseHandler.onFailure(500, new Header[0], "", new Exception(status + " / " + error.getMessage(), error));
                    }
                }
            }.execute();
        }
    }

    public static class MultiDateDeserializer implements JsonDeserializer<Date> {
        private final String[] formats;

        public MultiDateDeserializer(String[] formats) {
            this.formats = formats;
        }

        @Override
        public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            for (String format : formats) {
                try {
                    return new SimpleDateFormat(format, Locale.US).parse(json.getAsString());
                } catch (ParseException ignored) {
                }
            }
            throw new JsonParseException(String.format("Unparseable date: \"%s\". Supported formats: %s",
                                                       json.getAsString(),
                                                       Arrays.toString(formats)));
        }
    }

    public static class ResponseHandler<T> extends GsonHttpResponseHandler<T> {
        @Nullable private final DataHandler<T> handler;
        private final String name;
        private String TAG;
        public static Gson gson;

        public ResponseHandler(@NotNull Class<T> dataClass, @Nullable DataHandler<T> handler) {
            super(dataClass, gson);
            this.name = dataClass.getSimpleName();
            this.handler = handler;
            TAG = CFRestClient.TAG + " [" + name + "] ";
        }


//        @Override
//        public void onStart() {
//            Log.v(TAG, "onStart");
//        }

//        @Override
//        public void onFinish() {
//            Log.v(TAG, "onFinish");
//        }

        @Override
        public void onSuccess(final T response) {
            try {
                if (response == null) {
                    Log.d(TAG, "returned empty!");
                    if (handler != null) handler.onError(new Exception(EMPTY_MESSAGE));
                } else {
                    if (response instanceof JSONDeserializable) ((JSONDeserializable) response).afterDeserialization();
                    Log.d(TAG, "succeed.");
                    if (handler != null) {
                        handler.onData(response);
                        handler.onFinish();
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "" + e.getMessage(), e);
            }
        }

        @Override
        public void onFailure(int statusCode, @Nullable Header[] headers, @Nullable String responseBody, Throwable error) {
            try {
                if (!(error instanceof NetworkingUtils.NotConnectedException)) {
                    Log.w(TAG, "Error! " + error.getMessage() + " — " + responseBody);
                }
                if (handler != null) {
                    handler.onError(error);
                    handler.onFinish();
                }
            } catch (Exception e) {
                Log.e(TAG, "" + e.getMessage(), e);
            }
        }
    }
}
