package com.qp.utils;

import android.widget.Toast;

import com.qp.BaseApp;

/**
 * @author Listen.Li
 */
public class ToastUtils {
    private static Toast toast;

    public static void show(String msg) {
        if (toast == null) {
            toast = Toast.makeText(BaseApp.getInstance(), msg, Toast.LENGTH_LONG);
        } else {
            toast.setText(msg);
        }
        toast.show();
    }

    public static void show(int msg) {
        if (toast == null) {
            toast = Toast.makeText(BaseApp.getInstance(), msg + "", Toast.LENGTH_LONG);
        } else {
            toast.setText(msg + "");
        }
        toast.show();
    }
}
