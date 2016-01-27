package com.example.vigi.androiddownload.download;

import java.io.InputStream;

public abstract class NetWorkResponse {
    public long mTotalLength;
    public long mContentLength;
    public InputStream mContentStream;
    public boolean mSupportRange = false;
    public DownloadError mError;

    public boolean hasError() {
        return mError != null;
    }

    public abstract void disconnect();

}