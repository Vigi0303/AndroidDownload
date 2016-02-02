package com.example.vigi.androiddownload.download;

import com.orhanobut.logger.Logger;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;

/**
 * Created by Vigi on 2016/1/20.
 */
public class DownloadWorker {
    private static final int SLEEP_INTERNAL = 500;
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
        InputStream bis = null;
        OutputStream bos = null;
        NetWorkResponse response = null;
        DownloadException error = null;

        try {
            response = mNetWorkPerformer.performDownloadRequest(mDownloadRequest);
            if (mDownloadRequest.isCancel()) {
                return null;
            }
            validateServerData(mDownloadRequest, response);
            mDelivery.postTotalLength(mDownloadRequest, response.mTotalLength);

            File targetFile = mDownloadRequest.getTargetFile();
            bis = new BufferedInputStream(response.mContentStream);
            // TODO: 2016/2/1 file io exception
            bos = generateWriteStream(targetFile, response.mTotalLength, mDownloadRequest.getStartPos());
            byte[] tmp = new byte[STREAM_BUFFER];
            long downloadedBytes = 0;
            int len;
            // TODO: 2016/2/1 IOException
            // TODO: 2016/2/1 may throw lots kind of exception when bad or no network
            while ((len = bis.read(tmp)) != -1) {
                bos.write(tmp, 0, len);
                downloadedBytes += len;
                mDelivery.postLoading(mDownloadRequest, downloadedBytes);
                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }
                if (mDownloadRequest.isCancel()) {
                    return null;
                }
            }
        } catch (IOException e) {
            Logger.e(e, "vigi");
        } catch (Exception e) {
            // unhandled exception
            error = new DownloadException(DownloadException.EXCEPTION_CODE_UNKNOWN, e);
        } finally {
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
        return new DownloadResult(error);
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

    protected void validateServerData(DownloadRequest request, NetWorkResponse response) throws DownloadException {
        // TODO: 2016/1/26 and do not support 0 length download
        if (response.mContentLength == 0 || response.mTotalLength == 0) {
            throw new DownloadException(DownloadException.EXCEPTION_CODE_PARSE, "url(" + request.getOriginalUrl() + ") does not return content length");
        }
    }
}
