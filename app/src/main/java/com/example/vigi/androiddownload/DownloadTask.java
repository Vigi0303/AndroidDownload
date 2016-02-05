package com.example.vigi.androiddownload;

/**
 * Created by Vigi on 2016/1/18.
 */
public class DownloadTask {
    public static final int WAIT = 0;
    public static final int DOWNLOADING = 1;
    public static final int PAUSE = 2;
    public static final int ERROR = 3;
    public static final int FINISH = 4;
    public static final int PROCESSING = 5;   // disable status to keep it atomic

    public String url;
    public String title;
    public long totalSize;
    public long downloadedSize;
    public int status;
    public long createTime;
    public long finishTime;
    public String fileName;
}
