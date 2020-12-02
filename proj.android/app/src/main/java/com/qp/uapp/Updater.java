package com.qp.uapp;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;

import com.qp.hybird.BuildConfig;
import com.qp.hybird.HBAssetMgr;
import com.qp.net.HttpManager;

import org.json.JSONObject;

import java.io.File;


public class Updater {
    public static void appLaunch(String szUrl, ILaunchCallBack callBack){
        String jsonStr = null;
        try {
            JSONObject json = new JSONObject();
            json.put("versionCode", HBAssetMgr.assetMgrVersionCode());
            json.put("channelId", AppConstants.CHANNELID);
            json.put("deviceID", CmmnHelper.getDeviceID());
            jsonStr = json.toString();
        }catch (Exception e){
            e.printStackTrace();
        }
        if(null==jsonStr){
            callBack.onLaunchCallBack(-1,null);
            return;
        }
        HttpManager.getInstance().postWithJson(szUrl, jsonStr, new HttpManager.HttpCallBack() {
            @Override
            public void onSuccess(String rsp) {
                callBack.onLaunchCallBack(0,rsp);
            }
            @Override
            public void onError() {
                callBack.onLaunchCallBack((-1),null);
            }
        });
    }

    public static void installApk(Activity activity, File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >=  Build.VERSION_CODES.N) {
            Uri apkUri = FileProvider.getUriForFile(activity, BuildConfig.APPLICATION_ID+".fileprovider", file);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        }
        activity.startActivity(intent);
    }

    public interface ILaunchCallBack{
        void onLaunchCallBack(int errorCode, String rsp);
    }
}
