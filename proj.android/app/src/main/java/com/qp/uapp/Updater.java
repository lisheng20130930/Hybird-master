package com.qp.uapp;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;

import com.alibaba.fastjson.JSON;
import com.qp.hybird.BuildConfig;
import com.qp.hybird.HBAssetMgr;
import com.qp.net.HttpManager;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Response;

public class Updater {
    public static void appLaunch(String szUrl, ILaunchCallBack callBack){
        Map<String,Object> params = new HashMap<>();
        params.put("versionCode", HBAssetMgr.assetMgrVersionCode());
        params.put("channelId", AppConstants.CHANNELID);
        params.put("deviceID", CmmnHelper.getDeviceID());
        HttpManager.getInstance().postWithJson(szUrl, JSON.toJSONString(params), new HttpManager.MyCallBack() {
            @Override
            public void onSuccess(Response response) throws IOException {
                callBack.onLaunchCallBack(0,response.body().string());
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
