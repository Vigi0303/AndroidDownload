package com.example.vigi.androiddownload;

import android.app.Application;

import com.orhanobut.logger.Logger;

/**
 * Created by Vigi on 2016/2/1.
 */
public class DownloadApplication extends Application {
    private static DownloadApplication mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        Logger.init();
    }

    public static DownloadApplication getInstance() {
        return mInstance;
    }
}
