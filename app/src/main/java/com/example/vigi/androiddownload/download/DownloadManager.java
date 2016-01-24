package com.example.vigi.androiddownload.download;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Vigi on 2016/1/20.
 */
public class DownloadManager {
    private static AtomicInteger SequenceGenerator = new AtomicInteger(0);
    private HttpPerformer mHttpPerformer;
    private DownloadWorker mWorker;
    private BlockingQueue<DownloadRequest> mRequestQueue;
    private DownloadListener mDownloadListener;

    public DownloadManager() {
        this(null, null);
    }

    public DownloadManager(HttpPerformer httpPerformer, DownloadListener downloadListener) {
        if (httpPerformer == null) {
            mHttpPerformer = new HttpUrlPerformer();
        } else {
            mHttpPerformer = httpPerformer;
        }
        mDownloadListener = downloadListener;
        mRequestQueue = new PriorityBlockingQueue<>();
    }

    public void addDownload(DownloadRequest downloadRequest) {
        if (mWorker == null) {
            start();
        }
        downloadRequest.setSequence(SequenceGenerator.incrementAndGet());
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
