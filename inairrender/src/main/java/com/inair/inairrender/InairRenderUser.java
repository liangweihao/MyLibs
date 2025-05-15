package com.inair.inairrender;

import android.app.Application;
import android.content.Context;
import android.graphics.Rect;
import android.hardware.HardwareBuffer;
import android.hardware.display.DisplayManager;
import android.opengl.GLES20;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.inair.inairsharetexture.SharedTexture;


public class InairRenderUser {
    private HardwareBuffer hardwareBuffer;
    private SharedTexture sharedTexture;
    private static final String TAG = "InairRenderUser";
    private final DpManagerProxy.IDpMangerListenerProxy dpMangerListenerProxy =
            new DpManagerProxy.IDpMangerListenerProxy() {
                @Override
                public void onEventForParcelableChanged(long type, Object channelData) {
                    super.onEventForParcelableChanged(type, channelData);
                    Bundle bundle = DpManagerProxy.hasImageAllocBundle(
                            channelData,
                            windowDisplayID,
                            contextPackageName
                    );
                    if (bundle != null) {
                        hardwareBuffer = DpManagerProxy.getBundleHardwareBuffer(bundle);
                        if (displayPosition != null) {
                            updateDisplay(displayPosition,isTop);
                        }
                        new Thread(() -> {
                            if (renderCallBack != null && !isDestory) {
                                renderCallBack.onRenderCreate();
                            }
                        }).start();
                    }
                }
            };
    private RenderCallBack renderCallBack;
    private Rect displayPosition;
    private boolean isTop;


    private int windowDisplayID(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && context.getDisplay() != null) {
            return context.getDisplay().getDisplayId();
        } else {
            DisplayManager systemService =
                    (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
            return systemService.getDisplays()[0].getDisplayId();
        }
    }

    private int windowDisplayID;
    private String contextPackageName;

    public void onCreate(Context context) {
        isDestory = false;
        isShow = false;
        windowDisplayID = windowDisplayID(context);
        contextPackageName = context.getPackageName();
        DpManagerProxy.globalApplication = (Application) context.getApplicationContext();
        DpManagerProxy.addDpImageDisplayManagerListener(dpMangerListenerProxy);
        DpManagerProxy.createImagePreviewCreate(windowDisplayID, contextPackageName);
    }

    public void initSourceSize(
            int videoBufferWidth, int videoBufferHeight, RenderCallBack renderCallBack
    ) {
        this.renderCallBack = renderCallBack;
        Log.d(
                TAG,
                "initSourceSize with: videoBufferWidth = [" + videoBufferWidth + "], videoBufferHeight = [" + videoBufferHeight + "]"
        );
        DpManagerProxy.createImageRequestBuffer(
                videoBufferWidth,
                videoBufferHeight,
                windowDisplayID,
                contextPackageName
        );
    }

    public void bindTextureId(int glTextureId) {
        if (hardwareBuffer != null) {
            sharedTexture = new SharedTexture(hardwareBuffer);
            sharedTexture.bindTexture(glTextureId, GLES20.GL_TEXTURE_2D);
        } else {
            Log.e(
                    TAG,
                    "Please initialize the source size by calling the initSourceSize method before proceeding."
            );
        }
    }


    public void updateDisplay(Rect displayPosition,boolean isTop) {
        Log.i(TAG, "updateDisplay " + displayPosition);
        if (hardwareBuffer != null) {
            DpManagerProxy.createImagePreviewOpen(
                    hardwareBuffer,
                    displayPosition,
                    windowDisplayID,
                    isTop,
                    DpManagerProxy.DataType.LR_3D,
                    contextPackageName
            );
        } else {
            this.displayPosition = displayPosition;
            this.isTop = isTop;
            Log.e(
                    TAG,
                    "Please initialize the source size by calling the initSourceSize method before proceeding."
            );
        }
    }

    private boolean isShow = false;

    public void show() {
        isShow = true;
        DpManagerProxy.createImagePreviewShow(windowDisplayID, contextPackageName);
    }


    public void hide() {
        isShow = false;
        DpManagerProxy.createImagePreviewHide(windowDisplayID, contextPackageName);
    }
    private boolean isDestory = false;

    public void onDestroy() {
        isDestory = true;
        renderCallBack = null;
        DpManagerProxy.createImagePreviewDestroy(windowDisplayID, contextPackageName);
        DpManagerProxy.removeDpImageDisplayManagerListener(dpMangerListenerProxy);
        if (sharedTexture != null) {
            sharedTexture.release();
        }
    }

}
