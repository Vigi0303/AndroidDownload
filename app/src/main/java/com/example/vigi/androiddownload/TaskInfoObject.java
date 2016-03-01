package com.example.vigi.androiddownload;

/**
 * Created by Vigi on 2016/2/6.
 */
public class TaskInfoObject {
    /**
     * you should assign a unique ID, in you own condition.
     * <p>we use hashCode here.</p>
     */
    public int id;
    public String url;
    public String title;
    public long totalSize;
    public long downloadedSize;
    public boolean isCompleted;
    public long createTime;
    public long finishTime;
    public String fileName;

    public TaskInfoObject() {
        id = 0;
        isCompleted = false;
    }

    public void copyFrom(TaskInfoObject source) {
        if (source == this) {
            return;
        }
        id = source.id;
        url = source.url;
        title = source.title;
        totalSize = source.totalSize;
        downloadedSize = source.downloadedSize;
        isCompleted = source.isCompleted;
        createTime = source.createTime;
        finishTime = source.finishTime;
        fileName = source.fileName;
    }
}
