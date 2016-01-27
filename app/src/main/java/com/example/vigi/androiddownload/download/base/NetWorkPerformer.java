package com.example.vigi.androiddownload.download.base;

import java.io.IOException;

/**
 * Thread Safety Performer
 * Created by Vigi on 2016/1/22.
 */
public abstract class NetWorkPerformer<T1 extends NetWorkRequest, T2 extends NetWorkResponse> {
    protected static final int DEFAULT_TIMEOUT_MS = 5000;
    protected String mUserAgent;

    public NetWorkPerformer(String userAgent) {
        mUserAgent = userAgent;
    }

    public abstract T2 performDownloadRequest(T1 request) throws IOException;
}
