package com.inair.inaircommon;


import android.annotation.SuppressLint;
import android.app.Application;
import android.hardware.HardwareBuffer;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;

import com.nothing.commonutils.utils.Lg;
import com.nothing.commonutils.utils.RefInvoke;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


@Keep
public class DpManagerProxy {


    public static final String CLASS_CHANNEL_DATA = "android.service.duoping.ChannelData";
    public static final String CLASS_CHANNEL_DATA_KEYS = "android.service.duoping.ChannelDataKeys";
    public static final String CLASS_CHANNEL_ACTION = "android.service.duoping.ChannelAction";
    public static final String CLASS_DPMANAGER = "android.duoping.DpManager";
    public static final String CLASS_DpChannelType = "android.service.duoping.DpChannelType";
    public static final String CLASS_DP_MANAGER_LISTENER = "android.duoping.IDpMangerListener";
    public static Application globalApplication;


    public static int getConstImagePreview() {
        Object imagePreview = RefInvoke.getStaticFieldObject(CLASS_CHANNEL_ACTION, "IMAGE_PREVIEW");
        if (imagePreview != null) {
            return (int) imagePreview;
        }
        return 1020;
    }

    public static long getConstTypeImageDisplay() {
        Object imagePreview = RefInvoke.getStaticFieldObject(CLASS_DpChannelType,
                "TYPE_IMAGE_DISPLAY");
        if (imagePreview != null) {
            return (long) imagePreview;
        }
        return 1 << 5;
    }

    public static String getConstActionImageOpen() {
        Object imagePreview = RefInvoke.getStaticFieldObject(CLASS_CHANNEL_DATA_KEYS,
                "IMAGE_OPEN");
        if (imagePreview != null) {
            return (String) imagePreview;
        }
        return "image_open";
    }

    public static String getConstActionImageRequestAlloc() {
        Object imagePreview = RefInvoke.getStaticFieldObject(CLASS_CHANNEL_DATA_KEYS,
                "IMAGE_REQUEST_SHARE_ALLOC");
        if (imagePreview != null) {
            return (String) imagePreview;
        }
        return "image_request_share_alloc";
    }

    public static String getConstActionImageAlloc() {
        Object imagePreview = RefInvoke.getStaticFieldObject(CLASS_CHANNEL_DATA_KEYS,
                "IMAGE_SHARE_ALLOC");
        if (imagePreview != null) {
            return (String) imagePreview;
        }
        return "image_share_alloc";
    }

    public static String getConstAction() {
        Object imagePreview = RefInvoke.getStaticFieldObject(CLASS_CHANNEL_DATA_KEYS, "ACTION");
        if (imagePreview != null) {
            return (String) imagePreview;
        }
        return "action";
    }

    public static String getConstDisplayID() {
        Object imagePreview = RefInvoke.getStaticFieldObject(CLASS_CHANNEL_DATA_KEYS, "DISPLAY_ID");
        if (imagePreview != null) {
            return (String) imagePreview;
        }
        return "display_id";
    }

    public static String getConstMaterialType() {
        Object imagePreview = RefInvoke.getStaticFieldObject(CLASS_CHANNEL_DATA_KEYS, "DISPLAY_ID");
        if (imagePreview != null) {
            return (String) imagePreview;
        }
        return "material_type";
    }



    public static String getConstActionImageCreate() {
        Object imagePreview = RefInvoke.getStaticFieldObject(CLASS_CHANNEL_DATA_KEYS,
                "IMAGE_CREATE");
        if (imagePreview != null) {
            return (String) imagePreview;
        }
        return "image_create";
    }

    public static String getConstActionImageDestroy() {
        Object imagePreview = RefInvoke.getStaticFieldObject(CLASS_CHANNEL_DATA_KEYS,
                "IMAGE_DESTROY");
        if (imagePreview != null) {
            return (String) imagePreview;
        }
        return "image_destroy";
    }

