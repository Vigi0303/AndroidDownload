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

    @Override
    public HttpResponse performDownloadRequest(DownloadRequest downloadRequest) {
        while (true) {
            if (downloadRequest.isCancel()) {
                return null;
            }

            HttpURLConnection connection = null;
            try {
                String urlStr = downloadRequest.getUrl();
                URL url = new URL(urlStr);
                if (!"http".equals(url.getProtocol())) {
                    throw new MalformedURLException("url(" + url + ") must be http url!");
                }
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Range", "bytes=" + downloadRequest.getStartPos() + "-");

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_MOVED_PERM || responseCode == HttpURLConnection.HTTP_MOVED_TEMP) {
                    downloadRequest.setRedirectUrl(connection.getHeaderField("Location"));
                    continue;
                }
                if (responseCode != HttpURLConnection.HTTP_OK && responseCode != HttpURLConnection.HTTP_PARTIAL) {
                    return null;
                }
                HttpResponse hr = new HttpResponse();
                String lengthStr = connection.getHeaderField("Content-Length");
                if (TextUtils.isEmpty(lengthStr)) {
                    hr.mTotalLength = 0;
                } else {
                    hr.mTotalLength = Long.parseLong(lengthStr);
                }
                hr.mContentStream = connection.getInputStream();
                return hr;
            } catch (IOException e) {
                return null;
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }
    }
}
