package com.nothing.demo;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.Display;
import android.view.Surface;

import com.nothing.commonutils.utils.ServiceUtils;

import androidx.annotation.Nullable;


public class ScreenService extends Service {
    private MediaProjectionManager mediaProjectionManager;

    private static final String TAG = "ScreenService";
    private static final String CHANNEL_ID = "sunlogin_sdk_channel";
    private static final String CHANNEL_NAME = "channel_mediaprojection";
    private static final int NOTIFICATION_ID = 1000;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ServiceUtils.bindNotification(1000, this, CHANNEL_ID, CHANNEL_NAME,
                                      R.drawable.icon_launcher, getString(R.string.app_name),
                                      "录屏");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getParcelableExtra("data") != null) {
            mediaProjectionManager = (MediaProjectionManager) getSystemService(
                    MEDIA_PROJECTION_SERVICE);
            MediaProjection mediaProjection = mediaProjectionManager.getMediaProjection(
                    Activity.RESULT_OK, intent.getParcelableExtra("data"));
            mediaProjection.registerCallback(new MediaProjection.Callback() {
                @Override
                public void onStop() {
                    super.onStop();
                    Log.d(TAG, " media onStop() called");
                }
            }, new Handler(Looper.getMainLooper()));
            SurfaceTexture surfaceTexture = new SurfaceTexture(false);
            surfaceTexture.setOnFrameAvailableListener(
                    new SurfaceTexture.OnFrameAvailableListener() {
                        @Override
                        public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                            Log.d(TAG, "onFrameAvailable() called with: surfaceTexture = [" +
                                       surfaceTexture + "]");
                        }
                    });
            Surface surface = new Surface(surfaceTexture);
            VirtualDisplay virtualDisplay;
            virtualDisplay = mediaProjection.createVirtualDisplay("test", 1920, 1080, 160,
                                                                  DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                                                                  surface,
                                                                  new VirtualDisplay.Callback() {
                                                                      @Override
                                                                      public void onPaused() {
                                                                          super.onPaused();
                                                                          Log.d(TAG,
                                                                                "VirtualDisplay onPaused() called");
                                                                      }

                                                                      @Override
                                                                      public void onResumed() {
                                                                          super.onResumed();
                                                                          Log.d(TAG,
                                                                                "VirtualDisplay onResumed() called");

                                                                      }

                                                                      @Override
                                                                      public void onStopped() {
                                                                          super.onStopped();
                                                                          Log.d(TAG,
                                                                                "VirtualDisplay onStopped() called");

                                                                      }
                                                                  }, new Handler(
                            Looper.getMainLooper()));

            Display display = virtualDisplay.getDisplay();
            System.out.println(display);
        }

        return super.onStartCommand(intent, flags, startId);
    }

}