    public static String getConstActionImageOpenPre() {
        Object imagePreview = RefInvoke.getStaticFieldObject(CLASS_CHANNEL_DATA_KEYS,
                "IMAGE_OPEN_PRE");
        if (imagePreview != null) {
            return (String) imagePreview;
        }
        return "image_open_pre";
    }

    public static String getConstActionImageNext() {
        Object imagePreview = RefInvoke.getStaticFieldObject(CLASS_CHANNEL_DATA_KEYS,
                "IMAGE_OPEN_NEXT");
        if (imagePreview != null) {
            return (String) imagePreview;
        }
        return "image_open_next";
    }

    public static String getConstImageModelCreate() {
        Object imagePreview = RefInvoke.getStaticFieldObject(CLASS_CHANNEL_DATA_KEYS,
                "IMAGE_MODEL_CREATE");
        if (imagePreview != null) {
            return (String) imagePreview;
        }
        return "image_model_create";
    }

    public static String getConstImageModelMaterialType() {
        Object imagePreview = RefInvoke.getStaticFieldObject(CLASS_CHANNEL_DATA_KEYS,
                "IMAGE_MODEL_MATERIAL_TYPE");
        if (imagePreview != null) {
            return (String) imagePreview;
        }
        return "image_model_material_type";
    }

    public static String getConstImageModelDestroy() {
        Object imagePreview = RefInvoke.getStaticFieldObject(CLASS_CHANNEL_DATA_KEYS,
                "IMAGE_MODEL_DESTROY");
        if (imagePreview != null) {
            return (String) imagePreview;
        }
        return "image_model_destroy";
    }

    public static String getConstImageModelOpen() {
        Object imagePreview = RefInvoke.getStaticFieldObject(CLASS_CHANNEL_DATA_KEYS,
                "IMAGE_MODEL_OPEN");
        if (imagePreview != null) {
            return (String) imagePreview;
        }
        return "image_model_open";
    }

    public static String getConstImageModelEvent() {
        Object imagePreview = RefInvoke.getStaticFieldObject(CLASS_CHANNEL_DATA_KEYS,
                "IMAGE_MODEL_EVENT");
        if (imagePreview != null) {
            return (String) imagePreview;
        }
        return "image_model_event";
    }

    public static String getConstDataKeysDATA() {
        Object imagePreview = RefInvoke.getStaticFieldObject(CLASS_CHANNEL_DATA_KEYS, "DATA");
        if (imagePreview != null) {
            return (String) imagePreview;
        }
        return "data";
    }

    public static String getConstDataKeysMotionEvent() {
        Object imagePreview = RefInvoke.getStaticFieldObject(CLASS_CHANNEL_DATA_KEYS, "MOTION_EVENT");
        if (imagePreview != null) {
            return (String) imagePreview;
        }
        return "motion_event";
    }


    public static String getConstDataKeysHardwareBuffer() {
        Object imagePreview = RefInvoke.getStaticFieldObject(CLASS_CHANNEL_DATA_KEYS, "HARDWARE_BUFFER");
        if (imagePreview != null) {
            return (String) imagePreview;
        }
        return "hardware_buffer";
    }

    public static String getConstDataKeysDATATYPE() {
        Object imagePreview = RefInvoke.getStaticFieldObject(CLASS_CHANNEL_DATA_KEYS, "DATA_TYPE");
        if (imagePreview != null) {
            return (String) imagePreview;
        }
        return "data_type";
    }

    public static String getConstDataKeysResolutionWidth() {
        Object imagePreview = RefInvoke.getStaticFieldObject(CLASS_CHANNEL_DATA_KEYS,
                "RESOLUTION_WIDTH");
        if (imagePreview != null) {
            return (String) imagePreview;
        }
        return "resolution_width";
    }

    public static String getConstDataKeysResolutionHeight() {
        Object imagePreview = RefInvoke.getStaticFieldObject(CLASS_CHANNEL_DATA_KEYS,
                "RESOLUTION_HEIGHT");
        if (imagePreview != null) {
            return (String) imagePreview;
        }
        return "resolution_height";
    }

