package com.example.vigi.androiddownload.download;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.io.File;
import java.io.IOException;

/**
 * Created by Vigi on 2016/1/27.
 */
public class DownloadRequest implements Comparable<DownloadRequest> {
    private int mSequence = 0;
    private String mOriginalUrl;
    private String mRedirectUrl;
    private boolean mCancel = false;
    private File mTargetFile;
    private long mStartPos = 0;

    public DownloadRequest(@NonNull String urlStr, @NonNull File file) {
        this(urlStr, file, 0);
    }

    public DownloadRequest(@NonNull String urlStr, @NonNull File file, long startPos) {
        if (TextUtils.isEmpty(urlStr)) {
            throw new IllegalArgumentException("urlStr can not be empty!");
        }
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (!file.isFile()) {
            throw new IllegalArgumentException("A illegal file!");
        }

        mOriginalUrl = urlStr;
        mTargetFile = file;
        mStartPos = startPos;
    }

    public void setSequence(int sequence) {
        mSequence = sequence;
    }

    public String getUrl() {
        return (mRedirectUrl != null) ? mRedirectUrl : mOriginalUrl;
    }

    public String getOriginalUrl() {
        return mOriginalUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        mRedirectUrl = redirectUrl;
    }

    public File getTargetFile() {
        return mTargetFile;
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

    protected void onDispatched() {

    }

    protected void onFinish(NetWorkResponse response) {

    }

    protected void onLoading(long downloadedBytes) {

    }
}
