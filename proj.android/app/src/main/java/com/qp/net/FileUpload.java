package com.qp.net;

import android.webkit.CookieManager;
import com.qp.utils.Logger;
import com.qp.utils.MD5Util;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class FileUpload {
    private static FileUpload instance;
    private OkHttpClient mOkHttpClient;

    private FileUpload() {
        mOkHttpClient = new OkHttpClient.Builder()
                .connectTimeout(120,TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(120,TimeUnit.SECONDS)
                .callTimeout(300, TimeUnit.SECONDS)
                .retryOnConnectionFailure(false)
                .build();
    }

    public static FileUpload getInstance() {
        if (instance == null) {
            synchronized (FileUpload.class) {
                if (instance == null) {
                    instance = new FileUpload();
                }
            }
        }
        return instance;
    }

    public void upload(File file, String requestURL, OnUploadListener listener){
        MultipartBody.Builder requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM);
        RequestBody body = RequestBody.create(MediaType.parse("application/octet-stream"), file);
        String filename = null;
        try {
            filename = URLEncoder.encode(file.getName(), "UTF-8");
        }catch (Exception e){
            filename = MD5Util.MD5(file.getName());
            Logger.log("[Exception@FileUpload] pos=00M&&message="+e.getMessage());
        }
        requestBody.addFormDataPart("file", filename, body);
        CookieManager cookieManager = CookieManager.getInstance();
        String  sCookie = cookieManager.getCookie(requestURL);
        Request.Builder builder = new Request.Builder();
        builder.url(requestURL)
                .post(requestBody.build());
        if(sCookie!=null&&sCookie.length()>0){
            builder.addHeader("Cookie", sCookie);
        }
        Request request = builder.build();

        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Logger.log("[Trace@OKHttp] Error ====>"+e.getMessage());
                listener.onUploadCallback(500,"{}");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                listener.onUploadCallback(200,response.body().string());
            }
        });
    }

    public interface OnUploadListener{
        void onUploadCallback(int status, String res);
    }
}