package com.example.vigi.androiddownload.download;

import android.text.TextUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

/**
 * Created by Vigi on 2016/1/21.
 */
public class UrlConnectionPerformer implements NetWorkPerformer {
    public static final int DEFAULT_TIMEOUT_MS = 5000;
    private SSLSocketFactory mSslSocketFactory;

    public UrlConnectionPerformer() {
    }

    public UrlConnectionPerformer(SSLSocketFactory sslSocketFactory) {
        mSslSocketFactory = sslSocketFactory;
    }

    @Override
    public HttpResponse performDownloadRequest(DownloadRequest downloadRequest) throws IOException {
        while (true) {
            if (downloadRequest.isCancel()) {
                return null;
            }

            String urlStr = downloadRequest.getUrl();
            URL url = new URL(urlStr);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            if ("https".equals(url.getProtocol()) && mSslSocketFactory != null) {
                ((HttpsURLConnection) connection).setSSLSocketFactory(mSslSocketFactory);
            }
            connection.setRequestMethod("GET");
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
            HttpResponse hr = new HttpUrlResponse(connection);
            String lengthStr = connection.getHeaderField("Content-Length");
            if (TextUtils.isEmpty(lengthStr)) {
                hr.mContentLength = 0;
            } else {
                hr.mContentLength = Long.parseLong(lengthStr);
            }
            lengthStr = connection.getHeaderField("Content-Range");
            if (TextUtils.isEmpty(lengthStr)) {
                hr.mTotalLength = hr.mContentLength;
            } else {
                hr.mTotalLength = Long.parseLong(lengthStr.substring(lengthStr.lastIndexOf("/") + 1, lengthStr.length()));
            }
            hr.mContentStream = connection.getInputStream();
            return hr;
        }
    }

    class HttpUrlResponse extends HttpResponse {
        private HttpURLConnection mConnection;

        public HttpUrlResponse(HttpURLConnection connection) {
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
