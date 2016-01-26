package com.example.vigi.androiddownload.download;

import android.os.Process;
import android.util.Log;

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
    private static final int STREAM_BUFFER = 1024;
    private BlockingQueue<DownloadRequest> mRequestQueue;
    private NetWorkPerformer mNetWorkPerformer;
    private volatile boolean mQuit = false;   // TODO: 2016/1/24 why volatile

    public DownloadWorker(BlockingQueue<DownloadRequest> requestQueue
            , NetWorkPerformer netWorkPerformer) {
        super("DownloadWorker");
        mRequestQueue = requestQueue;
        mNetWorkPerformer = netWorkPerformer;
    }

    @Override
    public void run() {
        android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
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
            DownloadRequest.RequestListener requestListener = downloadRequest.getListener();
            if (requestListener != null) {
                requestListener.onStartWorker();
            }
            Log.d("debug", "onStartWorker");

            InputStream bis = null;
            OutputStream bos = null;
            NetWorkPerformer.NetWorkResponse netWorkResponse = null;

            try {
                netWorkResponse = mNetWorkPerformer.performDownloadRequest(downloadRequest);
                if (netWorkResponse == null
                        || netWorkResponse.mTotalLength == 0
                        || netWorkResponse.mContentStream == null) {
                    throw new DownloadError();
                }
                File targetFile = downloadRequest.getTargetFile();
                bis = new BufferedInputStream(netWorkResponse.mContentStream);
                bos = generateWriteStream(targetFile, netWorkResponse.mTotalLength, downloadRequest.getStartPos());
                byte[] tmp = new byte[STREAM_BUFFER];
                long currentPos = downloadRequest.getStartPos();
                int len;
                while ((len = bis.read(tmp)) != -1) {
                    bos.write(tmp, 0, len);
                    currentPos += len;
                    if (requestListener != null) {
                        requestListener.onLoading(currentPos);
                    }
                    Log.d("debug", "onLoading: " + currentPos);
                    if (Thread.interrupted()) {
                        return;
                    }
                    if (downloadRequest.isCancel()) {
                        break;
                    }
                }
                Log.d("debug", "finish");
            } catch (Exception e) {
                // TODO: 2016/1/25 handle exception
                Log.d("debug", "error");
                e.printStackTrace();
            } finally {
                if (requestListener != null) {
                    requestListener.onFinish();
                }
                Log.d("debug", "onFinally");
                downloadRequest.cancel();
                if (netWorkResponse != null) {
                    netWorkResponse.disconnect();
                }
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

    private OutputStream generateWriteStream(File file, long fileLength, long startPos) throws IOException {
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        raf.setLength(fileLength);
        raf.seek(startPos);
        return new BufferedOutputStream(new FileOutputStream(raf.getFD()));
    }

    public void quit() {
        mQuit = true;
        interrupt();
    }
}
