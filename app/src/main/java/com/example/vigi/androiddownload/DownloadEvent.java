package com.example.vigi.androiddownload;

import com.example.vigi.androiddownload.core.DownloadRequest;
import com.example.vigi.androiddownload.core.DownloadResult;

/**
 * Created by Vigi on 2016/2/5.
 */
public class DownloadEvent {
    public static class Create {
        public DownloadRequest request;

        public Create(DownloadRequest request) {
            this.request = request;
        }
    }
    public static class DisPatched {
        public DownloadRequest request;

        public DisPatched(DownloadRequest request) {
            this.request = request;
        }
    }

    public static class ReadLength {
        public DownloadRequest request;
        public long totalBytes;

        public ReadLength(DownloadRequest request, long totalBytes) {
            this.request = request;
            this.totalBytes = totalBytes;
        }
    }

    public static class Loading {
        public DownloadRequest request;
        public long currBytes;

        public Loading(DownloadRequest request, long currBytes) {
            this.request = request;
            this.currBytes = currBytes;
        }
    }

    public static class Finish {
        public DownloadRequest request;
        public DownloadResult result;

        public Finish(DownloadRequest request, DownloadResult result) {
            this.request = request;
            this.result = result;
        }
    }

    public static class Canceled {
        public DownloadRequest request;

        public Canceled(DownloadRequest request) {
            this.request = request;
        }
    }
}
