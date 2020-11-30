package com.qp.uapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.FrameLayout;

import com.qp.EsActivity;
import com.qp.hybird.BuildConfig;
import com.qp.hybird.HBPlugin;
import com.qp.hybird.HBWebView;
import com.qp.hybird.R;
import com.qp.utils.Logger;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;
import com.zhihu.matisse.internal.entity.CaptureStrategy;

import java.util.List;
import java.util.Set;

import butterknife.BindView;

public class HBActivity extends EsActivity {
    protected static final FrameLayout.LayoutParams COVER_SCREEN_PARAMS = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    private View customView;
    private FrameLayout fullscreenContainer;
    private WebChromeClient.CustomViewCallback customViewCallback;
    private static final int MEDIA_REQUEST = 320;

    @BindView(R.id.iv_web_view)
    HBWebView webView;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_hybird;
    }

    @Override
    protected void initEventAndData() {
        HBPlugin.registerExtender(new HBExtender());
        webView.initHybrid(this);
        webView.setWebChromeClient(new QChromeWebClient());
        loadURL("file:///android_asset/fileInput.html");
    }

    private void loadURL(String url){
        Logger.log("[Trace@H5App] load URL ="+url);
        webView.loadUrl(url);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        webView.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        if(requestCode!=MEDIA_REQUEST||_filePathCallback==null){
            return;
        }
        if(hasPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE)){
            _showFileChooser();
        }else{
            showMsg("权限未允许");
            _filePathCallback.onReceiveValue(new Uri[]{});
            _filePathCallback = null;
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if(requestCode!=MEDIA_REQUEST||_filePathCallback==null){
            return;
        }
        _filePathCallback.onReceiveValue(new Uri[]{});
        _filePathCallback = null;
    }

    static class FullscreenHolder extends FrameLayout {
        public FullscreenHolder(Context ctx) {
            super(ctx);
            setBackgroundColor(ctx.getResources().getColor(android.R.color.black));
        }
        @Override
        public boolean onTouchEvent(MotionEvent evt) {
            return true;
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                /** 回退键 事件处理 优先级:视频播放全屏-网页回退-关闭页面 */
                if (customView != null) {
                    hideCustomView();
                    return true;
                }
                try {
                    webView.loadUrl("javascript:CB_onKeyback();");
                }catch (Exception e){
                    e.printStackTrace();
                }
                return true;
            default:
                return super.onKeyUp(keyCode, event);
        }
    }

    private void setStatusBarVisibility(boolean visible) {
        int flag = visible ? 0 : WindowManager.LayoutParams.FLAG_FULLSCREEN;
        getWindow().setFlags(flag, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if(!visible) {
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            }else {
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Logger.log("[Trace@H5App] onActivityResult requestCode="+requestCode+",resultCode="+resultCode);
        super.onActivityResult(requestCode, resultCode, data);
        if(MEDIA_REQUEST==requestCode && null!=_filePathCallback){
            Uri result[] = null;
            if(resultCode!=RESULT_OK || MEDIA_REQUEST!=requestCode && null==data){
                result = new Uri[]{};
            }else {
                List<String> medias = Matisse.obtainPathResult(data);
                if (medias.size() <= 0) {
                    result = new Uri[]{};
                } else {
                    result = new Uri[medias.size()];
                    for (int i = 0; i < medias.size(); i++) {
                        result[i] = Uri.parse("file://" + medias.get(i));
                    }
                }
            }
            _filePathCallback.onReceiveValue(result);
            _filePathCallback = null;
        }else {
            webView.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void showCustomView(View view, WebChromeClient.CustomViewCallback callback) {
        if (customView != null) {
            callback.onCustomViewHidden();
            return;
        }
        FrameLayout parent = (getWindow().getDecorView()).findViewById(R.id.container);
        fullscreenContainer = new FullscreenHolder(HBActivity.this);
        fullscreenContainer.addView(view, COVER_SCREEN_PARAMS);
        parent.addView(fullscreenContainer, COVER_SCREEN_PARAMS);
        customView = view;
        webView.setVisibility(View.GONE);
        setStatusBarVisibility(false);
        customViewCallback = callback;
    }

    private void hideCustomView() {
        if (customView == null) {
            return;
        }
        webView.setVisibility(View.VISIBLE);
        setStatusBarVisibility(true);
        FrameLayout parent = (getWindow().getDecorView()).findViewById(R.id.container);
        parent.removeView(fullscreenContainer);
        fullscreenContainer = null;
        customView = null;
        customViewCallback.onCustomViewHidden();
    }

    private ValueCallback<Uri[]> _filePathCallback = null;
    private String _acctypes[] = null;

    private boolean _showFileChooser(){
        Set<MimeType> mimeTypes = null;
        if(_acctypes[0].contains("image")){
            mimeTypes = MimeType.of(MimeType.JPEG,MimeType.PNG);
        }else if(_acctypes[0].contains("vedio")){
            mimeTypes = MimeType.of(MimeType.MP4,MimeType.THREEGPP);
        }else{
            mimeTypes = MimeType.of(MimeType.JPEG,MimeType.PNG,MimeType.MP4,MimeType.THREEGPP);
        }
        Matisse.from(HBActivity.this)
                .choose(mimeTypes)
                .maxSelectablePerMediaType(9,1)
                .countable(false)
                .showSingleMediaType(true)
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                .thumbnailScale(0.85F)
                .imageEngine(new GlideEngine())
                .forResult(MEDIA_REQUEST);
        return true;
    }

    private boolean showFileChooser(String acctypes[], ValueCallback<Uri[]> filePathCallback){
        if(null != _filePathCallback){
            _filePathCallback.onReceiveValue(null);
            _filePathCallback = null;
        }
        _filePathCallback = filePathCallback;
        _acctypes = acctypes;
        if(!hasPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE)){
            requestPermissions(MEDIA_REQUEST, Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE);
            return true;
        }
        return _showFileChooser();
    }

    private class QChromeWebClient extends WebChromeClient {
        public void onGeolocationPermissionsShowPrompt(String origin, android.webkit.GeolocationPermissions.Callback callback) {
            callback.invoke(origin, true, true);
            super.onGeolocationPermissionsShowPrompt(origin, callback);
        }

        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
            String acctypes[] = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                acctypes = fileChooserParams.getAcceptTypes();
            }
            if(acctypes==null||acctypes.length<=0){
                return false;
            }
            return showFileChooser(acctypes,filePathCallback);
        }

        @Override
        public View getVideoLoadingProgressView() {
            FrameLayout frameLayout = new FrameLayout(HBActivity.this);
            frameLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            return frameLayout;
        }

        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            showCustomView(view, callback);
        }

        @Override
        public void onHideCustomView() {
            hideCustomView();
        }
    }
}
