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
        public int taskId;

        public DisPatched(int taskId) {
            this.taskId = taskId;
        }
    }

    public static class ReadLength {
        public int taskId;
        public long totalBytes;

        public ReadLength(int taskId, long totalBytes) {
            this.taskId = taskId;
            this.totalBytes = totalBytes;
        }
    }

    public static class Loading {
        public int taskId;
        public long currBytes;

        public Loading(int taskId, long currBytes) {
            this.taskId = taskId;
            this.currBytes = currBytes;
        }
    }

    public static class Finish {
        public int taskId;
        public DownloadResult result;

        public Finish(int taskId, DownloadResult result) {
            this.taskId = taskId;
            this.result = result;
        }
    }

    public static class Canceled {
        public int taskId;

        public Canceled(int taskId) {
            this.taskId = taskId;
        }
    }
}
