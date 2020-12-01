package com.qp.net;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpManager {
    private static HttpManager instance;
    private OkHttpClient mOkHttpClient;

    private HttpManager() {
        mOkHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    public static HttpManager getInstance() {
        if (instance == null) {
            synchronized (HttpManager.class) {
                if (instance == null) {
                    instance = new HttpManager();
                }
            }
        }
        return instance;
    }

    public void get(String url, MyCallBack callBack) {
        Request request = bulidRequestForGet(url);
        requestNetWork(request, callBack);
    }

    public void postWithFormData(String url, Map<String, String> parms, MyCallBack callBack) {
        Request request = bulidRequestForPostByForm(url, parms);
        requestNetWork(request, callBack);
    }

    public void postWithJson(String url, String json, MyCallBack callBack) {
        Request request = bulidRequestForPostByJson(url, json);
        requestNetWork(request, callBack);
    }

    private Request bulidRequestForPostByJson(String url, String json) {
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json);
        return new Request.Builder()
                .url(url)
                .post(body)
                .build();
    }

    private Request bulidRequestForPostByForm(String url, Map<String, String> parms) {
        FormBody.Builder builder = new FormBody.Builder();
        if (parms != null) {
            for (Map.Entry<String, String> entry :
                    parms.entrySet()) {
                builder.add(entry.getKey(), entry.getValue());
            }
        }
        FormBody body = builder.build();
        return new Request.Builder()
                .url(url)
                .post(body)
                .build();
    }

    private Request bulidRequestForGet(String url) {
        return new Request.Builder()
                .url(url)
                .get()
                .build();
    }

    private void requestNetWork(Request request, MyCallBack callBack) {
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callBack.onError();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException{
                if (response.isSuccessful()) {
                    callBack.onSuccess(response.body().string());
                } else {
                    callBack.onError();
                }
            }
        });
    }

    public interface MyCallBack {
        void onSuccess(String rsp);
        void onError();
    }
}
