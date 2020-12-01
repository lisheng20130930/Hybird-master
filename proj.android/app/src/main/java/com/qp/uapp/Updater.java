package com.qp.uapp;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;

import com.qp.hybird.BuildConfig;
import com.qp.hybird.HBAssetMgr;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Updater {
    public static void appLaunch(String szUrl, ILaunchCallBack callBack){
        Map<String,Object> params = new HashMap<>();
        params.put("versionCode", HBAssetMgr.assetMgrVersionCode());
        params.put("channelId", AppConstants.CHANNELID);
        params.put("deviceID", CmmnHelper.getDeviceID());
        HttpManager.request("POST", szUrl, params, new HttpManager.HttpCallBack() {
            @Override
            public void handle(int status, String res) {
                callBack.onLaunchCallBack((200==status)?0:(-1),res);
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
