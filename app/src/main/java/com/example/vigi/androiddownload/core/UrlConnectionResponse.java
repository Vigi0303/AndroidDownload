package com.example.vigi.androiddownload.core;

import java.net.HttpURLConnection;

public class UrlConnectionResponse extends NetWorkResponse {
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