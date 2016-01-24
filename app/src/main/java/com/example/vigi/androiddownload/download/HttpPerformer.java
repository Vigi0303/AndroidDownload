package com.example.vigi.androiddownload.download;

import java.io.InputStream;

/**
 * Created by Vigi on 2016/1/22.
 */
public interface HttpPerformer {

    /**
     * @return null表示解析失败
     */
    public HttpResponse performDownloadRequest(DownloadRequest downloadRequest);

    public void cancel();

    public static class HttpResponse {
        public long mTotalLength;
        public long mContentLength;
        public InputStream mContentStream;
    }
}
