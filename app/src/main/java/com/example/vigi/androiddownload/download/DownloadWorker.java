package com.example.vigi.androiddownload.download;

import android.os.Process;

import com.orhanobut.logger.Logger;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;

/**
 * Created by Vigi on 2016/1/20.
 */
public class DownloadWorker extends Thread {
    private static final int STREAM_BUFFER = 1024;
    private BlockingQueue<DownloadRequest> mRequestQueue;
    private NetWorkPerformer mNetWorkPerformer;
    private DownloadDelivery mDelivery;
    private volatile boolean mQuit = false;   // TODO: 2016/1/24 why volatile

    public DownloadWorker(BlockingQueue<DownloadRequest> requestQueue
            , NetWorkPerformer netWorkPerformer, DownloadDelivery delivery) {
        super("DownloadWorker");
        mRequestQueue = requestQueue;
        mNetWorkPerformer = netWorkPerformer;
        mDelivery = delivery;
    }

    @Override
    public void run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
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
            mDelivery.postDispatched(downloadRequest);

            InputStream bis = null;
            OutputStream bos = null;
            NetWorkResponse response = null;

            try {
                response = mNetWorkPerformer.performDownloadRequest(downloadRequest);
                if (downloadRequest.isCancel()) {
                    continue;
                }
                validateServerData(downloadRequest, response);
                mDelivery.postTotalLength(downloadRequest, response.mTotalLength);

                File targetFile = downloadRequest.getTargetFile();
                bis = new BufferedInputStream(response.mContentStream);
                // TODO: 2016/2/1 file io exception
                bos = generateWriteStream(targetFile, response.mTotalLength, downloadRequest.getStartPos());
                byte[] tmp = new byte[STREAM_BUFFER];
                long downloadedBytes = 0;
                int len;
                // TODO: 2016/2/1 IOException
                // TODO: 2016/2/1 may throw lots kind of exception when bad or no network
                while ((len = bis.read(tmp)) != -1) {
                    bos.write(tmp, 0, len);
                    downloadedBytes += len;
                    mDelivery.postLoading(downloadRequest, downloadedBytes);
                    if (Thread.interrupted()) {
                        return;
                    }
                    if (downloadRequest.isCancel()) {
                        break;
                    }
                }
            } catch (InterruptedException e) {
                continue;
            } catch (IOException e) {
                Logger.e(e, "vigi");
                if (response == null) {
                    response = new UrlConnectionResponse(null);
                    response.mError = new DownloadError.NetWorkError();
                } else if (e instanceof SocketException) {
                } else {
                    // unexpected io error
                    response.mError = new DownloadError(e);
                }
            } catch (DownloadError error) {
                response.mError = error;
            } catch (Exception e) {
                // unhandled exception
            } finally {
                mDelivery.postFinish(downloadRequest, response);
                downloadRequest.cancel();
                if (response != null) {
                    response.disconnect();
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
        if (!file.exists() && startPos == 0) {
            RandomAccessFile raf = new RandomAccessFile(file, "rw");
            raf.setLength(fileLength);
            raf.close();
            return new BufferedOutputStream(new FileOutputStream(file));
        }
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        raf.seek(startPos);
        return new BufferedOutputStream(new FileOutputStream(raf.getFD()));
    }

    protected void validateServerData(DownloadRequest request, NetWorkResponse response) throws DownloadError.ServerError {
        // TODO: 2016/1/26 and do not support 0 length download
        if (response.mContentLength == 0 || response.mTotalLength == 0) {
            throw new DownloadError.ServerError("url(" + request.getOriginalUrl() + ") does not return content length");
        }
    }

    public void quit() {
        mQuit = true;
        interrupt();
    }
}