    @Nullable
    private static Object createChannelDataInstance() {
        return RefInvoke.invokeStaticMethod(CLASS_CHANNEL_DATA, "createInstance", null, null);
    }


    @Keep
    public static class IDpMangerListenerProxy {

        private Object proxyObj;

        /**
         * type : getConstImageDisplay 对应的渲染类型
         */
        @Keep
        public void onEventChanged(long type, byte[] data) {

        }

        @Keep
        public void onEventForParcelableChanged(long type, @NonNull Object channelData) {

        }

        @Keep
        public IBinder asBinder() {
            return null;
        }
    }


    @Nullable
    private static Object proxyDPManagerListener(IDpMangerListenerProxy proxy) {
        Pair<Class<?>, Object> instance = RefInvoke.proxyTargetClassInstance(
                CLASS_DP_MANAGER_LISTENER,
                new RefInvoke.InvokeHandler(proxy));
        if (instance == null) {
            return null;
        }
        proxy.proxyObj = instance.second;
        return instance.second;
    }


    public static boolean addDpImageDisplayManagerListener(IDpMangerListenerProxy listener) {
        RefInvoke.setFieldObject(getDpManagerInstance().getClass().getName(),getDpManagerInstance(),"mRegistedDpMangerCallback",false);
        Object dpServiceInstance = getDpManagerInstance();
        if (dpServiceInstance != null) {
            try {
                return (boolean) RefInvoke.invokeInstanceMethod(dpServiceInstance,
                        "addDpManagerListener",
                        new Class[]{long.class, Class.forName(CLASS_DP_MANAGER_LISTENER)},
                        new Object[]{
                                ((long) getConstTypeImageDisplay()),
                                RefInvoke.proxyTargetClassInstance(CLASS_DP_MANAGER_LISTENER,new RefInvoke.InvokeHandler(listener){
                                    @Override
                                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                                        try {
                                            listener.proxyObj = proxy;
                                            for (Method me : listener.getClass().getMethods()) {
                                                if (me.getName().equals(method.getName())) {
                                                    return me.invoke(listener, args);
                                                }
                                            }
                                        } catch (Exception var5) {
                                            Exception e = var5;
                                            e.printStackTrace();
                                        }
                                        return method.invoke(listener, args);
                                    }
                                }).second
                        });
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static void removeDpImageDisplayManagerListener(IDpMangerListenerProxy listener) {
        Object dpServiceInstance = getDpManagerInstance();
        if (dpServiceInstance != null) {
            try {
                RefInvoke.invokeInstanceMethod(dpServiceInstance,
                        "removeDpManagerListener",
                        new Class[]{long.class, Class.forName(CLASS_DP_MANAGER_LISTENER)},
                        new Object[]{
                                ((long) getConstTypeImageDisplay()), listener.proxyObj
                        });
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    private static void setAction(@Nullable Object obj, int action) {
        if (obj != null) {
            RefInvoke.invokeInstanceMethod(obj,
                    "setAction",
                    new Class[]{int.class},
                    new Object[]{action});
        }
    }

    private static int getAction(@Nullable Object obj) {
        if (obj != null) {
            return (int) RefInvoke.invokeInstanceMethod(obj,
                    "getAction",
                    new Class[]{},
                    new Object[]{});
        }
        return -1;
    }


    private static void putString(@Nullable Object obj, String action) {
        if (obj != null) {
            RefInvoke.invokeInstanceMethod(obj,
                    "putString",
                    new Class[]{String.class},
                    new Object[]{action});
        }
    }

    private static void putInt(@Nullable Object obj, int action) {
        if (obj != null) {
            RefInvoke.invokeInstanceMethod(obj,
                    "putInt",
                    new Class[]{int.class},
                    new Object[]{action});
        }
    }

    private static void setBundle(@Nullable Object obj, Bundle action) {
        if (obj != null) {
            RefInvoke.invokeInstanceMethod(obj,
                    "setBundle",
                    new Class[]{Bundle.class},
                    new Object[]{action});
        }
    }


    @NonNull
    private static Bundle getBundle(@Nullable Object obj) {
        if (obj != null) {
            return (Bundle) RefInvoke.invokeInstanceMethod(obj,
                    "getBundle",
                    new Class[]{},
                    new Object[]{});
        }
        return new Bundle();
    }



    @SuppressLint("WrongConstant")
    @Nullable
    private static Object getDpManagerInstance() {
        if (globalApplication == null){
            return null;
        }
        return globalApplication.getSystemService("dp");
    }

    private static void writeChannel(long var1, Object channelData) {
        Object getService = getDpManagerInstance();
        if (getService != null) {
            RefInvoke.invokeInstanceMethod(getService,
                    "writeChannel",
                    new Class[]{long.class, RefInvoke.getClass(CLASS_CHANNEL_DATA)},
                    new Object[]{var1, channelData});
            Lg.d(TAG, "writeChannel() called with: var1 = [" + var1 + "], channelData = [" + channelData + "]");
        }
    }

    private static final String TAG = "DpManagerProxy";

    public static void createImagePreviewCreate(int displayId) {
        try {

            Object channelData = createChannelDataInstance();
            setAction(channelData, getConstImagePreview());
            Bundle bundle = new Bundle();
            bundle.putString(getConstAction(), getConstActionImageCreate());
            bundle.putInt(getConstDisplayID(), displayId);
            setBundle(channelData, bundle);
            writeChannel(getConstTypeImageDisplay(), channelData);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, " " + e);
        }
    }


    public static void createImageModelCreate(int displayId) {
        try {

            Object channelData = createChannelDataInstance();
            setAction(channelData, getConstImagePreview());
            Bundle bundle = new Bundle();
            bundle.putString(getConstAction(), getConstImageModelCreate());
            bundle.putInt(getConstDisplayID(), displayId);
            setBundle(channelData, bundle);
            writeChannel(getConstTypeImageDisplay(), channelData);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, " " + e);
        }
    }

    public static void createImageModelMaterialType(int displayId,Uri uri) {
        try {

            Object channelData = createChannelDataInstance();
            setAction(channelData, getConstImagePreview());
            Bundle bundle = new Bundle();
            bundle.putString(getConstAction(), getConstImageModelMaterialType());
            bundle.putInt(getConstDisplayID(), displayId);
            bundle.putParcelable(getConstDataKeysDATA(), uri);
            setBundle(channelData, bundle);
            writeChannel(getConstTypeImageDisplay(), channelData);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, " " + e);
        }
    }


    public static void createImageModelDestroy(int displayId) {
        try {

            Object channelData = createChannelDataInstance();
            setAction(channelData, getConstImagePreview());
            Bundle bundle = new Bundle();
            bundle.putString(getConstAction(), getConstImageModelDestroy());
            bundle.putInt(getConstDisplayID(), displayId);
            setBundle(channelData, bundle);
            writeChannel(getConstTypeImageDisplay(), channelData);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, " " + e);
        }
    }


    public static void createImageModelOpen(int displayId, Uri uri) {
        try {
            Object channelData = createChannelDataInstance();
            setAction(channelData, getConstImagePreview());
            Bundle bundle = new Bundle();
            bundle.putString(getConstAction(), getConstImageModelOpen());
            bundle.putInt(getConstDisplayID(), displayId);
            bundle.putParcelable(getConstDataKeysDATA(), uri);
            setBundle(channelData, bundle);
            writeChannel(getConstTypeImageDisplay(), channelData);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, " " + e);
        }
    }

