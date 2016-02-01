package com.example.vigi.androiddownload.download;

/**
 * Created by Vigi on 2016/1/20.
 */
public class DownloadException extends Exception {
    private int mExceptionCode = 0;

    public DownloadException(int exceptionCode) {
        this.mExceptionCode = exceptionCode;
    }

    public DownloadException(int exceptionCode, String detailMessage) {
        super(detailMessage);
        this.mExceptionCode = exceptionCode;
    }

    public DownloadException(int exceptionCode, String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
        this.mExceptionCode = exceptionCode;
    }

    public DownloadException(int exceptionCode, Throwable throwable) {
        super(throwable);
        this.mExceptionCode = exceptionCode;
    }

    public int getExceptionCode() {
        return mExceptionCode;
    }

    public static final int EXCEPTION_CODE_PARSE = 101;

    /**
     * a substitute of {@link java.net.UnknownHostException}
     * <p>Also it might has no network</p>
     */
    public static final int EXCEPTION_CODE_UNKNOWN_HOST = 102;

    public static final int EXCEPTION_CODE_NO_CONNECTION = 103;

    /**
     * a unexpected exception and we should specific it when it comes out
     */
    public static final int EXCEPTION_CODE_UNKNOWN = 999;

}
