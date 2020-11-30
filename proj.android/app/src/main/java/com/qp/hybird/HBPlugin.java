package com.qp.hybird;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import com.qp.utils.Logger;
import com.qp.utils.ToastUtils;
import com.qp.hybird.HBJSBridge.HBPluginBase;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;
import com.zhihu.matisse.ui.MatisseActivity;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public abstract class HBPlugin {
    public static final String M_getDeviceInfo = "getDeviceInfo";
    public static final String M_imageChoose  = "imageChoose";
    public static final String M_fileUpload    = "fileUpload";
    public static final String M_localStorerage    = "localStorerage";
    public static final String M_getLocation  = "getLocation";
    public static final String M_imagesToPdf = "imagesToPdf";
    public static final String M_getImageLocation = "getImageLocation";
    public static final String M_contractPermission = "contractPermission";
    public static final String M_clearCache = "clearCache";
    public static final String M_videoChoose = "videoChoose";
    public static final String M_networkStatus = "networkStatus";
    public static final String M_setCookie = "setCookie";
    public static final String M_mediaChoose = "mediaChoose";
    public static final String M_aliasPushId = "aliasPushId";
    public static final String M_offlineUsrId = "offlineUsrId";
    public static final String M_downloadAndShare = "downloadAndShare";
    public static final String M_mapNav = "mapNav";
    public static final String M_img2Album = "img2Album";
    public static final String M_setStartImg = "setStartImg";
    public static final String M_cacheSize = "cacheSize";

    public static HBPluginBase getInstance(String cbName, String method, String reqStr){
        if(method.equals(M_imageChoose)){
            return new M_ImageChoose(cbName,reqStr);
        }
        if(method.equals(M_mediaChoose)){
            return new M_MediaChoose(cbName,reqStr);
        }
        for(IPlugExtender extender : extenderList){
            HBPluginBase plug = extender.getInstance(cbName,method,reqStr);
            if(null!=plug){
                return plug;
            }
        }
        return new M_UnSupported(cbName,reqStr);
    }

    private static List<IPlugExtender> extenderList = new LinkedList<>();
    public static void registerExtender(IPlugExtender extender){
        extenderList.add(extender);
    }

    public interface IPlugExtender{
        HBPluginBase getInstance(String cbName, String method, String reqStr);
    }

    protected static class M_ImageChoose extends HBPluginBase {

        public M_ImageChoose(String cbName, String reqStr) {
            super(cbName, reqStr);
        }

        public boolean openGelary(int maxNumber) {
            try {
                Set<MimeType> mimeTypes = null;
                mimeTypes = MimeType.of(MimeType.JPEG,MimeType.PNG,MimeType.MP4,MimeType.THREEGPP);
                Matisse.from(getActivity())
                        .choose(mimeTypes)
                        .maxSelectablePerMediaType(maxNumber,1)
                        .countable(false)
                        .showSingleMediaType(true)
                        .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                        .thumbnailScale(0.85F)
                        .imageEngine(new GlideEngine());
                Intent intent = new Intent(getActivity(),MatisseActivity.class);
                toOtherActivity(intent,321,true);
            }catch (Exception e){
                return false;
            }
            return true;
        }

        @Override
        public void execute() {
            if(!openGelary(8)){
                abort();
            }
        }

        @Override
        protected void onActivityResult(int resultCode, Intent data){
            ToastUtils.show("You are Back now ~ IMG!");
            end();
        }
    }

    protected static class M_MediaChoose extends HBPluginBase {
        private String mCurrentFilePath = null;

        public M_MediaChoose(String cbName, String reqStr) {
            super(cbName, reqStr);
        }

        private File createMeidaFile(Activity activity, String suffix) throws IOException {
            String cameraPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath() + "/Camera/";
            File storageDir = new File(cameraPath);
            if (!storageDir.exists()) {
                storageDir.mkdirs();
            }
            storageDir = new File(cameraPath, System.currentTimeMillis() + suffix);
            mCurrentFilePath = storageDir.getAbsolutePath();
            return storageDir;
        }

        private boolean takePicture(){
            Logger.log("[Trace@FileChooser] enter tack picture");
            boolean r = true;
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (Build.VERSION.SDK_INT<Build.VERSION_CODES.N){
                Uri imageUri = null;
                try {
                    imageUri = Uri.fromFile(createMeidaFile(getActivity(),".jpg"));
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    toOtherActivity(intent, 215, true);
                } catch (Exception e) {
                    Logger.log("[Trace@FileChooser] exception ....."+e.getMessage());
                    r = false;
                }
            }else {
                try {
                    ContentValues contentValues = new ContentValues(1);
                    contentValues.put(MediaStore.Images.Media.DATA, createMeidaFile(getActivity(),".jpg").getAbsolutePath());
                    Uri uri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                    toOtherActivity(intent, 215, true);
                }catch (Exception e) {
                    Logger.log("[Trace@FileChooser] exception ....."+e.getMessage());
                    r = false;
                }
            }
            return r;
        }

        @Override
        protected void onActivityResult(int resultCode, Intent data){
            ToastUtils.show("You are Back now!");
            mCurrentFilePath = null;
            end();
        }

        @Override
        public void execute() {
            if(!takePicture()){
                abort();
            }
        }

        @Override
        public String[] requiredPermissions(){
            return new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA,Manifest.permission.ACCESS_COARSE_LOCATION};
        }
    }

    protected static class M_UnSupported extends HBPluginBase {

        public M_UnSupported(String cbName, String reqStr) {
            super(cbName, reqStr);
        }

        @Override
        public void execute(){
            abort();
        }
    }
}
