package com.example.vigi.androiddownload;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

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
    public static final String BUNDLE_TASK_ID = "taskId";

    public static final int ACTION_NEW_TASK = 0x0101;
    public static final int ACTION_DELETE_TASK = 0x0103;
    public static final int ACTION_RESUME_TASK = 0x0104;
    public static final int ACTION_STOP_ALL = 0x0105;

    private DownloadManager mDownloadManager;
    private Handler mUIHandler;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        mDownloadManager = new DownloadManager();
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
            case ACTION_NEW_TASK: {
                String url = intent.getStringExtra(BUNDLE_URL);
                if (TextUtils.isEmpty(url)) {
                    return START_NOT_STICKY;
                }
                addNewTask(url);
                break;
            }
            case ACTION_RESUME_TASK: {
                int taskId = intent.getIntExtra(BUNDLE_TASK_ID, -1);
                if (taskId == -1) {
                    return START_NOT_STICKY;
                }
                TaskAccessor task = TasksHolder.getInstance().getAccessor(taskId);
                if (task == null) {
                    return START_NOT_STICKY;
                }
                if (task.status == TaskAccessor.FINISH
                        || task.status == TaskAccessor.DOWNLOADING) {
                    return START_NOT_STICKY;
                }
                resumeTask(task);
                break;
            }
            case ACTION_STOP_ALL: {
                mDownloadManager.stop();
                break;
            }
            case ACTION_DELETE_TASK: {
                int taskId = intent.getIntExtra(BUNDLE_TASK_ID, -1);
                if (taskId == -1) {
                    return START_NOT_STICKY;
                }
                TaskAccessor task = TasksHolder.getInstance().getAccessor(taskId);
                if (task == null) {
                    return START_NOT_STICKY;
                }
                deleteTask(task);
                break;
            }
            default:
                Log.w(TAG, "UnHandle DownloadService command: " + action);
                break;
        }
        return START_STICKY;
    }

    private void addNewTask(String url) {
        int guessEnd = url.lastIndexOf("?");
        String guessName = url.substring(url.lastIndexOf("/") + 1, guessEnd == -1 ? url.length() : guessEnd);
        int urlHash = url.hashCode();
        String hashStr = String.valueOf(urlHash);
        File targetFile = new File(getExternalFilesDir(TasksHolder.DOWNLOAD_DIR + File.separator + hashStr), guessName);
        File infoJsonFile = new File(getExternalFilesDir(TasksHolder.DOWNLOAD_DIR + File.separator + hashStr), TasksHolder.INFO_FILE_NAME);
        TaskAccessor task = new TaskAccessor(infoJsonFile, urlHash);
        DownloadRequest request = new DownloadRequest(url, targetFile, 0);
        request.setTimeOut(20000);
        request.addRequestListener(new RequestListenerImpl(task, request));
        TasksHolder.getInstance().addRequest(request, task);
        mDownloadManager.addDownload(request);
    }

    private void resumeTask(TaskAccessor task) {
        String hashStr = String.valueOf(task.info.url.hashCode());
        File targetFile = new File(getExternalFilesDir(TasksHolder.DOWNLOAD_DIR + File.separator + hashStr), task.info.fileName);
        // continue from last position if we have
        long downloadedSize = 0;
        if (targetFile.exists()) {
            if (task.info.totalSize == targetFile.length()) {
                downloadedSize = task.info.downloadedSize;
            }
        }
        DownloadRequest request = new DownloadRequest(
                task.info.url, targetFile, downloadedSize
        );
        request.setTimeOut(20000);
        request.addRequestListener(new RequestListenerImpl(task, request));
        mDownloadManager.addDownload(request);
        TasksHolder.getInstance().addRequest(request, task);
    }

    private void deleteTask(TaskAccessor task) {
        final int taskId = task.info.id;
        DownloadRequest request = TasksHolder.getInstance().getRequest(taskId);
        if (request != null) {
            request.clearRequestListeners();
            if (!request.isCancel()) {
                TasksHolder.getInstance().cancel(taskId);
                request.addRequestListener(new SimpleRequestListener() {
                    @Override
                    public void onFinish(DownloadResult result) {
                        deleteTaskInternal(taskId);
                    }

                    @Override
                    public void onCanceled() {
                        deleteTaskInternal(taskId);
                    }
                });
            } else {
                deleteTaskInternal(taskId);
            }
        } else {
            deleteTaskInternal(taskId);
        }
    }

    private void deleteTaskInternal(int taskId) {
        TasksHolder.getInstance().removeRequest(taskId);
        TasksHolder.getInstance().removeAccessor(taskId);
    }

    private void postEventOnMainThread(Object event) {
        mUIHandler.obtainMessage(0, event).sendToTarget();
    }

    @Override
    public void onDestroy() {
        mDownloadManager.stop();
    }

    class RequestListenerImpl implements DownloadRequest.RequestListener {
        private final TaskAccessor mTask;
        private final DownloadRequest mDownloadRequest;

        RequestListenerImpl(TaskAccessor task, DownloadRequest downloadRequest) {
            mTask = task;
            mDownloadRequest = downloadRequest;
        }

        @Override
        public void onCreate() {
            mTask.status = TaskAccessor.WAIT;
            mTask.info.id = mDownloadRequest.getOriginalUrl().hashCode();
            mTask.info.url = mDownloadRequest.getOriginalUrl();
            mTask.info.fileName = mDownloadRequest.getTargetFile().getName();
            mTask.info.downloadedSize = mDownloadRequest.getCurrentBytes();
            if (mTask.info.createTime == 0) {
                mTask.info.createTime = System.currentTimeMillis();
            }
            mTask.info.title = mDownloadRequest.getOriginalUrl();
            mTask.info.isCompleted = false;
            mTask.syncInfoFile();
            postEventOnMainThread(new DownloadEvent.Create(mDownloadRequest));
        }

        @Override
        public void onDispatched() {
            mTask.status = TaskAccessor.PROCESSING;
            postEventOnMainThread(new DownloadEvent.DisPatched(mTask.info.id));
        }

        @Override
        public void onReadLength(long totalBytes) {
            mTask.info.totalSize = totalBytes;
            mTask.syncInfoFile();
            postEventOnMainThread(new DownloadEvent.ReadLength(mTask.info.id, totalBytes));
        }

        @Override
        public void onLoading(long downloadedBytes) {
            if (!mDownloadRequest.isCancel()) {
                mTask.status = TaskAccessor.DOWNLOADING;
            }
            mTask.info.downloadedSize = downloadedBytes;
//            mTask.syncInfoFile();          // this action is invoked too often and may cause IO crowding
            postEventOnMainThread(new DownloadEvent.Loading(mTask.info.id, downloadedBytes));
        }

        @Override
        public void onFinish(DownloadResult result) {
            if (result.isSuccess()) {
                mTask.status = TaskAccessor.FINISH;
                mTask.info.isCompleted = true;
                mTask.info.finishTime = System.currentTimeMillis();
            } else {
                mTask.status = TaskAccessor.ERROR;
            }
            mTask.syncInfoFile();
            TasksHolder.getInstance().removeRequest(mTask.info.id);
            postEventOnMainThread(new DownloadEvent.Finish(mTask.info.id, result));
        }

        @Override
        public void onCanceled() {
            mTask.status = TaskAccessor.IDLE;
            mTask.syncInfoFile();
            TasksHolder.getInstance().removeRequest(mTask.info.id);
            postEventOnMainThread(new DownloadEvent.Canceled(mTask.info.id));
        }
    }

    abstract class SimpleRequestListener implements DownloadRequest.RequestListener {

        @Override
        public void onCreate() {

        }

        @Override
        public void onDispatched() {

        }

        @Override
        public void onReadLength(long totalBytes) {

        }

        @Override
        public void onLoading(long downloadedBytes) {

        }

        @Override
        public void onFinish(DownloadResult result) {

        }

        @Override
        public void onCanceled() {

        }
    }
}
