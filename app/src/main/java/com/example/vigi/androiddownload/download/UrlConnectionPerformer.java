package com.example.vigi.androiddownload.download;

import android.text.TextUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

/**
 * Created by Vigi on 2016/1/21.
 */
public class UrlConnectionPerformer implements NetWorkPerformer<UrlConnectionResponse> {

    private SSLSocketFactory mSslSocketFactory;
    private String mUserAgent;


    public UrlConnectionPerformer(String userAgent) {
        this(userAgent, null);
    }

    public UrlConnectionPerformer(String userAgent, SSLSocketFactory sslSocketFactory) {
        mUserAgent = userAgent;
        mSslSocketFactory = sslSocketFactory;
    }

    @Override
    public UrlConnectionResponse performDownloadRequest(DownloadRequest request, long range) throws DownloadException {
        while (true) {
            if (request.isCancel()) {
                return null;
            }

            try {
                String urlStr = request.getUrl();
                URL url = new URL(urlStr);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                if ("https".equals(url.getProtocol()) && mSslSocketFactory != null) {
                    ((HttpsURLConnection) connection).setSSLSocketFactory(mSslSocketFactory);
                }
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", mUserAgent);
                connection.setRequestProperty("connection", "close");
                if (range != 0) {
                    connection.setRequestProperty("Range", "bytes=" + range + "-");
                }
                connection.setConnectTimeout(DEFAULT_TIMEOUT_MS);
                connection.setReadTimeout(DEFAULT_TIMEOUT_MS);
                connection.connect();

                UrlConnectionResponse hcr = new UrlConnectionResponse(connection);
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_MOVED_PERM || responseCode == HttpURLConnection.HTTP_MOVED_TEMP) {
                    request.setRedirectUrl(connection.getHeaderField("Location"));
                    continue;
                }
                if (responseCode != HttpURLConnection.HTTP_OK && responseCode != HttpURLConnection.HTTP_PARTIAL) {
                    throw new DownloadException(DownloadException.EXCEPTION_CODE_PARSE
                            , "url(" + request.getOriginalUrl() + ") return error statusCode(" + responseCode + ")");
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
                // parse content length
                String lengthStr = connection.getHeaderField("Content-Length");
                if (TextUtils.isEmpty(lengthStr)) {
                    hcr.contentLength = 0;
                } else {
                    hcr.contentLength = Long.parseLong(lengthStr);
                }
                hcr.totalLength = hcr.contentLength;
                if (isAcceptRange) {
                    lengthStr = connection.getHeaderField("Content-Range");
                    if (!TextUtils.isEmpty(lengthStr)) {
                        hcr.totalLength = Long.parseLong(lengthStr.substring(lengthStr.lastIndexOf("/") + 1, lengthStr.length()));
                    }
                    hcr.supportRange = true;
                }
                hcr.contentStream = connection.getInputStream();
                return hcr;
            } catch (IOException e) {
                if (e instanceof MalformedURLException) {
                    throw new DownloadException(DownloadException.BAD_URL, e);
                }
                if (e instanceof UnknownHostException) {
                    throw new DownloadException(DownloadException.UNKNOWN_HOST, e);
                }
                if (e instanceof SocketException) {
                    throw new DownloadException(DownloadException.NO_CONNECTION, e);
                }
                if (e instanceof SocketTimeoutException) {
                    throw new DownloadException(DownloadException.SOCKET_TIMEOUT, e);
                }
                throw new DownloadException(DownloadException.UNKNOWN_NETWORK, e);
            }
        }
    }
}
