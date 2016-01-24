package com.example.vigi.androiddownload.download;

import android.text.TextUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Vigi on 2016/1/21.
 */
public class HttpUrlPerformer implements HttpPerformer {
    private HttpURLConnection mConnection;

    @Override
    public HttpResponse performDownloadRequest(DownloadRequest downloadRequest) {
        while (true) {
            mConnection = null;
            if (downloadRequest.isCancel()) {
                return null;
            }

            try {
                String urlStr = downloadRequest.getUrl();
                URL url = new URL(urlStr);
                if (!"http".equals(url.getProtocol())) {
                    throw new MalformedURLException("url(" + url + ") must be http url!");
                }
                mConnection = (HttpURLConnection) url.openConnection();
                mConnection.setRequestMethod("GET");
                mConnection.setRequestProperty("Range", "bytes=" + downloadRequest.getStartPos() + "-");

                int responseCode = mConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_MOVED_PERM || responseCode == HttpURLConnection.HTTP_MOVED_TEMP) {
                    downloadRequest.setRedirectUrl(mConnection.getHeaderField("Location"));
                    continue;
                }
                if (responseCode != HttpURLConnection.HTTP_OK && responseCode != HttpURLConnection.HTTP_PARTIAL) {
                    return null;
                }
                HttpResponse hr = new HttpResponse();
                String lengthStr = mConnection.getHeaderField("Content-Length");
                if (TextUtils.isEmpty(lengthStr)) {
                    hr.mContentLength = 0;
                } else {
                    hr.mContentLength = Long.parseLong(lengthStr);
                }
                lengthStr = mConnection.getHeaderField("Content-Range");
                if (TextUtils.isEmpty(lengthStr)) {
                    hr.mTotalLength = hr.mContentLength;
                } else {
                    hr.mTotalLength = Long.parseLong(lengthStr.substring(lengthStr.lastIndexOf("/") + 1, lengthStr.length()));
                }
                hr.mContentStream = mConnection.getInputStream();
                return hr;
            } catch (IOException e) {
                return null;
            } finally {
                if (mConnection != null) {
                    mConnection.disconnect();
                }
            }
        }
    }

    @Override
    public void cancel() {
        // TODO: 2016/1/24 to validate whether code below takes effect
        if (mConnection != null) {
            mConnection.disconnect();
        }
    }
}
