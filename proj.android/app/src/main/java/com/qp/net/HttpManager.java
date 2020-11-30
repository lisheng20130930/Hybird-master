package com.qp.net;

import android.webkit.CookieManager;

import com.qp.utils.Logger;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpManager {
    private static ExecutorService pool = Executors.newFixedThreadPool(1);
    public static volatile int counter = 0;

    public static String buildQuery(Map<String,Object> map){
        StringBuilder stringBuilder = new StringBuilder();
        if(!map.isEmpty()){
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String value = entry.getValue().toString();
                if(value==null||value.equals("null")){
                    continue;
                }
                stringBuilder.append(entry.getKey());
                stringBuilder.append("=");
                stringBuilder.append(URLEncoder.encode(value));
                stringBuilder.append("&");
            }
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        }
        return stringBuilder.toString();
    }

    public static String getUrlByMap(String url, Map<String,Object> map){
        if(null==url || url.length()==0 || map == null){
            return url;
        }
        StringBuilder stringBuilder = new StringBuilder(url);
        if(!map.isEmpty()){
            stringBuilder.append("?");
            stringBuilder.append(buildQuery(map));
        }
        return stringBuilder.toString();
    }

    private static Map<String,Object> httpGet(String szUrl){
        HttpURLConnection connection = null;
        InputStreamReader in = null;
        String result = "";
        int status = 0;
        try {
            URL url = new URL(szUrl);
            connection = (HttpURLConnection)url.openConnection();
            connection.setConnectTimeout(35000);
            connection.setReadTimeout(35000);
            CookieManager cookieManager = CookieManager.getInstance();
            String  sCookie = cookieManager.getCookie(szUrl);
            if(null!=sCookie) {
                connection.setRequestProperty("Cookie", sCookie);
            }
            connection.connect();
            status = connection.getResponseCode();
            if(status == 200){
                in = new InputStreamReader(connection.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(in);
                StringBuffer strBuffer = new StringBuffer();
                String line = null;
                while ((line = bufferedReader.readLine()) != null) {
                    strBuffer.append(line);
                }
                result = strBuffer.toString();
            }else{
                result = "";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        Map<String,Object> r = new HashMap<>();
        r.put("status",status);
        r.put("res",result);
        return r;
    }

    private static Map<String,Object> sendPost(String szUrl,String params) {
        HttpURLConnection connection = null;
        InputStreamReader in = null;
        String result = "";
        int status = 0;
        try {
            URL url = new URL(szUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(35000);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Cache-control", "no-cache");
            connection.setReadTimeout(35000);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
            CookieManager cookieManager = CookieManager.getInstance();
            String  sCookie = cookieManager.getCookie(szUrl);
            if(null!=sCookie) {
                connection.setRequestProperty("Cookie", sCookie);
            }
            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.write(params.getBytes("UTF-8"));
            wr.flush();
            wr.close();
            status = connection.getResponseCode();
            if(status == 200){
                in = new InputStreamReader(connection.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(in);
                StringBuffer strBuffer = new StringBuffer();
                String line = null;
                while ((line = bufferedReader.readLine()) != null) {
                    strBuffer.append(line);
                }
                result = strBuffer.toString();
            }else{
                result = "";
            }
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (in != null) {
                try {
                    in.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        Map<String,Object> r = new HashMap<>();
        r.put("status",status);
        r.put("res",result);
        return r;
    }

    public static void request(String method, String szUrl, Map<String,Object> params, HttpCallBack handler){
        counter ++;
        pool.submit(new Runnable() {
            @Override
            public void run() {
                Map<String,Object> r = null;
                if(method.toLowerCase().equals("get")){
                    r = httpGet(getUrlByMap(szUrl,params));
                }else if(method.toLowerCase().equals("post")){
                    r = sendPost(szUrl,buildQuery(params));
                }
                handler.handle(((Integer)(r.get("status"))),(String)r.get("res"));
                counter--;
                Logger.log("[Trace@HttpManager] ======COUNTER = "+counter);
            }
        });
    }

    public static void clear(){
        try {
            pool.shutdownNow();
            pool = null;
        }catch (Exception e){
            Logger.log("[Trace@HttpManager] ===>"+e.getMessage());
        }
    }

    public interface HttpCallBack{
        void handle(int status, String res);
    }
}
