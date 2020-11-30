package com.qp.uapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import android.net.ConnectivityManager;
import android.os.Build;
import android.telephony.TelephonyManager;

import com.qp.EsApp;
import com.qp.utils.SharedPreferenceUtil;

public class CmmnHelper {
    public static int getVersionCode(){
        PackageManager packageManager = EsApp.getInstance().getPackageManager();
        int versionCode = 0;
        try{
            PackageInfo packInfo = packageManager.getPackageInfo(EsApp.getInstance().getPackageName(),0);
            versionCode = packInfo.versionCode;
        }catch (Exception e){
        }
        return versionCode;
    }

    public static String getVersionName(){
        PackageManager packageManager = EsApp.getInstance().getPackageManager();
        try{
            PackageInfo packInfo = packageManager.getPackageInfo(EsApp.getInstance().getPackageName(),0);
            return packInfo.versionName;
        }catch (Exception e){
        }
        return "";
    }

    public static String getPsuedoID(){
        return "Ad" + Build.BOARD.length()%10 + Build.BRAND.length()%10
                + Build.CPU_ABI.length()%10 + Build.DEVICE.length()%10
                + Build.DISPLAY.length()%10 + Build.HOST.length()%10
                + Build.ID.length()%10 + Build.MANUFACTURER.length()%10
                + Build.MODEL.length()%10 + Build.PRODUCT.length()%10
                + Build.TAGS.length()%10 + Build.TYPE.length()%10
                + Build.USER.length()%10;
    }

    public static boolean isNetworkConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager)EsApp.getInstance().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getActiveNetworkInfo() != null;
    }

    public static String getIMEI(Context ctx){
        TelephonyManager mTelephonyMgr = (TelephonyManager) ctx.getSystemService(ctx.TELEPHONY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ctx.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                return getPsuedoID();
            }
        }
        String imei = mTelephonyMgr.getDeviceId();
        if (null != imei && imei.length() > 5) { // min 5 numbers
            return imei;
        }
        return getPsuedoID();
    }

    public static String getDeviceID(){
        String deviceID = SharedPreferenceUtil.getString(EsApp.getInstance(),"device_uuid");
        if(deviceID!=null&&deviceID.length()>5){
            return deviceID;
        }
        deviceID = getIMEI(EsApp.getInstance());
        SharedPreferenceUtil.setString(EsApp.getInstance(),"device_uuid",deviceID);
        return deviceID;
    }

    public static String getMoblieParamter(String key) {
        if(key.equals("channelID")) {
            return AppConstants.CHANNELID;
        }
        if(key.equals("deviceID")) {
            return getDeviceID();
        }
        return "";
    }

    public static String getDataDir(){
        String pszDataDir = EsApp.getInstance().getFilesDir().toString();
        return pszDataDir;
    }
}