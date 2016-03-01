package com.example.vigi.androiddownload.core;

import android.os.SystemClock;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.ProtocolException;
import java.net.SocketException;

/**
 * Created by Vigi on 2016/1/20.
 */
public class DownloadWorker {
    private static final int SLEEP_INTERNAL_MS = 1000;
    private static final int STREAM_BUFFER = 1024;
    private NetWorkPerformer mNetWorkPerformer;
    private DownloadDelivery mDelivery;
    private DownloadRequest mDownloadRequest;

    public DownloadWorker(NetWorkPerformer netWorkPerformer, DownloadDelivery delivery, DownloadRequest request) {
        mNetWorkPerformer = netWorkPerformer;
        mDelivery = delivery;
        mDownloadRequest = request;
    }

    public DownloadResult work() throws InterruptedException {
        long startTimeMs = SystemClock.elapsedRealtime();
        long downloadedBytes = 0;
        long timeMsRecord = startTimeMs;
        int retryCount = 0;
        while (true) {
            InputStream bis = null;
            CustomOutputStream bos = null;
            NetWorkResponse response = null;
            DownloadException error = null;
            try {
                if (mDownloadRequest.isCancel()) {
                    return null;
                }
                LogHelper.logError("I am performing~");
                response = mNetWorkPerformer.performDownloadRequest(mDownloadRequest, mDownloadRequest.getStartPos() + downloadedBytes);
                timeMsRecord = SystemClock.elapsedRealtime();
                if (mDownloadRequest.isCancel()) {
                    return null;
                }
                validateServerData(mDownloadRequest, response);
                if (!response.supportRange) {
                    mDownloadRequest.setStartPos(0);
                    downloadedBytes = 0;
                }
                mDelivery.postTotalLength(mDownloadRequest, response.totalLength);

                File targetFile = mDownloadRequest.getTargetFile();
                bis = new BufferedInputStream(response.contentStream);
                bos = new CustomOutputStream(generateWriteStream(targetFile, response.totalLength, mDownloadRequest.getStartPos()));
                byte[] bytesTmp = new byte[STREAM_BUFFER];
                int bytesLen;
                long lastTimeMs = 0;
                long currTime;
                while ((bytesLen = bis.read(bytesTmp)) != -1) {
                    bos.customWrite(bytesTmp, 0, bytesLen);
                    downloadedBytes += bytesLen;
                    currTime = SystemClock.elapsedRealtime();
                    timeMsRecord = currTime;
                    if (currTime - lastTimeMs >= mDownloadRequest.getRate()) {
                        lastTimeMs = currTime;
                        mDelivery.postLoading(mDownloadRequest, downloadedBytes);
                        LogHelper.logError("I receive " + bytesLen + " and downloaded " + downloadedBytes + " :)");
                    }
                    if (Thread.interrupted()) {
                        throw new InterruptedException();
                    }
                    if (mDownloadRequest.isCancel()) {
                        return null;
                    }
                }
                LogHelper.logError("I read finish. bytesLen=" + bytesLen + " and total size=" + downloadedBytes);
            } catch (InterruptedException e) {
                throw e;
            } catch (Exception e) {
                if (e instanceof IOException) {
                    if (e instanceof SocketException) {
                        error = new DownloadException(DownloadException.NO_CONNECTION, e);
                    } else if (e instanceof ProtocolException) {
                        error = new DownloadException(DownloadException.UNKNOWN_NETWORK, e);
                    } else {
                        error = new DownloadException(DownloadException.UNKNOWN, e);
                    }
                } else if (e instanceof DownloadException) {
                    error = (DownloadException) e;
                } else {
                    // unhandled exception
                    error = new DownloadException(DownloadException.UNKNOWN, e);
                }

                if (error.isBadNetwork()) {
                    int waitMs = (int) (SystemClock.elapsedRealtime() - timeMsRecord);
                    if (waitMs <= mDownloadRequest.getTimeOut()) {
                        LogHelper.logError("wait for network: " + waitMs + "ms");
                        Thread.sleep(SLEEP_INTERNAL_MS);       // I need have a rest
                        retryCount += 1;
                        continue;
                    }
                    LogHelper.logError("I give up ... for wait " + waitMs + "ms", e);
                } else {
                    LogHelper.logError("some one kill me!!", e);
                }
            } finally {
                if (response != null) {
                    response.disconnect();
                }
                mDelivery.postLoading(mDownloadRequest, downloadedBytes);
                try {
                    if (bos != null) {
                        bos.flush();
                        bos.close();
                    }
                    if (bis != null) {
                        bis.close();
                    }
                } catch (IOException e) {
                    LogHelper.logError("unknown error", e);
                }
            }
            return new DownloadResult(error);
        }
    }

    private OutputStream generateWriteStream(File file, long fileLength, long startPos) throws DownloadException {
        try {
            RandomAccessFile raf = new RandomAccessFile(file, "rw");
            raf.setLength(fileLength);
            raf.seek(startPos);
            return new FileOutputStream(raf.getFD());
        } catch (IOException e) {
            throw new DownloadException(DownloadException.LOCAL_IO, e);
        }
    }

    private void validateServerData(DownloadRequest request, NetWorkResponse response) throws DownloadException {
        // TODO: 2016/1/26 and do not support 0 length download
        if (response.contentLength == 0 || response.totalLength == 0) {
            throw new DownloadException(DownloadException.EXCEPTION_CODE_PARSE, "url(" + request.getOriginalUrl() + ") does not return content length");
        }
    }

    private class CustomOutputStream extends BufferedOutputStream {

        public CustomOutputStream(OutputStream out) {
            super(out);
        }

        public void customWrite(byte[] buffer, int offset, int length) throws DownloadException {
            try {
                super.write(buffer, offset, length);
            } catch (IOException e) {
                throw new DownloadException(DownloadException.LOCAL_IO, e);
            }
        }
    }
}
