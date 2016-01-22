package com.example.vigi.androiddownload.download;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Created by Vigi on 2016/1/20.
 */
public class WorkerManager {
    private BlockingQueue<DownloadRequest> mRequestQueue;
    private DownloadWorker mWorker;
    private HttpPerformer mHttpPerformer;

    public WorkerManager() {
        this(null);
    }

    public WorkerManager(HttpPerformer httpPerformer) {
        if (httpPerformer == null) {
            mHttpPerformer = new HttpUrlPerformer();
        } else {
            mHttpPerformer = httpPerformer;
        }
        mRequestQueue = new PriorityBlockingQueue<>();
    }

    public void addDownload(DownloadRequest downloadRequest) {
        if (mWorker == null) {
            start();
        }
        mRequestQueue.add(downloadRequest);
    }

    public void start() {
        stop();

        mWorker = new DownloadWorker(mRequestQueue, mHttpPerformer);
        mWorker.start();
    }

    public void stop() {
        if (mWorker != null) {
            mWorker.quit();
            mWorker = null;
        }
    }
}
