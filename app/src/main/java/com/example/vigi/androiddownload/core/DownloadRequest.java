package com.example.vigi.androiddownload.core;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.io.File;

/**
 * Created by Vigi on 2016/1/27.
 */
public class DownloadRequest implements Comparable<DownloadRequest> {
    private static final int DEFAULT_RATE_MS = 1000;
    private int mSequence = 0;
    private String mOriginalUrl;
    private String mRedirectUrl;
    private boolean mCancel = false;
    private File mTargetFile;
    private long mStartPos = 0;
    private long mDownloadedBytes = 0;
    private long mTotalBytes = 0;
    private int mTimeOut = 0;
    private int mRate = DEFAULT_RATE_MS;
    private DownloadResult mResult;

    public DownloadRequest(@NonNull String urlStr, @NonNull File file) {
        this(urlStr, file, 0);
    }

    public DownloadRequest(@NonNull String urlStr, @NonNull File file, long startPos) {
        if (TextUtils.isEmpty(urlStr)) {
            throw new IllegalArgumentException("urlStr can not be empty!");
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

    protected DownloadResult getResult() {
        return mResult;
    }

    protected void setResult(DownloadResult result) {
        mResult = result;
    }

    protected void setDownloadedBytes(long downloadedBytes) {
        mDownloadedBytes = downloadedBytes;
    }

    public long getCurrentBytes() {
        return mStartPos + mDownloadedBytes;
    }

    public long getTotalBytes() {
        return mTotalBytes;
    }

    protected void setTotalBytes(long totalBytes) {
        mTotalBytes = totalBytes;
    }

    public int getTimeOut() {
        return mTimeOut;
    }

    public void setTimeOut(int timeOut) {
        mTimeOut = timeOut;
    }

    public int getRate() {
        return mRate;
    }

    public void setRate(int rate) {
        mRate = rate;
    }

    /**
     * cancel request and it cannot reuse any more
     * <p>It doesn't take effect immediately until {@link #onCanceled()} reached</p>
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

    protected void onReadLength(long totalBytes) {

    }

    protected void onFinish(DownloadResult result) {

    }

    protected void onLoading(long downloadedBytes) {

    }

    protected void onCanceled() {

    }
}
