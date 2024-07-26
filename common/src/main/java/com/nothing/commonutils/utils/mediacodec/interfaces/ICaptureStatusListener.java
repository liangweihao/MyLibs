package com.nothing.commonutils.utils.mediacodec.interfaces;


public interface ICaptureStatusListener {
    public static final String FAIL_TO_CAPTURE_SCREEN = "Failed to capture screen.";
    public static final String GET_MEDIA_PROJECTION_ERROR = "Failed to get MediaProjection, please try to request MediaProjection permission again. null";
    public static final String PREPARE_START_CAPTURE_SCREEN = "Prepare start capture screen.";
    public static final int STATUS_CAPTURE_SCREEN_ERROR = 4;
    public static final int STATUS_CREATE_ENCODER_ERROR = 3;
    public static final int STATUS_CREATE_PROJECTION_ERROR = 1;
    public static final int STATUS_NETWORK_BLOCKING = 5;
    public static final int STATUS_NETWORK_FRAMES = 6;
    public static final int STATUS_REMOTE_CONNECTED = 0;
    public static final int STATUS_REQUEST_PERMISSION_ERROR = 2;

    void onCaptureStatusChanged(int i, String str);
}
