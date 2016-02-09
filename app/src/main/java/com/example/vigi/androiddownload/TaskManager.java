package com.example.vigi.androiddownload;

import android.util.SparseArray;

import com.example.vigi.androiddownload.core.DownloadRequest;

/**
 * A container of {@link TaskAccessor} and {@link DownloadRequest} in work queue.
 * <p/>
 * Created by Vigi on 2016/2/8.
 */
public class TaskManager {
    private static TaskManager ourInstance;

    public static TaskManager getInstance() {
        if (ourInstance == null) {
            synchronized (TaskManager.class) {
                if (ourInstance == null) {
                    ourInstance = new TaskManager();
                }
            }
        }
        return ourInstance;
    }

    private TaskManager() {
        mRequestInSession = new SparseArray<>();
        mAllTaskAccessor = new SparseArray<>();
        initLoadAccessors();
    }

    private SparseArray<DownloadRequest> mRequestInSession;
    private SparseArray<TaskAccessor> mAllTaskAccessor;

    private void initLoadAccessors() {
        // TODO: 2016/2/8 iterate files and store
    }

    public void addRequest(DownloadRequest request) {
        mRequestInSession.put(request.getOriginalUrl().hashCode(), request);
    }

    /**
     * @param taskId you should assign a unique ID, in you own condition.
     *               <p>we use hashCode here.</p>
     */
    public void cancel(int taskId) {
        DownloadRequest request = mRequestInSession.get(taskId);
        if (request != null) {
            request.cancel();
        }
        mRequestInSession.remove(taskId);
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
}
