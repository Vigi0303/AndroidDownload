package com.example.vigi.androiddownload.download;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.concurrent.BlockingQueue;

/**
 * Created by Vigi on 2016/1/20.
 */
public class DownloadWorker extends Thread {
    private BlockingQueue<DownloadRequest> mRequestQueue;
    private HttpPerformer mHttpPerformer;
    private volatile boolean mQuit = false;

    public DownloadWorker(BlockingQueue<DownloadRequest> requestQueue, HttpPerformer httpPerformer) {
        mRequestQueue = requestQueue;
        mHttpPerformer = httpPerformer;
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

            InputStream bis = null;
            OutputStream bos = null;
            HttpPerformer.HttpResponse httpResponse = mHttpPerformer.performDownloadRequest(downloadRequest);

            if (httpResponse == null || httpResponse.mTotalLength == 0 || httpResponse.mContentStream == null) {
                continue;
            }

            try {
                File targetFile = downloadRequest.getTargetFile();
                if (!targetFile.exists()) {
                    RandomAccessFile raf = new RandomAccessFile(targetFile, "rw");
                    raf.setLength(httpResponse.mTotalLength);
                    raf.close();
                }
                bis = new BufferedInputStream(httpResponse.mContentStream);
                bos = new BufferedOutputStream(new FileOutputStream(targetFile));
                byte[] tmp = new byte[4096];
                int len;
                while ((len = bis.read(tmp)) != -1) {
                    bos.write(tmp, 0, len);
                    bos.flush();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (bis != null) {
                        bis.close();
                    }
                    if (bos != null) {
                        bos.flush();
                        bos.close();
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