    public static void createImageModelMotionEvent(int displayId, MotionEvent event) {
        try {
            Object channelData = createChannelDataInstance();
            setAction(channelData, getConstImagePreview());
            Bundle bundle = new Bundle();
            bundle.putString(getConstAction(), getConstImageModelEvent());
            bundle.putInt(getConstDisplayID(), displayId);
            bundle.putParcelable(getConstDataKeysMotionEvent(), event);
            setBundle(channelData, bundle);
            writeChannel(getConstTypeImageDisplay(), channelData);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, " " + e);
        }
    }

    public static void createImagePreviewDestroy(int displayID) {
        try {
            Object channelData = createChannelDataInstance();
            setAction(channelData, getConstImagePreview());
            Bundle bundle = new Bundle();
            bundle.putString(getConstAction(), getConstActionImageDestroy());
            bundle.putInt(getConstDisplayID(), displayID);
            setBundle(channelData, bundle);
            writeChannel(getConstTypeImageDisplay(), channelData);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, " " + e);
        }
    }

    public static void createImagePreviewOpen(HardwareBuffer buffer, int dataType) {
        try {
            Object channelData = createChannelDataInstance();
            setAction(channelData, getConstImagePreview());
            Bundle bundle = new Bundle();
            bundle.putString(getConstAction(), getConstActionImageOpen());
            bundle.putParcelable(getConstDataKeysHardwareBuffer(), buffer);
            bundle.putInt(getConstDataKeysDATATYPE(), dataType);
            setBundle(channelData, bundle);
            writeChannel(getConstTypeImageDisplay(), channelData);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, " " + e);
        }
    }

    public static void createImageRequestBuffer(int width, int height) {
        try {
            Object channelData = createChannelDataInstance();
            setAction(channelData, getConstImagePreview());
            Bundle bundle = new Bundle();
            bundle.putString(getConstAction(), getConstActionImageRequestAlloc());
            bundle.putInt(getConstDataKeysResolutionWidth(), width);
            bundle.putInt(getConstDataKeysResolutionHeight(), height);
            setBundle(channelData, bundle);
            Lg.d(TAG, "请求创建 IMAGE_REQUEST_SHARE_ALLOC buffer " + width + ":" + height);
            writeChannel(getConstTypeImageDisplay(), channelData);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, " " + e);
        }
    }


    @Nullable
    public static HardwareBuffer getBundleHardwareBuffer(Bundle bundle) {
        Parcelable parcelable = bundle.getParcelable(getConstDataKeysHardwareBuffer());
        if (parcelable instanceof HardwareBuffer) {
            return ((HardwareBuffer) parcelable);
        }
        return null;
    }

    @Nullable
    public static Bundle hasImageAllocBundle(Object readChannelData) {
        int action = getAction(readChannelData);
        if (action == getConstImagePreview()) {
            Bundle bundle = getBundle(readChannelData);
            return bundle.getString(getConstAction(), "").equals(getConstActionImageAlloc()) ? bundle : null;
        }
        return null;
    }

    @NotNull
    public static boolean isImagePreviewPre(Object readChannelData) {
        int action = getAction(readChannelData);
        if (action == getConstImagePreview()) {
            Bundle bundle = getBundle(readChannelData);
            return bundle.getString(getConstAction(), "").equals(getConstActionImageOpenPre());
        }
        return false;
    }

    @NotNull
    public static boolean isImagePreviewNext(Object readChannelData) {
        int action = getAction(readChannelData);
        if (action == getConstImagePreview()) {
            Bundle bundle = getBundle(readChannelData);
            return bundle.getString(getConstAction(), "").equals(getConstActionImageNext());
        }
        return false;
    }

    public static void createImagePreviewPre(int displayID) {
        try {
            Object channelData = createChannelDataInstance();
            setAction(channelData, getConstImagePreview());
            Bundle bundle = new Bundle();
            bundle.putString(getConstAction(), getConstActionImageOpenPre());
            bundle.putInt(getConstDisplayID(), displayID);
            setBundle(channelData, bundle);
            writeChannel(getConstTypeImageDisplay(), channelData);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, " " + e);
        }
    }


    public static void createImagePreviewNext(int displayID) {
        try {
            Object channelData = createChannelDataInstance();
            setAction(channelData, getConstImagePreview());
            Bundle bundle = new Bundle();
            bundle.putString(getConstAction(), getConstActionImageNext());
            bundle.putInt(getConstDisplayID(), displayID);
            setBundle(channelData, bundle);
            writeChannel(getConstTypeImageDisplay(), channelData);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, " " + e);
        }
    }

}
