package com.example.vigi.androiddownload.download;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

/**
 * Created by Vigi on 2016/1/27.
 */
public class DownloadDelivery {
    private static final int MSG_DISPATCHED = 1;
    private static final int MSG_FINISH = 2;
    private static final int MSG_LOADING = 3;
    private static final int MSG_READ_LENGTH = 4;

    protected Handler mHandler;

    public DownloadDelivery(Looper looper) {
        mHandler = new DeliveryHandler(looper == null ? Looper.getMainLooper() : looper);
    }

    public void postDispatched(DownloadRequest request) {
        mHandler.obtainMessage(MSG_DISPATCHED, request).sendToTarget();
    }

    public void postFinish(DownloadRequest request, NetWorkResponse response) {
        request.setResponse(response);
        mHandler.obtainMessage(MSG_FINISH, request).sendToTarget();
    }

    public void postLoading(DownloadRequest request, long downloadedBytes) {
        request.setDownloadedBytes(downloadedBytes);
        mHandler.obtainMessage(MSG_LOADING, request).sendToTarget();
    }

    public void postTotalLength(DownloadRequest request, long totalBytes) {
        request.setTotalBytes(totalBytes);
        mHandler.obtainMessage(MSG_READ_LENGTH, request).sendToTarget();
    }

    class DeliveryHandler extends Handler {

        public DeliveryHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_DISPATCHED: {
                    DownloadRequest request = (DownloadRequest) msg.obj;
                    Log.d("debug", "request(" + request.getOriginalUrl() + ")onDispatched");
                    request.onDispatched();
                    break;
                }
                case MSG_FINISH: {
                    DownloadRequest request = (DownloadRequest) msg.obj;
                    Log.d("debug", "request(" + request.getOriginalUrl() + ")onFinish");
                    request.onFinish(request.getResponse());
                    break;
                }
                case MSG_LOADING: {
                    DownloadRequest request = (DownloadRequest) msg.obj;
//                    Log.d("debug", "request(" + request.getOriginalUrl() + ")onLoading");
                    request.onLoading(request.getCurrentBytes());
                    break;
                }
                case MSG_READ_LENGTH: {
                    DownloadRequest request = (DownloadRequest) msg.obj;
                    Log.d("debug", "request(" + request.getOriginalUrl() + ")onReadLength");
                    request.onReadLength(request.getTotalBytes());
                    break;
                }
                default:
                    Log.w("warn", "unknown msg to deliver");
            }
        }
    }
}
