package com.example.vigi.androiddownload;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.util.IOUtils;
import com.example.vigi.androiddownload.core.DownloadDelivery;
import com.example.vigi.androiddownload.core.DownloadManager;
import com.example.vigi.androiddownload.core.DownloadRequest;
import com.example.vigi.androiddownload.core.DownloadResult;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by Vigi on 2016/1/25.
 */
public class DownloadService extends Service {
    private static final String TAG = DownloadService.class.getSimpleName();

    public static final String BUNDLE_ACTION = "action";
    public static final String BUNDLE_URL = "url";

    public static final int ACTION_NEW_DOWNLOAD_VIDEO = 0x0101;
    public static final int ACTION_PAUSE_VIDEOS = 0x0102;
    public static final int ACTION_DELETE_VIDEOS = 0x0103;
    public static final int ACTION_RESUME_DOWNLOAD_VIDEOS = 0x0104;
    public static final int ACTION_STOP_ALL = 0x0105;

    private HandlerThread mDAOThread;
    private DownloadManager mDownloadManager;
    private Handler mUIHandler;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        mDAOThread = new HandlerThread("mDAOThread", Process.THREAD_PRIORITY_BACKGROUND);
        mDAOThread.start();
        mDownloadManager = new DownloadManager(null, new DownloadDelivery(mDAOThread.getLooper()));
        mUIHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.obj != null) {
                    EventBus.getInstance().postEvent(msg.obj);
                    return true;
                }
                return false;
            }
        });
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
                addNewTask(url);
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

    private void addNewTask(String url) {
        int guessEnd = url.lastIndexOf("?");
        String guessName = url.substring(url.lastIndexOf("/") + 1, guessEnd == -1 ? url.length() : guessEnd);
        String hashStr = String.valueOf(url.hashCode());
        File file = new File(getExternalFilesDir("download/" + hashStr), guessName);
        DownloadRequest downloadRequest = new DownloadRequestImpl(url, file, lookupInfoFile(file));
        downloadRequest.setTimeOut(10000);  // 10s to debug
        mDownloadManager.addDownload(downloadRequest);
    }

    private long lookupInfoFile(File targetFile) {
        // continue from last position if we have
        File infoJsonFile = new File(targetFile.getParent(), "info.json");
        long downloadedSize = 0;
        DownloadTask task = generateBean(infoJsonFile);
        if (task != null && targetFile.exists()) {
            if (task.totalSize == targetFile.length()) {
                downloadedSize = task.downloadedSize;
            }
        }
        return downloadedSize;
    }

    private DownloadTask generateBean(File infoJsonFile) {
        if (infoJsonFile.exists()) {
            FileReader reader = null;
            try {
                reader = new FileReader(infoJsonFile);
                StringBuilder sb = new StringBuilder();
                char[] buffer = new char[512];
                int len;
                while ((len = reader.read(buffer)) != -1) {
                    sb.append(buffer, 0, len);
                }
                return JSON.parseObject(sb.toString(), DownloadTask.class);
            } catch (java.io.IOException e) {
                Log.e("vigi", "read failed", e);
            } finally {
                IOUtils.close(reader);
            }
        }
        return null;
    }

    private void postEventOnMainThread(Object event) {
        mUIHandler.obtainMessage(0, event).sendToTarget();
    }

    @Override
    public void onDestroy() {
        mDAOThread.quit();
        mDownloadManager.stop();
    }

    class DownloadRequestImpl extends DownloadRequest {
        public DownloadRequestImpl(@NonNull String urlStr, @NonNull File file, long startPos) {
            super(urlStr, file, startPos);
        }

        @Override
        protected void onCreate() {
            File targetFile = getTargetFile();
            File infoJsonFile = new File(targetFile.getParent(), "info.json");
            DownloadTask task = generateBean(infoJsonFile);
            if (task == null) {
                task = new DownloadTask();
                task.url = getOriginalUrl();
                task.downloadedSize = getCurrentBytes();
                task.status = DownloadTask.WAIT;
                task.createTime = System.currentTimeMillis();
                task.title = task.url;
            }

            syncTaskWriter(task);
        }

        private boolean syncTaskWriter(DownloadTask task) {
            File targetFile = getTargetFile();
            File infoJsonFile = new File(targetFile.getParent(), "info.json");
            FileWriter writer = null;
            try {
                writer = new FileWriter(infoJsonFile, false);
                writer.write(JSON.toJSONString(task));
            } catch (IOException e) {
                cancel();
                Log.e("vigi", "write failed", e);
            } finally {
                IOUtils.close(writer);
            }
        }

        @Override
        protected void onDispatched() {
            postEventOnMainThread(new DownloadEvent.DisPatched(this));
        }

        @Override
        protected void onReadLength(long totalBytes) {
            postEventOnMainThread(new DownloadEvent.ReadLength(this, totalBytes));
        }

        @Override
        protected void onFinish(DownloadResult result) {
            postEventOnMainThread(new DownloadEvent.Finish(this, result));
        }

        @Override
        protected void onLoading(long downloadedBytes) {
            postEventOnMainThread(new DownloadEvent.Loading(this, downloadedBytes));
        }

        @Override
        protected void onCanceled() {
            postEventOnMainThread(new DownloadEvent.Canceled(this));
        }
    }
}
