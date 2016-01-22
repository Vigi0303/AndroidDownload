package com.example.vigi.androiddownload.download;

import android.text.TextUtils;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Vigi on 2016/1/20.
 */
public class DownloadRequest implements Comparable<DownloadRequest> {
    private static AtomicInteger SequenceGenerator = new AtomicInteger();
    private int mSequence = 0;
    private String mOriginalUrl;
    private String mRedirectUrl;
    private File mTargetFile;
    private boolean mCancel;
    private long mStartPos;

    private DownloadRequest() {
    }

    public static DownloadRequest buildRequest(String urlStr, File file) {
        if (TextUtils.isEmpty(urlStr)) {
            throw new IllegalArgumentException("urlStr can not be empty!");
        }
        if (file == null) {
            throw new IllegalArgumentException("file can not be null!");
        }
        if (!file.exists()) {

        }
        DownloadRequest request = new DownloadRequest();
        request.mSequence = SequenceGenerator.incrementAndGet();
        request.mOriginalUrl = urlStr;
        request.mTargetFile = file;
        request.mCancel = false;
        request.mStartPos = 0;
        return request;
    }

    public String getUrl() {
        return (mRedirectUrl != null) ? mRedirectUrl : mOriginalUrl;
    }

    public String getOriginalUrl() {
        return mOriginalUrl;
    }

    public File getTargetFile() {
        return mTargetFile;
    }

    public void setRedirectUrl(String redirectUrl) {
        mRedirectUrl = redirectUrl;
    }

    public long getStartPos() {
        return mStartPos;
    }

    public void setStartPos(long startPos) {
        mStartPos = startPos;
    }

    /**
     * cancel后无法再次启动
     */
    public void cancel() {
        mCancel = true;
        mTargetFile = null;
    }

    public boolean isCancel() {
        return mCancel;
    }

    @Override
    public int compareTo(DownloadRequest another) {
        return this.mSequence - another.mSequence;
    }
}
