package com.qp.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferenceUtil {
    private static final String SHAREDPREFERENCES_NAME = "my_sp";

    private static SharedPreferences getAppSp(Context ctx) {
        return ctx.getSharedPreferences(SHAREDPREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public static void setString(Context ctx,String key, String value) {
        getAppSp(ctx).edit().putString(key, value).commit();
    }

    public static String getString(Context ctx,String key) {
        return getAppSp(ctx).getString(key, "");
    }
}
