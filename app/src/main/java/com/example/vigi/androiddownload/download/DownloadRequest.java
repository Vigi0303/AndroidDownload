package com.example.vigi.androiddownload.download;

import android.support.annotation.NonNull;

import com.example.vigi.androiddownload.download.base.NetWorkRequest;

import java.io.File;
import java.io.IOException;

/**
 * Created by Vigi on 2016/1/27.
 */
public class DownloadRequest extends NetWorkRequest {
    private File mTargetFile;
    private long mStartPos = 0;

    public DownloadRequest(@NonNull String urlStr, @NonNull File file) {
        this(urlStr, file, 0);
    }

    public DownloadRequest(@NonNull String urlStr, @NonNull File file, long startPos) {
        super(urlStr);
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

        mTargetFile = file;
        mStartPos = startPos;
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

    @Override
    public void cancel() {
        super.cancel();
        mTargetFile = null;
    }

    protected void onLoading(long downloadedBytes) {

    }
}
