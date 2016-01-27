package com.example.vigi.androiddownload.download.base;

import java.io.InputStream;

public abstract class NetWorkResponse {
    public long mTotalLength;
    public long mContentLength;
    public InputStream mContentStream;

    public abstract void disconnect();
}