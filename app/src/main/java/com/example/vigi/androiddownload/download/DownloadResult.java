package com.example.vigi.androiddownload.download;

/**
 * Created by Vigi on 2016/2/2.
 */
public class DownloadResult {
    public DownloadException error;

    public DownloadResult(DownloadException error) {
        this.error = error;
    }

}