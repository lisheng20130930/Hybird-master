package com.qp;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.github.ikidou.fragmentBackHandler.BackHandlerHelper;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.qp.utils.ToastUtils;

import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

/**
 * @author Listen.Li
 */
public class BaseActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {
    private KProgressHUD show;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        show = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("加载中....")
                .setCancellable(true);
    }

    @Override
    public void onBackPressed() {
        if (!BackHandlerHelper.handleBackPress(this)) {
            super.onBackPressed();
        }
    }

    public void ToOtherActivity(Class clazz,boolean isNewTask, String extra) {
        Intent intent = new Intent(this, clazz);
        if(isNewTask){
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        if(null!=extra){
            intent.putExtra("extra",extra);
        }
        startActivity(intent);
    }

    public void showWaitDialog() {
        show.show();
    }

    public void closeWaitDialog() {
        try {
            show.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showMsg(String msg) {
        ToastUtils.show(msg);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    public boolean hasPermissions(String ...permissions){
        return EasyPermissions.hasPermissions(this,permissions);
    }

    public void requestPermissions(int requestCode, String rationale, String ...permissions){
        EasyPermissions.requestPermissions(this, rationale, requestCode, permissions);
    }
}
