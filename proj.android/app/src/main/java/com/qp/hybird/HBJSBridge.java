package com.qp.hybird;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.webkit.JavascriptInterface;

import com.qp.utils.Logger;
import com.qp.utils.ToastUtils;

import org.json.JSONObject;

import java.util.HashMap;

public class HBJSBridge {
    private HashMap<Integer,HBPluginBase> rpMap = new HashMap<>();
    private HashMap<Integer,HBPluginBase> rrMap = new HashMap<>();
    private HBWebView mWebView = null;

    public HBJSBridge(HBWebView webView){
        mWebView = webView;
    }

    @JavascriptInterface
    public void callRouter(String reqStr, String cbName){
        String method = null;
        String _reqStr = null;
        try {
            JSONObject json = new JSONObject(reqStr);
            method = json.getString("Method");
            _reqStr = json.getString("Data");
        }catch (Exception e){
            method = "M_UnSupported";
            _reqStr = "";
        }
        HBPluginBase cr = HBPlugin.getInstance(cbName, method, _reqStr);
        cr.bindBridge(this);
        mWebView.getActivity().runOnUiThread(cr);
    }

    @JavascriptInterface
    public void trace(String str) {
        Logger.log("[Trace@JsBridge] "+str);
    }

    @JavascriptInterface
    public void exitApp(){
        Logger.log("[Trace@JsBridge] exitApp called.....");
        mWebView.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mWebView.getActivity().finish();
            }
        });
    }

    public boolean onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        HBPluginBase rp = rpMap.get(requestCode);
        if(null==rp){
            return false;
        }
        rpMap.remove(requestCode);
        rp.onRequestPermissionsResult(permissions,grantResults);
        return true;
    }

    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        HBPluginBase rr = rrMap.get(requestCode);
        if(null==rr){
            return false;
        }
        rrMap.remove(requestCode);
        rr.onActivityResult(resultCode,data);
        return true;
    }

    public static abstract class HBPluginBase implements Runnable{
        protected HBJSBridge bridge = null;
        protected String cbName = null;
        protected String reqStr = null;
        protected String errStr = null;
        protected String rspStr = null;

        public HBPluginBase(String cbName, String reqStr){
            this.cbName = cbName;
            this.reqStr = reqStr;
        }

        protected String[] requiredPermissions(){
            return null;
        }

        protected void bindBridge(HBJSBridge bridge){
            this.bridge = bridge;
        }

        @Override
        public void run() {
            String[] permissions = requiredPermissions();
            if(null==permissions
                    ||permissions.length==0
                    ||hasPermissions(permissions)){
                execute();
            }else{
                requestPermissions((hashCode()&0x0000FFFF),permissions);
            }
        }

        private boolean hasPermissions(String[] permissions){
            Activity activity = getActivity();
            if (Build.VERSION.SDK_INT >= 23) {
                //should be optimize by Easy permissions later
                int r = ContextCompat.checkSelfPermission(activity, permissions[0]);
                for(int i=1;i<permissions.length;i++){
                    r += ContextCompat.checkSelfPermission(activity, permissions[i]);
                }
                if (r != PackageManager.PERMISSION_GRANTED) {
                    return false;
                } else {
                    return true;
                }
            }
            return true;
        }

        private void requestPermissions(int requestCode, String[] permissions) {
            Logger.log("requestPermissions requestCode="+requestCode+",permissions size="+permissions.length);
            bridge.rpMap.put(requestCode,this);
            ActivityCompat.requestPermissions(getActivity(), permissions, requestCode);
        }

        protected void onRequestPermissionsResult(String[] permissions, int[] grantResults){
            int r = PackageManager.PERMISSION_GRANTED;
            for(int i=0;i<grantResults.length;i++){
                r += grantResults[0];
            }
            if(r!=PackageManager.PERMISSION_GRANTED) {
                ToastUtils.show("权限未允许");
                abort();
            }else {
                execute();
            }
        }

        protected void toOtherActivity(Intent intent, int requestCode, boolean bForResult){
            if(bForResult){
                bridge.rrMap.put(requestCode,this);
                getActivity().startActivityForResult(intent,requestCode);
            }else{
                getActivity().startActivity(intent);
            }
        }

        protected Activity getActivity(){
            return bridge.mWebView.getActivity();
        }

        protected void onActivityResult(int resultCode, Intent data){
            return;
        }

        protected abstract void execute();

        protected void abort(){
            errStr = "null"; //error occours
            rspStr = "{}"; //empty
            end();
        }

        protected void end(){
            if(Looper.getMainLooper()==Looper.myLooper()){
                runScript();
            }else {
                getActivity().runOnUiThread(new Runnable(){
                    @Override
                    public void run() {
                        runScript();
                    }
                });
            }
        }

        private void runScript() {
            bridge.mWebView.loadUrl("javascript:"+cbName+"('"+errStr+"','{\"result\":"+rspStr+"}')");
            bridge = null;
        }
    }
}
