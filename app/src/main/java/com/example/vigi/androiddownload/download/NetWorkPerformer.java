package com.example.vigi.androiddownload.download;

import android.support.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;

/**
 * Thread Safety Performer
 * Created by Vigi on 2016/1/22.
 */
public abstract class NetWorkPerformer {
    protected static final int DEFAULT_TIMEOUT_MS = 5000;
    private String mUserAgent;

    public NetWorkPerformer(String userAgent) {
        mUserAgent = userAgent;
    }

    @NonNull
    public final String getUserAgent() {
        return mUserAgent;
    }

    public abstract NetWorkResponse performDownloadRequest(DownloadRequest downloadRequest) throws IOException;

    public static abstract class NetWorkResponse {
        public long mTotalLength;
        public long mContentLength;
        public InputStream mContentStream;

        public abstract void disconnect();
    }
}
