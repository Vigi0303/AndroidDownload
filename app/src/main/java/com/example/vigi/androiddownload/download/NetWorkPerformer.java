package com.example.vigi.androiddownload.download;

import java.io.IOException;

/**
 * Thread Safety Performer
 * Created by Vigi on 2016/1/22.
 */
public interface NetWorkPerformer<T extends NetWorkResponse> {
    public static final int DEFAULT_TIMEOUT_MS = 5000;

    public T performDownloadRequest(DownloadRequest request) throws IOException, DownloadError, InterruptedException;
}
