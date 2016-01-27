package com.example.vigi.androiddownload.download;

import android.os.Looper;
import android.util.Log;

import com.example.vigi.androiddownload.download.base.NetWorkDelivery;

/**
 * Created by Vigi on 2016/1/27.
 */
public class DownloadDelivery extends NetWorkDelivery {

    public DownloadDelivery(Looper looper) {
        super(looper);
    }

    public void postLoading(DownloadRequest request, long downloadedBytes) {
        mHandler.post(new DownloadDeliveryRunnable(request, downloadedBytes));
    }

    class DownloadDeliveryRunnable implements Runnable {
        private DownloadRequest mRequest;
        private long mDownloadedBytes;

        public DownloadDeliveryRunnable(DownloadRequest request, long downloadedBytes) {
            mRequest = request;
            mDownloadedBytes = downloadedBytes;
        }

        @Override
        public void run() {
            Log.d("debug", "request(" + mRequest.getOriginalUrl() + ")onLoading");
            mRequest.onLoading(mDownloadedBytes);
        }
    }
}
