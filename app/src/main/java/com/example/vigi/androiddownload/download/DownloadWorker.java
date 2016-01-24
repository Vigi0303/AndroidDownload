package com.example.vigi.androiddownload.download;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.BlockingQueue;

/**
 * Created by Vigi on 2016/1/20.
 */
public class DownloadWorker extends Thread {
    private static final int STREAM_BUFFER = 4096;
    private BlockingQueue<DownloadRequest> mRequestQueue;
    private HttpPerformer mHttpPerformer;
    private DownloadListener mDownloadListener;
    private volatile boolean mQuit = false;   // TODO: 2016/1/24 why volatile

    public DownloadWorker(BlockingQueue<DownloadRequest> requestQueue
            , HttpPerformer httpPerformer, DownloadListener downloadListener) {
        mRequestQueue = requestQueue;
        mHttpPerformer = httpPerformer;
        mDownloadListener = downloadListener;
    }

    @Override
    public void run() {
        while (true) {
            DownloadRequest downloadRequest = null;
            try {
                downloadRequest = mRequestQueue.take();
            } catch (InterruptedException e) {
                if (mQuit) {
                    return;
                }
                continue;
            }

            BufferedInputStream bis = null;
            RandomAccessFile rafo = null;
            HttpPerformer.HttpResponse httpResponse = mHttpPerformer.performDownloadRequest(downloadRequest);

            if (httpResponse == null
                    || httpResponse.mTotalLength == 0
                    || httpResponse.mContentStream == null) {
                continue;
            }

            try {
                File targetFile = downloadRequest.getTargetFile();
                bis = new BufferedInputStream(httpResponse.mContentStream);
                rafo = new RandomAccessFile(targetFile, "rw");
                rafo.setLength(httpResponse.mTotalLength);
                rafo.seek(downloadRequest.getStartPos());

                byte[] tmp = new byte[STREAM_BUFFER];
                int len;
                while ((len = bis.read(tmp)) != -1) {
                    rafo.write(tmp, 0, len);
                }
            } catch (IOException e) {

            } finally {
                downloadRequest.cancel();
                mHttpPerformer.cancel();
                try {
                    if (bis != null) {
                        bis.close();
                    }
                    if (rafo != null) {
                        rafo.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void quit() {
        mQuit = true;
        interrupt();
    }
}
