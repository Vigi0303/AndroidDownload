package com.example.vigi.androiddownload;

import android.app.Application;
import android.content.Context;
import android.util.SparseArray;

import com.example.vigi.androiddownload.core.DownloadRequest;
import com.example.vigi.androiddownload.core.LogHelper;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * A container of {@link TaskAccessor} and {@link DownloadRequest} in work queue.
 * <p>must initialize by {@link #initLoadAccessors(Context)} in {@link Application#onCreate()}</p>
 * <p/>
 * Created by Vigi on 2016/2/8.
 */
public class TasksHolder {
    public static final String DOWNLOAD_DIR = "download";
    public static final String INFO_FILE_NAME = "info.json";

    private static TasksHolder ourInstance;

    public static TasksHolder getInstance() {
        if (ourInstance == null) {
            synchronized (TasksHolder.class) {
                if (ourInstance == null) {
                    ourInstance = new TasksHolder();
                }
            }
        }
        return ourInstance;
    }

    private TasksHolder() {
        mRequestInSession = new SparseArray<>();
        mAllTaskAccessor = new SparseArray<>();
    }

    private SparseArray<DownloadRequest> mRequestInSession;
    private SparseArray<TaskAccessor> mAllTaskAccessor;

    /**
     * must initialize in {@link Application#onCreate()}
     *
     * @param context the context for downloading
     */
    public void initLoadAccessors(Context context) {
        File rootDir = context.getExternalFilesDir(DOWNLOAD_DIR);
        if (rootDir == null) {
            throw new NullPointerException("can not find external storage");
        }
        File[] dirs = rootDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        });
        if (dirs == null) {
            return;
        }
        for (File dir : dirs) {
            File infoFile = new File(dir, INFO_FILE_NAME);
            if (!infoFile.exists())
                continue;
            TaskAccessor task = new TaskAccessor(infoFile);
            if (task.validateInfoJson()) {
                mAllTaskAccessor.put(task.info.id, task);
            }
        }
    }

    public void addRequest(DownloadRequest request, TaskAccessor task) {
        DownloadRequest oldRequest = mRequestInSession.get(task.info.id);
        if (oldRequest != null) {
            LogHelper.logError("task: " + task.info.id + " has already in task queue");
            return;
        }
        mRequestInSession.put(task.info.id, request);
        mAllTaskAccessor.put(task.info.id, task);
    }

    public void cancel(int taskId) {
        DownloadRequest request = mRequestInSession.get(taskId);
        if (request != null) {
            request.cancel();
        }
        mRequestInSession.remove(taskId);
        TaskAccessor task = mAllTaskAccessor.get(taskId);
        if (task != null) {
            task.status = TaskAccessor.DISABLED;
        } else {
            LogHelper.logError("should not be here");
        }
    }

    public TaskAccessor getAccessor(int taskId) {
        return mAllTaskAccessor.get(taskId);
    }

    public DownloadRequest getRequest(int taskId) {
        return mRequestInSession.get(taskId);
    }

    public void removeRequest(int taskId) {
        mRequestInSession.remove(taskId);
    }

    public List<TaskAccessor> getAllTaskAccessor() {
        List<TaskAccessor> result = new ArrayList<>();
        for (int i = 0; i < mAllTaskAccessor.size(); ++i) {
            TaskAccessor task = mAllTaskAccessor.valueAt(i);
            result.add(task);
        }
        return result;
    }
}
