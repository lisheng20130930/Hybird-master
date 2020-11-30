package com.qp.net;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;
import android.webkit.CookieManager;

import com.qp.utils.Logger;


public class FileUpload {
    private static final String TAG = "uploadFile";
    private static final int TIME_OUT = 10*10000000; //超时时间
    private static final String CHARSET = "utf-8"; //设置编码

    public static String uploadFile(File file,String RequestURL) {
        String BOUNDARY = UUID.randomUUID().toString();
        String PREFIX = "--" ;
        String LINE_END = "\r\n";
        String CONTENT_TYPE = "multipart/form-data";
        String result = "";

        try {
            URL url = new URL(RequestURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection(); conn.setReadTimeout(TIME_OUT); conn.setConnectTimeout(TIME_OUT);
            conn.setDoInput(true); //允许输入流
            conn.setDoOutput(true); //允许输出流
            conn.setUseCaches(false); //不允许使用缓存
            conn.setRequestMethod("POST"); //请求方式
            conn.setRequestProperty("Charset", CHARSET);//设置编码
            conn.setRequestProperty("connection", "keep-alive");
            conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary=" + BOUNDARY);

            CookieManager cookieManager = CookieManager.getInstance();
            String  sCookie = cookieManager.getCookie(RequestURL);
            if(null!=sCookie) {
                conn.setRequestProperty("Cookie", sCookie);
            }

            if(file!=null) {
                OutputStream outputSteam=conn.getOutputStream();
                DataOutputStream dos = new DataOutputStream(outputSteam);
                StringBuffer sb = new StringBuffer();
                sb.append(PREFIX);
                sb.append(BOUNDARY); sb.append(LINE_END);

                sb.append("Content-Disposition: form-data; name=\"file\"; filename=\""+file.getName()+"\""+LINE_END);
                sb.append("Content-Type: application/octet-stream; charset="+CHARSET+LINE_END);
                sb.append(LINE_END);
                dos.write(sb.toString().getBytes());
                InputStream is = new FileInputStream(file);
                byte[] bytes = new byte[1024];
                int len = 0;
                while((len=is.read(bytes))!=-1){
                    dos.write(bytes, 0, len);
                }
                is.close();
                dos.write(LINE_END.getBytes());
                byte[] end_data = (PREFIX+BOUNDARY+PREFIX+LINE_END).getBytes();
                dos.write(end_data);
                dos.flush();
                int res = conn.getResponseCode();
                if(res==200){
                    InputStreamReader in = new InputStreamReader(conn.getInputStream());
                    BufferedReader bufferedReader = new BufferedReader(in);
                    StringBuffer strBuffer = new StringBuffer();
                    String line = null;
                    while ((line = bufferedReader.readLine()) != null) {
                        strBuffer.append(line);
                    }
                    result = strBuffer.toString();
                    return result;
                }else{
                    Logger.log("[Trace@FileUpload] upload Error: response code:"+res);
                    result = null;
                }
            }
        } catch (Exception e){
            Logger.log("[Trace@FileUpload] upload Error (2)==>"+e.getMessage());
            e.printStackTrace();
        }

        return null;
    }
}