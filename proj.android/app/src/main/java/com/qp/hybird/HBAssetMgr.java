package com.qp.hybird;

public class HBAssetMgr{
    public static native void assetMgrSetConfig(String szUrl);
    public static native void assetMgrUpdate();
    public static native int assetMgrGetTotalCount();
    public static native int assetMgrGetCurrCount();
    public static native int assetMgrGetStatus();
    public static native void assetMgrRemoveALL();
    public static native void assetMgrClear();
    public static native long assetMgrVersionCode();
}
