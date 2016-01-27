package com.example.vigi.androiddownload.download.base;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

/**
 * Created by Vigi on 2016/1/27.
 */
public class NetWorkDelivery {
    private static final int MSG_DISPATCHED = 1;
    private static final int MSG_FINISH = 2;
    protected Handler mHandler;

    public NetWorkDelivery(Looper looper) {
        mHandler = new Handler(looper == null ? Looper.getMainLooper() : looper);
    }

    public void postDispatched(NetWorkRequest request) {
        mHandler.post(new NetWorkDeliveryRunnable(request, null, MSG_DISPATCHED));
    }

    public void postFinish(NetWorkRequest request, NetWorkResponse response) {
        mHandler.post(new NetWorkDeliveryRunnable(request, response, MSG_FINISH));
    }

    class NetWorkDeliveryRunnable implements Runnable {
        private NetWorkRequest mRequest;
        private NetWorkResponse mResponse;
        private int mMsg;

        public NetWorkDeliveryRunnable(NetWorkRequest request, NetWorkResponse response, int msg) {
            mRequest = request;
            mResponse = response;
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
                default:
                    Log.d("debug", "request(" + mRequest.getOriginalUrl() + ")onFinish");
                    mRequest.onFinish(mResponse);
            }
        }
    }
}
