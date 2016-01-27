package com.example.vigi.androiddownload.download.base;

import android.support.annotation.NonNull;
import android.text.TextUtils;

/**
 * Created by Vigi on 2016/1/20.
 */
public class NetWorkRequest implements Comparable<NetWorkRequest> {
    private int mSequence = 0;
    private String mOriginalUrl;
    private String mRedirectUrl;
    private boolean mCancel = false;

    public NetWorkRequest(@NonNull String urlStr) {
        if (TextUtils.isEmpty(urlStr)) {
            throw new IllegalArgumentException("urlStr can not be empty!");
        }
        mOriginalUrl = urlStr;
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

    /**
     * cancel后无法再次启动
     */
    public void cancel() {
        mCancel = true;
    }

    public boolean isCancel() {
        return mCancel;
    }

    @Override
    public int compareTo(NetWorkRequest another) {
        return this.mSequence - another.mSequence;
    }

    protected void onDispatched() {

    }

    protected void onFinish(NetWorkResponse response) {

    }
}
