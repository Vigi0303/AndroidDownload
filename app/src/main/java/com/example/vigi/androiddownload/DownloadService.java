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

import com.example.vigi.androiddownload.core.DownloadDelivery;
import com.example.vigi.androiddownload.core.DownloadManager;
import com.example.vigi.androiddownload.core.DownloadRequest;
import com.example.vigi.androiddownload.core.DownloadResult;

import java.io.File;

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
        TaskAccessor task = new TaskAccessor(infoJsonFile);
        if (targetFile.exists()) {
            if (task.info.totalSize == targetFile.length()) {
                downloadedSize = task.info.downloadedSize;
            }
        }
        return downloadedSize;
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
        private final TaskAccessor mTask;

        public DownloadRequestImpl(@NonNull String urlStr, @NonNull File file, long startPos) {
            super(urlStr, file, startPos);
            mTask = new TaskAccessor(new File(file.getParent(), "info.json"));
        }

        @Override
        protected void onCreate() {
            mTask.status = TaskAccessor.WAIT;
            mTask.info.url = getOriginalUrl();
            mTask.info.downloadedSize = getCurrentBytes();
            if (mTask.info.createTime == 0) {
                mTask.info.createTime = System.currentTimeMillis();
            }
            mTask.info.title = getOriginalUrl();
            mTask.info.isCompleted = false;
            if (!mTask.syncInfoFile()) {
                cancel();
            }
            postEventOnMainThread(new DownloadEvent.Create(this));
        }

        @Override
        protected void onDispatched() {
            mTask.status = TaskAccessor.PROCESSING;
            postEventOnMainThread(new DownloadEvent.DisPatched(this));
        }

        @Override
        protected void onReadLength(long totalBytes) {
            mTask.info.totalSize = totalBytes;
            postEventOnMainThread(new DownloadEvent.ReadLength(this, totalBytes));
        }

        @Override
        protected void onLoading(long downloadedBytes) {
            mTask.status = TaskAccessor.DOWNLOADING;
            mTask.info.downloadedSize = downloadedBytes;
//            mTask.syncInfoFile();          // this action is invoked too often and may cause IO crowding
            postEventOnMainThread(new DownloadEvent.Loading(this, downloadedBytes));
        }

        @Override
        protected void onFinish(DownloadResult result) {
            if (result.isSuccess()) {
                mTask.status = TaskAccessor.FINISH;
                mTask.info.isCompleted = true;
                mTask.info.finishTime = System.currentTimeMillis();
            } else {
                mTask.status = TaskAccessor.ERROR;
            }
            mTask.syncInfoFile();
            postEventOnMainThread(new DownloadEvent.Finish(this, result));
        }

        @Override
        protected void onCanceled() {
            mTask.status = TaskAccessor.PAUSE;
            mTask.syncInfoFile();
            postEventOnMainThread(new DownloadEvent.Canceled(this));
        }
    }
}
