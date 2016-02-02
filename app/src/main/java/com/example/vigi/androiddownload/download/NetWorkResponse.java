package com.example.vigi.androiddownload.download;

import java.io.InputStream;

public abstract class NetWorkResponse {
    public long mTotalLength;
    public long mContentLength;
    public InputStream mContentStream;
    public boolean mSupportRange = false;

    public abstract void disconnect();

}