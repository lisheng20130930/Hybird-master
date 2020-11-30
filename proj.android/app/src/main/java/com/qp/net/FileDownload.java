package com.qp.net;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class FileDownload {

    public static void download(String szURL,String filePathName, OnDownloadListener listener){
        new Thread() {
            public void run() {
                try {
                    URL url = new URL(szURL);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setReadTimeout(15000);
                    con.setConnectTimeout(15000);
                    con.setRequestProperty("Charset", "UTF-8");
                    con.setRequestMethod("GET");
                    if (con.getResponseCode() == 200) {
                        int length = con.getContentLength();
                        InputStream is = con.getInputStream();
                        FileOutputStream fileOutputStream = null;
                        if (is != null) {
                            File file = new File(filePathName);
                            fileOutputStream = new FileOutputStream(file);
                            byte[] buf = new byte[4096];
                            int ch;
                            long process = 0;
                            while ((ch = is.read(buf)) != -1) {
                                fileOutputStream.write(buf, 0, ch);
                                process += ch;
                                listener.onDownloading((int)(process*100/length));
                            }
                        }
                        if (fileOutputStream != null) {
                            fileOutputStream.flush();
                            fileOutputStream.close();
                        }
                        listener.onDownloadSuccess(filePathName);
                    }
                } catch (Exception e) {
                    listener.onDownloadFailed(e);
                }
            }
        }.start();
    }

    public interface OnDownloadListener{
        void onDownloadSuccess(String filePathName);
        void onDownloading(int progress);
        void onDownloadFailed(Exception e);
    }
}
