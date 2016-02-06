package com.example.vigi.androiddownload.core;

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

    public boolean isBadNetwork() {
        return mExceptionCode == UNKNOWN_HOST
                || mExceptionCode == NO_CONNECTION
                || mExceptionCode == SOCKET_TIMEOUT
//                || mExceptionCode == UNKNOWN_NETWORK     // TODO: 2016/2/3 to collect kinds of exception
                ;
    }

    public static final int EXCEPTION_CODE_PARSE = 101;

    /**
     * a substitute of {@link java.net.UnknownHostException}
     * <p>Also it might has no network</p>
     */
    public static final int UNKNOWN_HOST = 102;

    public static final int NO_CONNECTION = 103;

    public static final int LOCAL_IO = 104;

    public static final int BAD_URL = 105;

    /**
     * a substitute of {@link java.net.SocketTimeoutException}
     */
    public static final int SOCKET_TIMEOUT = 106;

    /**
     * a substitute of {@link InterruptedException}
     */
    public static final int THREAD_INTERRUPTED = 107;

    public static final int UNKNOWN_NETWORK = 500;

    /**
     * include all unexpected exceptions and we should specific it when it comes out
     */
    public static final int UNKNOWN = 999;

}
