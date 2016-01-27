package com.example.vigi.androiddownload.download;

/**
 * Created by Vigi on 2016/1/20.
 */
public class DownloadError extends Exception {
    public DownloadError() {
    }

    public DownloadError(Throwable throwable) {
        super(throwable);
    }

    public DownloadError(String detailMessage) {
        super(detailMessage);
    }

    public static class ServerError extends DownloadError {

        public ServerError(String s) {
            super(s);
        }
    }

    public static class NetWorkError extends DownloadError {
    }

    public static class TimeOutError extends NetWorkError {
    }
}
