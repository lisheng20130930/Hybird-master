package com.qp.hybird;

import android.content.Context;
import android.os.Handler;

import com.qp.uapp.CmmnHelper;

public class HBAssetMgr{
    private Handler handler = null;
    private int DELAY = 50;

    public HBAssetMgr(Context context, String szUrl){
        assetMgrSetConfig(CmmnHelper.getDataDir(), szUrl);
        (handler=new Handler()).postDelayed(mCallback,DELAY);
    }

    public void clear(){
        handler.removeCallbacks(mCallback);
        assetMgrClear();
    }

    protected Runnable mCallback = new Runnable(){
        @Override
        public void run() {
            handler.postDelayed(mCallback,DELAY);
            assetMgrLoop();
        }
    };

    public static native void assetMgrSetConfig(String strDataDir, String szUrl);
    public static native void assetMgrUpdate();
    public static native int assetMgrGetTotalCount();
    public static native int assetMgrGetCurrCount();
    public static native int assetMgrGetStatus();
    public static native void assetMgrRemoveALL();
    public static native void assetMgrLoop();
    public static native void assetMgrClear();
}
