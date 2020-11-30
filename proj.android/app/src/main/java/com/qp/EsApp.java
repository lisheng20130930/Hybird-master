package com.qp;

import android.app.Application;

/**
 * @author Listen.Li
 */
public class EsApp extends Application {

    private static EsApp instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static EsApp getInstance() {
        return instance;
    }
}
