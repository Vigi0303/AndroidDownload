package com.example.vigi.androiddownload;

import android.app.Application;

import com.orhanobut.logger.Logger;

/**
 * Created by Vigi on 2016/2/1.
 */
public class DownloadApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.init();
        TaskManager.getInstance().initLoadAccessors(this);
    }
}
