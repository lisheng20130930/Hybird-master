package com.qp.hybird;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.util.AttributeSet;
import android.webkit.CookieManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.qp.utils.Logger;

import java.lang.ref.WeakReference;

public class HBWebView extends WebView {
    private WeakReference<Activity> reference = null;
    private HBJSBridge bridge = null;

    public HBWebView(Context context) {
        super(context);
    }

    public HBWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HBWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public HBWebView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void initHybrid(Activity activity){
        CookieManager cookieManager = CookieManager.getInstance();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            cookieManager.setAcceptThirdPartyCookies(this,true);
        } else {
            cookieManager.setAcceptCookie(true);
        }
        initWebviewSettings(activity,this);
        getSettings().setMediaPlaybackRequiresUserGesture(false);
        //mWebView.setWebContentsDebuggingEnabled(true);
        setWebViewClient(new QWebViewClient());
        bridge = new HBJSBridge(this);
        addJavascriptInterface(bridge, "bridge");
        reference = new WeakReference<>(activity);
    }

    public Activity getActivity() {
        return reference.get();
    }

    private void initWebviewSettings(Context ctx, WebView webView){
        WebSettings settings =  webView.getSettings();
        settings.setUseWideViewPort(false);
        settings.setLoadWithOverviewMode(false);
        settings.setDomStorageEnabled(true);
        settings.setDefaultTextEncodingName("UTF-8");
        settings.setAllowContentAccess(true);
        settings.setAllowFileAccess(true);
        settings.setJavaScriptEnabled(true);
        settings.setGeolocationEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setAllowFileAccessFromFileURLs(true);
        settings.setAllowUniversalAccessFromFileURLs(true);
    }

    public boolean onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        return bridge.onRequestPermissionsResult(requestCode,permissions,grantResults);
    }

    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        return bridge.onActivityResult(requestCode,resultCode,data);
    }

    private class QWebViewClient extends WebViewClient{
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Logger.log("[Trace@H5App] Android ccLoadURL, url="+url);
            if (url.startsWith("tel:")) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                Uri data = Uri.parse(url);
                intent.setData(data);
                getActivity().startActivity(intent);
                return true;
            }
            view.loadUrl(url);
            return true;
        }
        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            Logger.log("[Exception@H5App] pos=006&&message=onReceivedError1"+description);
            super.onReceivedError(view, errorCode, description, failingUrl);
        }
        @Override
        public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse){
            Logger.log("[Exception@H5App] pos=007&&message=onReceivedHttpError"+errorResponse.getReasonPhrase());
            super.onReceivedHttpError(view,request,errorResponse);
        }
        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error){
            Logger.log("[Exception@H5App] pos=008&&message=onReceivedError2"+error.toString());
            super.onReceivedError(view,request,error);
        }
        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error){
            Logger.log("[Exception@H5App] pos=009&&message=onReceivedHttpError"+error.toString());
            handler.proceed();
        }
    }
}
