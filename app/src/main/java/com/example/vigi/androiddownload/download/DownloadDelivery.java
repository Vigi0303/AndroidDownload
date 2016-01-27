package com.example.vigi.androiddownload.download;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

/**
 * Created by Vigi on 2016/1/27.
 */
public class DownloadDelivery {
    private static final int MSG_DISPATCHED = 1;
    private static final int MSG_FINISH = 2;
    private static final int MSG_LOADING = 3;
    protected Handler mHandler;

    public DownloadDelivery(Looper looper) {
        mHandler = new Handler(looper == null ? Looper.getMainLooper() : looper);
    }

    public void postDispatched(DownloadRequest request) {
        mHandler.post(new DownloadDeliveryRunnable(request, null, 0, MSG_DISPATCHED));
    }

    public void postFinish(DownloadRequest request, NetWorkResponse response) {
        mHandler.post(new DownloadDeliveryRunnable(request, response, 0, MSG_FINISH));
    }

    public void postLoading(DownloadRequest request, long downloadedBytes) {
        mHandler.post(new DownloadDeliveryRunnable(request, null, downloadedBytes, MSG_LOADING));
    }

    class DownloadDeliveryRunnable implements Runnable {
        private DownloadRequest mRequest;
        private NetWorkResponse mResponse;
        private long mDownloadedBytes;
        private int mMsg;

        public DownloadDeliveryRunnable(DownloadRequest request, NetWorkResponse response, long downloadedBytes, int msg) {
            mRequest = request;
            mResponse = response;
            mDownloadedBytes = downloadedBytes;
            mMsg = msg;
        }

        @Override
        public void run() {
            switch (mMsg) {
                case MSG_DISPATCHED:
                    Log.d("debug", "request(" + mRequest.getOriginalUrl() + ")onDispatched");
                    mRequest.onDispatched();
                    break;
                case MSG_FINISH:
                    Log.d("debug", "request(" + mRequest.getOriginalUrl() + ")onFinish");
                    mRequest.onFinish(mResponse);
                case MSG_LOADING:
                    Log.d("debug", "request(" + mRequest.getOriginalUrl() + ")onLoading");
                    mRequest.onLoading(mDownloadedBytes);
                default:
            }
        }
    }
}
