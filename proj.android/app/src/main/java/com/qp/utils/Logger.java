package com.qp.utils;

import com.qp.App;

import java.io.FileOutputStream;

/**
 * @author Listen.Li
 */
public class Logger {
	//public static String strPath = Environment.getExternalStorageDirectory().getPath() + "/Demo_LOG.txt";
	public static String strPath = App.getInstance().getFilesDir().getPath() + "/Demo.log";

	/**
	 * LOG
	 */
	public static synchronized void log(String txt) {
		android.util.Log.e("LOG", txt);
		synchronized(Logger.class) {
			try {
				FileOutputStream out = new FileOutputStream(strPath, true);
				out.write((txt + "\r\n").getBytes());
				out.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
