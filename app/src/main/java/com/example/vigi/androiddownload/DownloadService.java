package com.example.vigi.androiddownload;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.example.vigi.androiddownload.download.DownloadManager;
import com.example.vigi.androiddownload.download.DownloadRequest;

import java.io.File;

/**
 * Created by Vigi on 2016/1/25.
 */
public class DownloadService extends Service {
    private static final String TAG = DownloadService.class.getSimpleName();

    public static final String BUNDLE_ACTION = "action";
    public static final String BUNDLE_URL = "url";

    public static final int ACTION_NEW_DOWNLOAD_VIDEO = 0x0100;
    public static final int ACTION_PAUSE_VIDEOS = 0x0105;
    public static final int ACTION_DELETE_VIDEOS = 0x0106;
    public static final int ACTION_RESUME_DOWNLOAD_VIDEOS = 0x0108;
    public static final int ACTION_STOP_ALL = 0x0109;

    private DownloadManager mDownloadManager;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        mDownloadManager = new DownloadManager();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            stopSelf();
            return START_NOT_STICKY;
        }
        int action = intent.getIntExtra(BUNDLE_ACTION, -1);
        switch (action) {
            case ACTION_NEW_DOWNLOAD_VIDEO:
                String url = intent.getStringExtra(BUNDLE_URL);
                if (TextUtils.isEmpty(url)) {
                    return START_NOT_STICKY;
                }
                int guessEnd = url.lastIndexOf("?");
                String guessName = url.substring(url.lastIndexOf("/") + 1, guessEnd == -1 ? url.length() : guessEnd);
                File file = new File(getExternalFilesDir("download"), guessName);
                DownloadRequest downloadRequest = new DownloadRequest(url, file);
                downloadRequest.setTimeOut(10000);  // 10s to debug
                mDownloadManager.addDownload(downloadRequest);
                return START_STICKY;
            case ACTION_STOP_ALL:
                mDownloadManager.stop();
                return START_NOT_STICKY;
            case ACTION_PAUSE_VIDEOS:
            case ACTION_RESUME_DOWNLOAD_VIDEOS:
            default:
                Log.w(TAG, "UnHandle DownloadService command: " + action);
                break;
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        mDownloadManager.stop();
    }
}
