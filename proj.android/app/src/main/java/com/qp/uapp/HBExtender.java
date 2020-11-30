package com.qp.uapp;

import com.qp.hybird.HBJSBridge;
import com.qp.hybird.HBPlugin.IPlugExtender;

public class HBExtender implements IPlugExtender {
    @Override
    public HBJSBridge.HBPluginBase getInstance(String cbName, String method, String reqStr) {
        return null;
    }
}
