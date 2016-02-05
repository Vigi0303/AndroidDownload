package com.example.vigi.androiddownload.download;

import android.os.Process;

import java.util.concurrent.BlockingQueue;

/**
 * Created by Vigi on 2016/2/2.
 */
public class DownloadDispatcher extends Thread {
    private BlockingQueue<DownloadRequest> mRequestQueue;
    private NetWorkPerformer mNetWorkPerformer;
    private DownloadDelivery mDelivery;
    private volatile boolean mQuit = false;   // TODO: 2016/1/24 why volatile

    public DownloadDispatcher(BlockingQueue<DownloadRequest> requestQueue
            , NetWorkPerformer netWorkPerformer, DownloadDelivery delivery) {
        super("DownloadDispatcher");
        mRequestQueue = requestQueue;
        mNetWorkPerformer = netWorkPerformer;
        mDelivery = delivery;
    }

    @Override
    public void run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        while (true) {
            try {
                DownloadRequest downloadRequest = mRequestQueue.take();

                mDelivery.postDispatched(downloadRequest);
                DownloadWorker worker = new DownloadWorker(mNetWorkPerformer, mDelivery, downloadRequest);
                DownloadResult result = worker.work();
                if (downloadRequest.isCancel()) {
                    mDelivery.postCanceled(downloadRequest);
                    continue;
                }
                mDelivery.postFinish(downloadRequest, result);
            } catch (InterruptedException e) {
                if (mQuit) {
                    return;
                }
            }
        }
    }

    public void quit() {
        mQuit = true;
        interrupt();
    }
}
