package com.example.vigi.androiddownload.download;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

/**
 * Created by Vigi on 2016/1/21.
 */
public class UrlConnectionPerformer extends NetWorkPerformer {

    private SSLSocketFactory mSslSocketFactory;


    public UrlConnectionPerformer(@NonNull String userAgent) {
        super(userAgent);

    }

    public UrlConnectionPerformer(@NonNull String userAgent, SSLSocketFactory sslSocketFactory) {
        super(userAgent);
        mSslSocketFactory = sslSocketFactory;
    }

    @Override
    public NetWorkResponse performDownloadRequest(DownloadRequest downloadRequest) throws IOException {
        while (true) {
            if (downloadRequest.isCancel()) {
                return null;
            }

            String urlStr = downloadRequest.getUrl();
            URL url = new URL(urlStr);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            // may throw IllegalStateException when connection is connected
            connection.setDoInput(true);
            if ("https".equals(url.getProtocol()) && mSslSocketFactory != null) {
                ((HttpsURLConnection) connection).setSSLSocketFactory(mSslSocketFactory);
            }

            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", getUserAgent());
            connection.setRequestProperty("connection", "close");
            if (downloadRequest.getStartPos() != 0) {
                connection.setRequestProperty("Range", "bytes=" + downloadRequest.getStartPos() + "-");
            }
            connection.setConnectTimeout(DEFAULT_TIMEOUT_MS);
            connection.setReadTimeout(DEFAULT_TIMEOUT_MS);
            connection.connect();

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_MOVED_PERM || responseCode == HttpURLConnection.HTTP_MOVED_TEMP) {
                downloadRequest.setRedirectUrl(connection.getHeaderField("Location"));
                continue;
            }
            if (responseCode != HttpURLConnection.HTTP_OK && responseCode != HttpURLConnection.HTTP_PARTIAL) {
                return null;
            }
            // check whether server support range, download from start if not
            boolean isAcceptRange = true;
            String acceptRangeStr = connection.getHeaderField("Accept-Ranges");
            if ("none".equals(acceptRangeStr)) {
                isAcceptRange = false;
            } else {
                if (!"bytes".equals(acceptRangeStr) && responseCode != HttpURLConnection.HTTP_PARTIAL) {
                    isAcceptRange = false;
                }
            }
            if (!isAcceptRange) {
                downloadRequest.setStartPos(0);
            }
            // parse content length
            // TODO: 2016/1/26 and do not support 0 length download
            NetWorkResponse hr = new UrlConnectionResponse(connection);
            String lengthStr = connection.getHeaderField("Content-Length");
            if (TextUtils.isEmpty(lengthStr)) {
                hr.mContentLength = 0;
            } else {
                hr.mContentLength = Long.parseLong(lengthStr);
            }
            hr.mTotalLength = hr.mContentLength;
            if (isAcceptRange) {
                lengthStr = connection.getHeaderField("Content-Range");
                if (!TextUtils.isEmpty(lengthStr)) {
                    hr.mTotalLength = Long.parseLong(lengthStr.substring(lengthStr.lastIndexOf("/") + 1, lengthStr.length()));
                }
            }
            hr.mContentStream = connection.getInputStream();
            return hr;
        }
    }

    class UrlConnectionResponse extends NetWorkResponse {
        private HttpURLConnection mConnection;

        public UrlConnectionResponse(HttpURLConnection connection) {
            mConnection = connection;
        }

        @Override
        public void disconnect() {
            if (mConnection != null) {
                mConnection.disconnect();
                mConnection = null;
            }
        }
    }
}
