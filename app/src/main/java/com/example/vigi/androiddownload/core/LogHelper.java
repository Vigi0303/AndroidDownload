package com.example.vigi.androiddownload.core;

import android.util.Log;

/**
 * Created by Vigi on 2016/2/22.
 */
public class LogHelper {
    private static final String GLOBAL_TAG = "vigi";

    public static void logError(String msg) {
        Log.e(GLOBAL_TAG, msg);
    }

    public static void logError(String msg, Throwable tr) {
        Log.e(GLOBAL_TAG, msg, tr);
    }
}
