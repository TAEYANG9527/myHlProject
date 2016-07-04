package com.itcalf.renhe.http;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * OkHttp implements HTTP Request
 *
 * @author YuZhiQiang
 */
public class OkHttpSender implements HttpSender {
    public static final MediaType MEDIA_TYPE_MARKDOWN = MediaType.parse("text/x-markdown; charset=utf-8");
    private static final String t = OkHttpSender.class.getSimpleName();
    static OkHttpClient client = new OkHttpClient();
    private static boolean log = true;

    static {
        client.setConnectTimeout(15, TimeUnit.SECONDS);
    }

    private String tag;

    public OkHttpSender(String tag) {
        this.tag = tag;
    }

    public static void disableLog() {
        log = false;
    }

    static String checkErrMsg(IOException ex) {
        if (ex instanceof UnknownHostException) {
            return "网络未连接";
        } else if (ex instanceof SocketTimeoutException) {
            return "网络请求超时";
        } else if (ex instanceof ConnectException) {
            return "连接服务器失败";
        }
        return "网络请求失败";
    }

    @Override
    public <T> void get(int id, String url, Map<String, Serializable> params, Map<String, Serializable> headers, Class<T> clazz) {
        Request.Builder builder = new Request.Builder();
        addHeaders(headers, builder);

        Request request = builder.url(appendUrl(url, params)).tag(tag).build();
        handleRequest(id, request, clazz);
    }

    @Override
    public void get(int id, String url, Map<String, Serializable> params, Map<String, Serializable> headers) {
        get(id, url, params, null, null);
    }

    @Override
    public <T> void get(int id, String url, Map<String, Serializable> params, Class<T> clazz) {
        get(id, url, params, null, clazz);
    }

    @Override
    public void get(int id, String url) {
        get(id, url, null, null, null);
    }

    @Override
    public <T> void get(int id, String url, Class<T> clazz) {
        get(id, url, null, null, clazz);
    }

    @Override
    public <T> void post(int id, String url, Map<String, Serializable> params, Map<String, Serializable> headers, Class<T> clazz) {
        FormEncodingBuilder formEncodingBuilder = new FormEncodingBuilder();
        // add parameters
        for (Map.Entry<String, Serializable> entry : params.entrySet()) {
            formEncodingBuilder.add(entry.getKey(), entry.getValue().toString());
        }
        RequestBody formBody = formEncodingBuilder.build();

        Request.Builder builder = new Request.Builder();
        addHeaders(headers, builder);

        Request request = builder.url(url).tag(tag).post(formBody).build();
        if (log) {
            Log.d(t, url + " params : " + params);
        }
        handleRequest(id, request, clazz);
    }

    @Override
    public void post(int id, String url, Map<String, Serializable> params, Map<String, Serializable> headers) {
        post(id, url, params, headers, null);
    }

    @Override
    public <T> void post(int id, String url, Map<String, Serializable> params, Class<T> clazz) {
        post(id, url, params, null, clazz);
    }

    @Override
    public void post(int id, String url, Map<String, Serializable> params) {
        post(id, url, params, null, null);
    }

    public <T> void uploadFilesWithProgress(int id, String url, Map<String, Serializable> params,
                                            CountingRequestBody.ProgressListener progressListener, Class<T> clazz) {
        MultipartBuilder builder = new MultipartBuilder().type(MultipartBuilder.FORM);
        if (params != null) {
            for (Map.Entry<String, Serializable> entry : params.entrySet()) {
                Serializable value = entry.getValue();
                if (value instanceof File) {
                    File file = (File) value;
                    builder.addFormDataPart(entry.getKey(), file.getName(),
                            new CountingRequestBody(RequestBody.create(MediaType.parse("file/file"), file), progressListener));
                } else {
                    builder.addFormDataPart(entry.getKey(), entry.getValue().toString());
                }
            }
        }
        Request request = new Request.Builder().header("Authorization", "Client-ID " + "android").url(url).post(builder.build())
                .build();
        handleRequest(id, request, clazz);
    }

    public void downloadFile(int id, String url, File file, CountingRequestBody.ProgressListener progressListener) {

    }

    /**
     * add headers
     *
     * @param headers
     * @param builder
     */
    private void addHeaders(Map<String, Serializable> headers, Request.Builder builder) {
        if (headers != null && !headers.isEmpty()) {
            for (Map.Entry<String, Serializable> entry : headers.entrySet()) {
                builder.addHeader(entry.getKey(), entry.getValue().toString());
            }
        }
    }

    private String appendUrl(String url, Map<String, Serializable> params) {
        if (params != null && !params.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append(url);
            sb.append("?");
            for (Map.Entry<String, Serializable> entry : params.entrySet()) {
                sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
            sb.deleteCharAt(sb.length() - 1);
            if (log) {
                Log.d(t, sb.toString());
            }
            return sb.toString();
        }

        return url;
    }

    private <T> void handleRequest(final int id, Request request, final Class<T> clazz) {
        Call call = client.newCall(request);
        // call.execute();
        call.enqueue(new com.squareup.okhttp.Callback() {

            @Override
            public void onResponse(final Response response) throws IOException {

                boolean success = true;
                String msg = null;
                String content = response.body().string();
                Object result = null;
                if (log) {
                    Log.d(t, content + "");
                }
                final Callback callback = TaskManager.getInstance().get(id);
                if (callback == null) {
                    return;
                }
                if (clazz == null) {
                    result = content;
                } else {
                    try {
                        result = JSON.parseObject(content, clazz);
                        if (result != null) {
                            callback.cacheData(id, result);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        msg = "数据解析失败";
                        success = false;
                    }
                }
                new Handler(Looper.getMainLooper()).post(new MyRunnable(success, id, result, msg));

            }

            @Override
            public void onFailure(Request request, IOException ex) {
                String msg = checkErrMsg(ex);
                if (log) {
                    Log.e(t, msg + "");
                }
                new Handler(Looper.getMainLooper()).post(new MyRunnable(false, id, null, msg));
            }
        });
    }

    @Override
    public void cancel(Object tag) {
        client.cancel(tag);
    }

    private static class MyRunnable implements Runnable {
        boolean success;
        int id;
        Object result;
        String msg;

        public MyRunnable(boolean success, int id, Object result, String msg) {
            this.success = success;
            this.id = id;
            this.result = result;
            this.msg = msg;
        }


        @Override
        public void run() {
            Callback callback = TaskManager.getInstance().get(id);
            if (callback == null) {
                return;
            }
            if (success) {
                callback.onSuccess(id, result);
            } else {
                callback.onFailure(id, msg);
            }
        }
    }

}
