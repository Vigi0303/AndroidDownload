package com.example.vigi.androiddownload.download;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Vigi on 2016/1/20.
 */
public class DownloadManager {
    private static final String DEFAULT_USER_AGENT = "AndroidDownload / 1.0";   // TODO: 2016/1/26 to be confirmed
    private static AtomicInteger SequenceGenerator = new AtomicInteger(0);
    private NetWorkPerformer mNetWorkPerformer;
    private DownloadWorker mWorker;
    private BlockingQueue<DownloadRequest> mRequestQueue;
    private String mUserAgent;

    public DownloadManager() {
        this(null);
    }

    public DownloadManager(NetWorkPerformer netWorkPerformer) {
        if (netWorkPerformer == null) {
            mNetWorkPerformer = new UrlConnectionPerformer(DEFAULT_USER_AGENT);
        } else {
            mNetWorkPerformer = netWorkPerformer;
        }
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

        mWorker = new DownloadWorker(mRequestQueue, mNetWorkPerformer);
        mWorker.start();
    }

    public void stop() {
        if (mWorker != null) {
            mWorker.quit();
            mWorker = null;
        }
    }
}
