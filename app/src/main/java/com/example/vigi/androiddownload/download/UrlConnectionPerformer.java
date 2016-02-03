package com.example.vigi.androiddownload.download;

import android.text.TextUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketException;
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
    public UrlConnectionResponse performDownloadRequest(DownloadRequest request)
            throws MalformedURLException, DownloadException {
        while (true) {
            if (request.isCancel()) {
                return null;
            }

            try {
                String urlStr = request.getUrl();
                // TODO: 2016/2/1 MalformedURLException - if spec could not be parsed as a URL.
                URL url = new URL(urlStr);
                // TODO: 2016/2/1 IOException - if an error occurs while opening the connection.
                // not appear even though no network
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                // TODO  may throw IllegalStateException when connection is connected that we can't handle
                connection.setDoInput(true);
                if ("https".equals(url.getProtocol()) && mSslSocketFactory != null) {
                    ((HttpsURLConnection) connection).setSSLSocketFactory(mSslSocketFactory);
                }
                // TODO: 2016/2/1 ProtocolException - if this is called after connected, or the method is not supported by this HTTP implementation.
                // we call setDoInput() above since it will not block
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", mUserAgent);
                connection.setRequestProperty("connection", "close");
                if (request.getStartPos() != 0) {
                    connection.setRequestProperty("Range", "bytes=" + request.getStartPos() + "-");
                }
                connection.setConnectTimeout(DEFAULT_TIMEOUT_MS);
                connection.setReadTimeout(DEFAULT_TIMEOUT_MS);
                // TODO: 2016/2/1 IOException - if an error occurs while connecting to the resource.
                // TODO: 2016/2/1 may throw lots kind of exception when bad or no network
                connection.connect();

                UrlConnectionResponse hcr = new UrlConnectionResponse(connection);
                // TODO: 2016/2/1 IOException - if there is an IO error during the retrieval.
                // TODO: 2016/2/1 may throw lots kind of exception when bad or no network
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_MOVED_PERM || responseCode == HttpURLConnection.HTTP_MOVED_TEMP) {
                    request.setRedirectUrl(connection.getHeaderField("Location"));
                    continue;
                }
                if (responseCode != HttpURLConnection.HTTP_OK && responseCode != HttpURLConnection.HTTP_PARTIAL) {
                    // TODO: 2016/2/1 custom exception : illegal status code
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
                if (!isAcceptRange) {
                    request.setStartPos(0);
                }
                // parse content length
                String lengthStr = connection.getHeaderField("Content-Length");
                if (TextUtils.isEmpty(lengthStr)) {
                    hcr.mContentLength = 0;
                } else {
                    hcr.mContentLength = Long.parseLong(lengthStr);
                }
                hcr.mTotalLength = hcr.mContentLength;
                if (isAcceptRange) {
                    lengthStr = connection.getHeaderField("Content-Range");
                    if (!TextUtils.isEmpty(lengthStr)) {
                        hcr.mTotalLength = Long.parseLong(lengthStr.substring(lengthStr.lastIndexOf("/") + 1, lengthStr.length()));
                    }
                    hcr.mSupportRange = true;
                }
                // TODO: 2016/2/1 IOException - if no InputStream could be created.
                // not appear even though no network
                hcr.mContentStream = connection.getInputStream();
                return hcr;
            } catch (MalformedURLException e) {
                throw e;
            } catch (IOException e) {
                // TODO: 2016/2/1 time out handle this
                if (e instanceof UnknownHostException) {
                    throw new DownloadException(DownloadException.UNKNOWN_HOST, e);
                }
                if (e instanceof SocketException) {
                    throw new DownloadException(DownloadException.NO_CONNECTION, e);
                }
                throw new DownloadException(DownloadException.UNKNOWN, e);
            }
        }
    }
}
