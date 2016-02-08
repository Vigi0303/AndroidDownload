package com.example.vigi.androiddownload.core;

import android.os.Looper;

import java.net.HttpURLConnection;
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
    private DownloadDispatcher mDispatcher;
    private BlockingQueue<DownloadRequest> mRequestQueue;
    private DownloadDelivery mDelivery;

    public DownloadManager() {
        this(null, null);
    }

    /**
     * @param netWorkPerformer perform network, default by {@link HttpURLConnection}
     * @param delivery         delivery {@link DownloadRequest#onLoading(long)}, {@link DownloadRequest#onFinish(DownloadResult)} and etc.
     *                         you can assign a {@link Looper} to {@link DownloadDelivery}
     */
    public DownloadManager(NetWorkPerformer netWorkPerformer, DownloadDelivery delivery) {
        if (netWorkPerformer == null) {
            mNetWorkPerformer = new UrlConnectionPerformer(DEFAULT_USER_AGENT);
        } else {
            mNetWorkPerformer = netWorkPerformer;
        }
        if (delivery == null) {
            mDelivery = new DownloadDelivery(Looper.getMainLooper());
        } else {
            mDelivery = delivery;
        }
        mRequestQueue = new PriorityBlockingQueue<>();
    }

    public void addDownload(DownloadRequest downloadRequest) {
        mDelivery.postCreate(downloadRequest);
        if (mDispatcher == null) {
            start();
        }
        downloadRequest.setSequence(SequenceGenerator.incrementAndGet());
        mRequestQueue.add(downloadRequest);
    }

    public void start() {
        stop();

        mDispatcher = new DownloadDispatcher(mRequestQueue, mNetWorkPerformer, mDelivery);
        mDispatcher.start();
    }

    public void stop() {
        if (mDispatcher != null) {
            mDispatcher.quit();
            mDispatcher = null;
        }
    }
}
