package com.qp.uapp;

import com.qp.hybird.HBJSBridge.HBPluginBase;
import com.qp.hybird.HBPlugin.IPlugExtender;

public class HBExtender implements IPlugExtender {
    public static final String M_vedioChatInit = "vedioChatInit";
    public static final String M_vedioChatUint = "vedioChatUint";
    public static final String M_vedioChatWaiting = "vedioChatWaiting";
    public static final String M_vedioChatGiveup = "vedioChatGiveup";
    public static final String M_vedioChatCall2 = "vedioChatCall2";
    public static final String M_invokeEShare = "invokeEShare";
    public static final String M_invokeEFenQi = "invokeEFenQi";
    public static final String M_invokeEFenQiVisaSDK = "invokeEFenQiVisaSDK";
    public static final String M_invokeGxbao = "invokeGxbao";

    @Override
    public HBPluginBase getInstance(String cbName, String method, String reqStr) {
        if(method.equals(M_invokeEShare)){
            return new M_InvokeEShare(cbName,reqStr);
        }
        return null;
    }

    protected static class M_InvokeEShare extends HBPluginBase {

        public M_InvokeEShare(String cbName, String reqStr) {
            super(cbName, reqStr);
        }

        @Override
        public void execute(){
            abort();
        }
    }
}
