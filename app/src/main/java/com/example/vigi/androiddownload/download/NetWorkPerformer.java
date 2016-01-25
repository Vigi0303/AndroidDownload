package com.example.vigi.androiddownload.download;

import java.io.IOException;
import java.io.InputStream;

/**
 * Thread Safety Performer
 * Created by Vigi on 2016/1/22.
 */
public interface NetWorkPerformer {

    /**
     * @return null表示解析失败
     */
    public HttpResponse performDownloadRequest(DownloadRequest downloadRequest) throws IOException;

    public static abstract class HttpResponse {
        public long mTotalLength;
        public long mContentLength;
        public InputStream mContentStream;

        public abstract void disconnect();
    }
}
