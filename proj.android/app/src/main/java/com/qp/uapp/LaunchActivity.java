package com.qp.uapp;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.qp.EsActivity;
import com.qp.hybird.HBAssetMgr;
import com.qp.hybird.R;
import com.qp.net.FileDownload;
import com.qp.utils.DataCleanManager;
import com.qp.utils.Logger;
import com.qp.utils.SharedPreferenceUtil;

import org.json.JSONObject;

import java.io.File;
import java.util.List;

import butterknife.BindView;

public class LaunchActivity extends EsActivity implements Updater.ILaunchCallBack{
    @BindView(R.id.iv_label)
    TextView mIVLabel;
    @BindView(R.id.iv_jump)
    TextView  mJumpLabel;
    static int BASIC_REQUEST_CODE = 130;
    Handler handler = new Handler();
    boolean bIsFirstLaunch = false;
    int recLen = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        super.onCreate(savedInstanceState);
    }

    private Runnable cbHotupdate = new Runnable(){
        @Override
        public void run() {
            HBAssetMgr.assetMgrUpdate();
            int status = HBAssetMgr.assetMgrGetStatus();
            if(9==status||10==status){
                mIVLabel.setVisibility(View.INVISIBLE);//Hide Label
                handler.removeCallbacks(cbHotupdate);
                startCountDown(true);
                return;
            }
            //update UI Label
            int totalCount = HBAssetMgr.assetMgrGetTotalCount();
            if(totalCount>0) {
                mIVLabel.setText("正在更新"+HBAssetMgr.assetMgrGetCurrCount()+"/"+totalCount+"...");
            }
            handler.postDelayed(cbHotupdate,50);
        }
    };

    public void gotoMainActivity(){
        Intent intent = new Intent();
        intent.setClass(this, HBActivity.class);
        startActivity(intent);
        finish();
    }

    private Runnable cbCountdown = new Runnable(){
        @Override
        public void run() {
            recLen--;
            mJumpLabel.setText("跳过 " + recLen);
            if (recLen <= 0) {
                handler.removeCallbacks(cbCountdown);
                gotoMainActivity();
            }else{
                handler.postDelayed(cbCountdown,1000);
            }
        }
    };

    private void startCountDown(boolean clearCache) {
        if(clearCache){
            DataCleanManager.clearAllCache(this);
        }
        mJumpLabel.setText("跳过 " + recLen);
        mJumpLabel.setVisibility(View.VISIBLE);
        handler.postDelayed(cbCountdown,1000);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_launch;
    }

    protected boolean isFirstLaunch(){
        String szKey = "KEY_firstLaunch";
        String value = SharedPreferenceUtil.getString(this, szKey);
        if(value.length()>0){
            return false;
        }
        SharedPreferenceUtil.setString(this, szKey,"NO");
        Logger.log("[Trace@LaunchActivity] first Launch = YES");
        return true;
    }

    @Override
    protected void initEventAndData() {
        bIsFirstLaunch = isFirstLaunch();
        if (!isTaskRoot()) {
            finish();
            return;
        }
        mJumpLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handler.removeCallbacks(cbCountdown);
                Logger.log("[Trace@LaunchActivity] skip Clicked at("+recLen+")seconds");
                gotoMainActivity();
            }
        });
        mJumpLabel.setVisibility(View.GONE);
        Logger.log("[Client@H5App] action=launch&&deviceID="+CmmnHelper.getDeviceID()+"&&channelId="+ AppConstants.CHANNELID+"&&version="+CmmnHelper.getVersionCode()+"&&isNewDevice="+(bIsFirstLaunch?"YES":"NO"));
        Logger.log("[Trace@DeviceInfo] mobileInfo="+ CmmnHelper.deviceInfo());
        Updater.appLaunch(AppConstants.G_SERVICE, this);
    }

    protected void checkVersionUpdateRealDo(int errorCode, String str){
        Logger.log("[Trace@LaunchActivity] checkVersionUpdateRealDo errorCode="+errorCode);
        if(errorCode!=0) { // ERROR
            startCountDown(false);
            return;
        }
        Logger.log("[Trace@LaunchActivity] Info: str="+str);
        int updateTag = 0;
        String szUrl = null;
        try {
            JSONObject json = new JSONObject(str);
            int responseCode = json.getInt("code");
            if(200==responseCode) {
                updateTag = json.getInt("updateTag");
                if (updateTag == 1 || updateTag == 2) {
                    szUrl = json.getString("szUrl");
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        if(updateTag==1 && null!=szUrl) {
            mIVLabel.setVisibility(View.VISIBLE);
            HBAssetMgr.assetMgrSetConfig(szUrl);
            handler.postDelayed(cbHotupdate,50);
            return;
        }

        if(updateTag==2 && null!=szUrl){
            final String _szUrl = szUrl;
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("提示");
            builder.setMessage("发现新版本，请更新!");
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which){
                    dialog.dismiss();
                    startDownload(_szUrl);
                }
            });
            builder.show();
            return;
        }
        startCountDown(false);
    }

    @Override
    public void onLaunchCallBack(int errorCode, String str) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                checkVersionUpdateRealDo(errorCode,str);
            }
        });
    }

    public static final String SAVE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath()+"/h5app.apk";
    protected String downloadURL = null;

    protected void startDownload(String szUrl){
        DataCleanManager.clearAllCache(this);
        downloadURL = szUrl;
        if(hasPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            startDownload();
        }else{
            requestPermissions(BASIC_REQUEST_CODE,Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        startDownload();
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        showMsg("允许使用存储权限才能下载新版本!");
    }

    protected void startDownload(){
        File file = new File(SAVE_PATH);
        if (file.exists()) {
            file.delete();
        }
        downFile(downloadURL);
    }

    protected void downFile(String szURL) {
        final ProgressDialog pBar = new ProgressDialog(this);
        pBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pBar.setCancelable(false);
        pBar.setTitle("正在下载...");
        pBar.setMessage("请稍候...");
        pBar.setProgress(0);
        pBar.setMax(100);
        pBar.show();
        FileDownload.getInstance().download(szURL, SAVE_PATH, new FileDownload.OnDownloadListener() {
            @Override
            public void onDownloadSuccess() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pBar.cancel();
                        Updater.installApk(LaunchActivity.this,new File(SAVE_PATH));
                    }
                });
            }

            @Override
            public void onDownloading(int progress) {
                pBar.setProgress(progress);
            }

            @Override
            public void onDownloadFailed() {
                Logger.log("[Trace@LaunchActivity] thread download apk ERROR");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pBar.cancel();
                        showMsg("下载失败,请重试!");
                    }
                });
            }
        });
    }
}
