package com.example.vigi.androiddownload.download;

/**
 * Created by Vigi on 2016/1/20.
 */
public interface DownloadListener {
    void onLoading(long current);
    void onError(DownloadError error);
    void onFinish();
}
