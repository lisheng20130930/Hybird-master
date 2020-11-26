package com.qp.utils;

import android.widget.Toast;

import com.qp.App;

/**
 * @author Listen.Li
 */
public class ToastUtils {
    private static Toast toast;

    public static void show(String msg) {
        if (toast == null) {
            toast = Toast.makeText(App.getInstance(), msg, Toast.LENGTH_LONG);
        } else {
            toast.setText(msg);
        }
        toast.show();
    }

    public static void show(int msg) {
        if (toast == null) {
            toast = Toast.makeText(App.getInstance(), msg + "", Toast.LENGTH_LONG);
        } else {
            toast.setText(msg + "");
        }
        toast.show();
    }
}
