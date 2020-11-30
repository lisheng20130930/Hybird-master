package com.qp;

import android.app.Application;
import android.content.res.AssetManager;
import android.os.Handler;

import com.qp.uapp.CmmnHelper;

/**
 * @author Listen.Li
 */
public class BaseApp extends Application {
    private AssetManager nativeAsset= null;
    private Handler handler = null;
    private int DELAY = 50;

    private static BaseApp instance = null;

    /* load so */
    static {
        System.loadLibrary("qpBase");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        CmmnHelper.nativeInit(nativeAsset=getAssets(),CmmnHelper.getDataDir());
        (handler=new Handler()).postDelayed(mCallback,DELAY);
    }

    @Override
    public void onTerminate() {
        handler.removeCallbacks(mCallback);
        CmmnHelper.nativeUint();
        super.onTerminate();
    }

    private Runnable mCallback = new Runnable(){
        @Override
        public void run() {
            handler.postDelayed(mCallback,DELAY);
            CmmnHelper.nativeLoop();
        }
    };

    public static BaseApp getInstance() {
        return instance;
    }
}
