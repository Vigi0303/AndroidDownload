package com.example.vigi.androiddownload.download;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.io.File;
import java.io.IOException;

/**
 * Created by Vigi on 2016/1/20.
 */
public class DownloadRequest implements Comparable<DownloadRequest> {
    private int mSequence = 0;
    private String mOriginalUrl;
    private String mRedirectUrl;
    private File mTargetFile;
    private boolean mCancel = false;
    private long mStartPos = 0;
    private RequestListener mListener;

    public DownloadRequest(@NonNull String urlStr, @NonNull File file) {
        this(urlStr, file, 0, null);
    }

    public DownloadRequest(@NonNull String urlStr, @NonNull File file, long startPos, RequestListener listener) {
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
        mListener = listener;
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

    public File getTargetFile() {
        return mTargetFile;
    }

    void setRedirectUrl(String redirectUrl) {
        mRedirectUrl = redirectUrl;
    }

    long getStartPos() {
        return mStartPos;
    }

    void setStartPos(long startPos) {
        mStartPos = startPos;
    }

    public RequestListener getListener() {
        return mListener;
    }

    public void setListener(RequestListener listener) {
        mListener = listener;
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

    public interface RequestListener {
        void onStartWorker();

        void onLoading(long current);

        void onError(DownloadError error);

        void onFinish();
    }
}
