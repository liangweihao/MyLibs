package com.inair.inairrender;


import android.annotation.SuppressLint;
import android.app.Application;
import android.graphics.Rect;
import android.hardware.HardwareBuffer;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static com.inair.ref.RefInvoke.InvokeHandler;
import static com.inair.ref.RefInvoke.getStaticFieldObject;
import static com.inair.ref.RefInvoke.invokeInstanceMethod;
import static com.inair.ref.RefInvoke.invokeStaticMethod;
import static com.inair.ref.RefInvoke.proxyTargetClassInstance;
import static com.inair.ref.RefInvoke.setFieldObject;

@Keep
public class DpManagerProxy {

    private final static String LG_Package = "com.nothing.commonutils.utils.Lg";
    private final static String GSON_Package = "com.nothing.commonutils.utils.GsonUtils";

    public static final String CLASS_CHANNEL_DATA = "android.service.duoping.ChannelData";
    public static final String CLASS_CHANNEL_DATA_KEYS = "android.service.duoping.ChannelDataKeys";
    public static final String CLASS_CHANNEL_ACTION = "android.service.duoping.ChannelAction";
    public static final String CLASS_DPMANAGER = "android.duoping.DpManager";
    public static final String CLASS_DpChannelType = "android.service.duoping.DpChannelType";
    public static final String CLASS_DP_MANAGER_LISTENER = "android.duoping.IDpMangerListener";
    public static Application globalApplication;

    public enum DataType {
        NORMAL(0), LR_3D(1), NORMAL_WIDE(2),TB_3D(3),LR_3D_WIDE(4),TB_3D_WIDE(5),DEEP_ANYTHING_CONVERT(6);

        public int type;

        DataType(int type) {
            this.type = type;
        }


        public static DataType getDataType(int type) {
            switch (type) {
                case 1:
                    return LR_3D;
                case 2:
                    return NORMAL_WIDE;
                case 3:
                    return TB_3D;
                case 4:
                    return LR_3D_WIDE;
                case 5:
                    return TB_3D_WIDE;
                case 6:
                    return DEEP_ANYTHING_CONVERT;
                default:
                    return NORMAL;
            }
        }
    }

    public static int getConstImagePreview() {
        Object imagePreview = getStaticFieldObject(CLASS_CHANNEL_ACTION, "IMAGE_PREVIEW");
        if (imagePreview != null) {
            return (int) imagePreview;
        }
        return 1020;
    }

    public static int getConstUnityAction() {
        Object imagePreview = getStaticFieldObject(CLASS_CHANNEL_ACTION, "UNITY_ACTION");
        if (imagePreview != null) {
            return (int) imagePreview;
        }
        return 1999;
    }

    public static int getConstInairSpaceWindowResize() {
        Object imagePreview =
                getStaticFieldObject(CLASS_CHANNEL_ACTION, "INAIR_SPACE_WINDOW_RESIZE");
        if (imagePreview != null) {
            return (int) imagePreview;
        }
        return 1027;
    }

    public static long getConstTypeImageDisplay() {
        Object imagePreview = getStaticFieldObject(
                CLASS_DpChannelType,
                "TYPE_IMAGE_DISPLAY"
        );
        if (imagePreview != null) {
            return (long) imagePreview;
        }
        return 1 << 5;
    }

    public static int getConstTypeDisplay() {
        return 16;
    }


    public static String getConstActionImageOpen() {
        Object imagePreview = getStaticFieldObject(CLASS_CHANNEL_DATA_KEYS, "IMAGE_OPEN");
        if (imagePreview != null) {
            return (String) imagePreview;
        }
        return "image_open";
    }

    public static String getConstActionImageRequestAlloc() {
        Object imagePreview = getStaticFieldObject(
                CLASS_CHANNEL_DATA_KEYS,
                "IMAGE_REQUEST_SHARE_ALLOC"
        );
        if (imagePreview != null) {
            return (String) imagePreview;
        }
        return "image_request_share_alloc";
    }

    public static String getConstActionImageAlloc() {
        Object imagePreview = getStaticFieldObject(
                CLASS_CHANNEL_DATA_KEYS,
                "IMAGE_SHARE_ALLOC"
        );
        if (imagePreview != null) {
            return (String) imagePreview;
        }
        return "image_share_alloc";
    }

    public static String getConstAction() {
        Object imagePreview = getStaticFieldObject(CLASS_CHANNEL_DATA_KEYS, "ACTION");
        if (imagePreview != null) {
            return (String) imagePreview;
        }
        return "action";
    }

    public static String getConstDisplayID() {
        Object imagePreview = getStaticFieldObject(CLASS_CHANNEL_DATA_KEYS, "DISPLAY_ID");
        if (imagePreview != null) {
            return (String) imagePreview;
        }
        return "display_id";
    }

    public static String getConstPackageName() {
        Object imagePreview =
                getStaticFieldObject(CLASS_CHANNEL_DATA_KEYS, "PACKAGE_NAME");
        if (imagePreview != null) {
            return (String) imagePreview;
        }
        return "package_name";
    }



    public static String getConstMaterialType() {
        Object imagePreview = getStaticFieldObject(CLASS_CHANNEL_DATA_KEYS, "DISPLAY_ID");
        if (imagePreview != null) {
            return (String) imagePreview;
        }
        return "material_type";
    }


    public static String getConstActionImageCreate() {
        Object imagePreview = getStaticFieldObject(
                CLASS_CHANNEL_DATA_KEYS,
                "IMAGE_CREATE"
        );
        if (imagePreview != null) {
            return (String) imagePreview;
        }
        return "image_create";
    }

    public static String getConstActionImageDestroy() {
        Object imagePreview = getStaticFieldObject(
                CLASS_CHANNEL_DATA_KEYS,
                "IMAGE_DESTROY"
        );
        if (imagePreview != null) {
            return (String) imagePreview;
        }
        return "image_destroy";
    }


    public static String getConstActionImageClearAll() {
        return "image_clear_all";
    }

    public static String getConstActionImageOpenPre() {
        Object imagePreview = getStaticFieldObject(
                CLASS_CHANNEL_DATA_KEYS,
                "IMAGE_OPEN_PRE"
        );
        if (imagePreview != null) {
            return (String) imagePreview;
        }
        return "image_open_pre";
    }

    public static String getConstActionImageNext() {
        Object imagePreview = getStaticFieldObject(
                CLASS_CHANNEL_DATA_KEYS,
                "IMAGE_OPEN_NEXT"
        );
        if (imagePreview != null) {
            return (String) imagePreview;
        }
        return "image_open_next";
    }

    public static String getConstImageModelCreate() {
        Object imagePreview = getStaticFieldObject(
                CLASS_CHANNEL_DATA_KEYS,
                "IMAGE_MODEL_CREATE"
        );
        if (imagePreview != null) {
            return (String) imagePreview;
        }
        return "image_model_create";
    }

    public static String getConstImageModelMaterialType() {
        Object imagePreview = getStaticFieldObject(
                CLASS_CHANNEL_DATA_KEYS,
                "IMAGE_MODEL_MATERIAL_TYPE"
        );
        if (imagePreview != null) {
            return (String) imagePreview;
        }
        return "image_model_material_type";
    }

    public static String getConstImageModelDestroy() {
        Object imagePreview = getStaticFieldObject(
                CLASS_CHANNEL_DATA_KEYS,
                "IMAGE_MODEL_DESTROY"
        );
        if (imagePreview != null) {
            return (String) imagePreview;
        }
        return "image_model_destroy";
    }

    public static String getConstImageModelOpen() {
        Object imagePreview = getStaticFieldObject(
                CLASS_CHANNEL_DATA_KEYS,
                "IMAGE_MODEL_OPEN"
        );
        if (imagePreview != null) {
            return (String) imagePreview;
        }
        return "image_model_open";
    }

    public static String getConstImageModelEvent() {
        Object imagePreview = getStaticFieldObject(
                CLASS_CHANNEL_DATA_KEYS,
                "IMAGE_MODEL_EVENT"
        );
        if (imagePreview != null) {
            return (String) imagePreview;
        }
        return "image_model_event";
    }

    public static String getConstDataKeysDATA() {
        Object imagePreview = getStaticFieldObject(CLASS_CHANNEL_DATA_KEYS, "DATA");
        if (imagePreview != null) {
            return (String) imagePreview;
        }
        return "data";
    }

    public static String getConstDataKeysMotionEvent() {
        Object imagePreview = getStaticFieldObject(
                CLASS_CHANNEL_DATA_KEYS,
                "MOTION_EVENT"
        );
        if (imagePreview != null) {
            return (String) imagePreview;
        }
        return "motion_event";
    }


    public static String getConstDataKeysHardwareBuffer() {
        Object imagePreview = getStaticFieldObject(
                CLASS_CHANNEL_DATA_KEYS,
                "HARDWARE_BUFFER"
        );
        if (imagePreview != null) {
            return (String) imagePreview;
        }
        return "hardware_buffer";
    }

    public static String getConstDataKeysDATATYPE() {
        Object imagePreview = getStaticFieldObject(CLASS_CHANNEL_DATA_KEYS, "DATA_TYPE");
        if (imagePreview != null) {
            return (String) imagePreview;
        }
        return "data_type";
    }
    public static String getConstDataKeysREQUESTACTION() {
        return "REQUEST_ACTION";
    }


    public static String getConstDataKeysREQUESTFAIL() {
        return "REQUEST_FAIL";
    }

    public static String getConstDataKeysACTIVE() {
        return "ACTIVE";
    }

    public static String getConstUnityActive() {
        return "UNITY_ACTIVE";
    }

    public static String getConstDataKeysResolutionWidth() {
        Object imagePreview = getStaticFieldObject(
                CLASS_CHANNEL_DATA_KEYS,
                "RESOLUTION_WIDTH"
        );
        if (imagePreview != null) {
            return (String) imagePreview;
        }
        return "resolution_width";
    }

    public static String getConstDataKeysResolutionHeight() {
        Object imagePreview = getStaticFieldObject(
                CLASS_CHANNEL_DATA_KEYS,
                "RESOLUTION_HEIGHT"
        );
        if (imagePreview != null) {
            return (String) imagePreview;
        }
        return "resolution_height";
    }

    public static String getConstActionShow() {
        return "ACTION_SHOW";
    }
    public static String getConstActionHide() {
        return "ACTION_HIDE";
    }

    public static String getConstActionPDFCreate() {
        return "pdf_create";
    }
    public static String getConstActionPDFDestroy() {
        return "pdf_destroy";
    }
    public static String getConstActionPDFInfo() {
        return "pdf_info";
    }
    public static String getConstActionPDFRequestAlloc() {
        return "pdf_request_alloc";
    }

    public static String getConstActionPDFOpen() {
        return "pdf_open";
    }
    public static String getConstActionPDFClose() {
        return "pdf_close";
    }
    public static String getConstActionPDFPageDown() {
        return "pdf_page_down";
    }
    public static String getConstActionPDFPageUp() {
        return "pdf_page_up";
    }



    public static String getConstDataKeySwitchAi3D(){
        return "switch_ai3d";
    }

    public static String getConstDataKeyOpenOrCloseAi3D(){
        return "is_open_ai3d";
    }

    public static String getConstDataKeySwitchAi3DResult(){
        return "switch_ai3d_result";
    }



    public static String getConstDataKeyGetAi3D(){
        return "get_ai3d";
    }

    public static String getConstDataKeyGetAi3DResult(){
        return "get_ai3d_result";
    }




    @Nullable
    public static Object createChannelDataInstance() {
        return invokeStaticMethod(CLASS_CHANNEL_DATA, "createInstance", null, null);
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
    public static Object proxyDPManagerListener(IDpMangerListenerProxy proxy) {
        Pair<Class<?>, Object> instance = proxyTargetClassInstance(
                CLASS_DP_MANAGER_LISTENER, new InvokeHandler(proxy));
        if (instance == null) {
            return null;
        }
        proxy.proxyObj = instance.second;
        return instance.second;
    }

    private static List<IDpMangerListenerProxy> dpMangerListenerProxies =  Collections.synchronizedList(new ArrayList<>());
    private static boolean hasRegisterProxy = false;
    private static IDpMangerListenerProxy dpMangerListenerProxyImpl = new IDpMangerListenerProxy(){

        @Override
        public void onEventChanged(long type, byte[] data) {
            super.onEventChanged(type, data);
            for (IDpMangerListenerProxy dpMangerListenerProxy : dpMangerListenerProxies) {
                try {
                    dpMangerListenerProxy.onEventChanged(type, data);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onEventForParcelableChanged(long type, @NonNull Object channelData) {
            super.onEventForParcelableChanged(type, channelData);
            for (IDpMangerListenerProxy dpMangerListenerProxy : dpMangerListenerProxies) {
                try {
                    dpMangerListenerProxy.onEventForParcelableChanged(type, channelData);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };


    public static boolean addDpImageDisplayManagerListener(IDpMangerListenerProxy listener) {
        try {
            dpMangerListenerProxies.add(listener);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (!hasRegisterProxy) {
                hasRegisterProxy = true;
                setFieldObject(getDpManagerInstance().getClass().getName(),
                        getDpManagerInstance(), "mRegistedDpMangerCallback", false
                );
                Object dpServiceInstance = getDpManagerInstance();
                if (dpServiceInstance != null) {
                    try {
                        Pair<Class<?>, Object> proxyTargetClassInstance
                                = proxyTargetClassInstance(
                                CLASS_DP_MANAGER_LISTENER,
                                new InvokeHandler(dpMangerListenerProxyImpl) {
                                    @Override
                                    public Object invoke(
                                            Object proxy,
                                            Method method,
                                            Object[] args
                                    ) throws Throwable {
                                        try {
                                            dpMangerListenerProxyImpl.proxyObj = proxy;
                                            for (Method me : dpMangerListenerProxyImpl.getClass()
                                                    .getMethods()) {
                                                if (me.getName()
                                                        .equals(method.getName())) {
                                                    return me.invoke(
                                                            dpMangerListenerProxyImpl,
                                                            args
                                                    );
                                                }
                                            }
                                        } catch (Exception var5) {
                                            var5.printStackTrace();
                                        }
                                        return method.invoke(
                                                dpMangerListenerProxyImpl,
                                                args
                                        );
                                    }
                                }
                        );
                        if (proxyTargetClassInstance == null) {
                            return false;
                        }
                        return (boolean) invokeInstanceMethod(
                                dpServiceInstance,
                                "addDpManagerListener",
                                new Class[]{long.class, com.inair.ref.RefInvoke.getClass(
                                        CLASS_DP_MANAGER_LISTENER)},
                                new Object[]{((long) getConstTypeImageDisplay()), proxyTargetClassInstance.second}
                        );
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }

            }
        }catch (Throwable e){
            e.printStackTrace();
        }

        return false;
    }

    public static void removeDpImageDisplayManagerListener(IDpMangerListenerProxy listener) {
        try {
            dpMangerListenerProxies.remove(listener);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        Object dpServiceInstance = getDpManagerInstance();
//        if (dpServiceInstance != null) {
//            try {
//                invokeInstanceMethod(dpServiceInstance, "removeDpManagerListener",
//                        new Class[]{long.class, Class.forName(
//                                CLASS_DP_MANAGER_LISTENER)},
//                        new Object[]{((long) getConstTypeImageDisplay()), listener.proxyObj}
//                );
//            } catch (Throwable e) {
//                e.printStackTrace();
//            }
//        }
    }

    public static void setAction(@Nullable Object obj, int action) {
        if (obj != null) {
            invokeInstanceMethod(obj, "setAction", new Class[]{int.class},
                    new Object[]{action}
            );
        }
    }

    public static int getAction(@Nullable Object obj) {
        if (obj != null) {
            return (int) invokeInstanceMethod(obj, "getAction", new Class[]{},
                    new Object[]{}
            );
        }
        return -1;
    }


    public static void putString(@Nullable Object obj, String action) {
        if (obj != null) {
            invokeInstanceMethod(obj, "putString", new Class[]{String.class},
                    new Object[]{action}
            );
        }
    }

    public static void putInt(@Nullable Object obj, int action) {
        if (obj != null) {
            invokeInstanceMethod(obj, "putInt", new Class[]{int.class},
                    new Object[]{action}
            );
        }
    }

    public static void setBundle(@Nullable Object obj, Bundle action) {
        if (obj != null) {
            invokeInstanceMethod(obj, "setBundle", new Class[]{Bundle.class},
                    new Object[]{action}
            );
        }
    }


    @NonNull
    public static Bundle getBundle(@Nullable Object channelData) {
        if (channelData != null) {
            return (Bundle) invokeInstanceMethod(channelData, "getBundle", new Class[]{},
                    new Object[]{}
            );
        }
        return new Bundle();
    }


    @SuppressLint("WrongConstant")
    @Nullable
    public static Object getDpManagerInstance() {
        if (globalApplication == null) {
            invokeStaticMethod(
                    LG_Package,
                    "e",
                    new Class[]{String.class, String.class,Object[].class},
                    new Object[]{TAG, "global application is null , do init"}
            );
            return null;
        }
        return globalApplication.getSystemService("dp");
    }

    public static void writeChannel(long var1, Object channelData) {
        Object getService = getDpManagerInstance();
        if (getService != null) {
            invokeInstanceMethod(getService, "writeChannel",
                    new Class[]{long.class, com.inair.ref.RefInvoke.getClass(
                            CLASS_CHANNEL_DATA)},
                    new Object[]{var1, channelData}
            );

            int action = getAction(channelData);
            Bundle bundle = getBundle(channelData);
            Object toJson = invokeStaticMethod(
                    GSON_Package,
                    "bundleToJson",
                    new Class[]{Bundle.class},
                    new Object[]{bundle}
            );
            invokeStaticMethod(
                    LG_Package,
                    "i",
                    new Class[]{String.class, String.class, Object[].class},
                    new Object[]{TAG, "write channel；%d data action:%d bundle:%s",  new Object[]{action, String.valueOf(
                            toJson)}}
            );
        }
    }

    public static final String TAG = "DpManagerProxy";

    public static void createImagePreviewCreate(int displayId, String uniqueID) {
        try {

            Object channelData = createChannelDataInstance();
            setAction(channelData, getConstImagePreview());
            Bundle bundle = new Bundle();
            bundle.putString(getConstAction(), getConstActionImageCreate());
            bundle.putInt(getConstDisplayID(), displayId);
            bundle.putString(getConstPackageName(), uniqueID);
            setBundle(channelData, bundle);
            writeChannel(getConstTypeImageDisplay(), channelData);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, " " + e);
        }
    }


    public static void createActionPageDown(int displayID, String uniqueID) {
        try {
            Object channelData = createChannelDataInstance();
            setAction(channelData, getConstImagePreview());
            Bundle bundle = new Bundle();
            bundle.putString(getConstAction(), "action_page_down");
            bundle.putInt(getConstDisplayID(), displayID);
            bundle.putString(getConstPackageName(), uniqueID);
            setBundle(channelData, bundle);
            writeChannel(getConstTypeImageDisplay(), channelData);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, " " + e);
        }
    }

    public static void createActionPageUp(int displayID,  String uniqueID) {
        try {
            Object channelData = createChannelDataInstance();
            setAction(channelData, getConstImagePreview());
            Bundle bundle = new Bundle();
            bundle.putString(getConstAction(), "action_page_up");
            bundle.putInt(getConstDisplayID(), displayID);
            bundle.putString(getConstPackageName(), uniqueID);
            setBundle(channelData, bundle);
            writeChannel(getConstTypeImageDisplay(), channelData);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, " " + e);
        }
    }

    public static void createActionShowLoading(int displayID, String uniqueID) {
        try {
            Object channelData = createChannelDataInstance();
            setAction(channelData, getConstImagePreview());
            Bundle bundle = new Bundle();
            bundle.putString(getConstAction(), "action_show_loading");
            bundle.putInt(getConstDisplayID(), displayID);
            bundle.putString(getConstPackageName(), uniqueID);
            setBundle(channelData, bundle);
            writeChannel(getConstTypeImageDisplay(), channelData);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, " " + e);
        }
    }

    public static void createActionHideLoading(int displayID,  String uniqueID) {
        try {
            Object channelData = createChannelDataInstance();
            setAction(channelData, getConstImagePreview());
            Bundle bundle = new Bundle();
            bundle.putString(getConstAction(), "action_hide_loading");
            bundle.putInt(getConstDisplayID(), displayID);
            bundle.putString(getConstPackageName(), uniqueID);
            setBundle(channelData, bundle);
            writeChannel(getConstTypeImageDisplay(), channelData);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, " " + e);
        }
    }




    public static void createImagePreviewShow(int displayId, String uniqueID) {
        try {

            Object channelData = createChannelDataInstance();
            setAction(channelData, getConstImagePreview());
            Bundle bundle = new Bundle();
            bundle.putString(getConstAction(), getConstActionShow());
            bundle.putInt(getConstDisplayID(), displayId);
            bundle.putString(getConstPackageName(), uniqueID);
            setBundle(channelData, bundle);
            writeChannel(getConstTypeImageDisplay(), channelData);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, " " + e);
        }
    }


    public static void createImagePreviewHide(int displayId, String uniqueID) {
        try {

            Object channelData = createChannelDataInstance();
            setAction(channelData, getConstImagePreview());
            Bundle bundle = new Bundle();
            bundle.putString(getConstAction(), getConstActionHide());
            bundle.putInt(getConstDisplayID(), displayId);
            bundle.putString(getConstPackageName(), uniqueID);
            setBundle(channelData, bundle);
            writeChannel(getConstTypeImageDisplay(), channelData);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, " " + e);
        }
    }
    public static void createMousePreviewLevel(int displayId,int level) {
        try {

            Object channelData = createChannelDataInstance();
            setAction(channelData, getConstImagePreview());
            Bundle bundle = new Bundle();
            bundle.putString(getConstAction(), "ACTION_MOUSE_PREVIEW_LEVEL");
            bundle.putInt(getConstDisplayID(), displayId);
            bundle.putInt("mouse_level", level);
            setBundle(channelData, bundle);
            writeChannel(getConstTypeImageDisplay(), channelData);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, " " + e);
        }
    }


    public static class GetAi3DResult{
        private boolean isOpen = false;

        public GetAi3DResult(boolean isOpen) {
            this.isOpen = isOpen;
        }

        public boolean isOpen() {
            return isOpen;
        }
    }

    @Nullable
    public static GetAi3DResult getGetSwitchAi3DResult( Object readChannelData,
                                                  int displayID,
                                                  String uniqueID) {
        int action = getAction(readChannelData);
        if (action == getConstImagePreview()) {
            Bundle bundle = getBundle(readChannelData);
            int displayOldID = bundle.getInt(getConstDisplayID(), -1);
            String packageOldName = bundle.getString(getConstPackageName(), "");
            if (Objects.equals(displayOldID, displayID) && Objects.equals(
                    packageOldName,
                    uniqueID
            ) && bundle.getString(getConstAction(), "").equals(getConstDataKeySwitchAi3DResult())) {
                return new GetAi3DResult(bundle.getBoolean(getConstDataKeyOpenOrCloseAi3D(), false));
            }
            return null;

        }
        return null;
    }

    @Nullable
    public static GetAi3DResult getGetAi3DResult( Object readChannelData,
                                                  int displayID,
                                                  String uniqueID
    ) {
        int action = getAction(readChannelData);
        if (action == getConstImagePreview()) {
            Bundle bundle = getBundle(readChannelData);
            int displayOldID = bundle.getInt(getConstDisplayID(), -1);
            String packageOldName = bundle.getString(getConstPackageName(), "");
            if (Objects.equals(displayOldID, displayID) && Objects.equals(packageOldName,
                    uniqueID
            ) && bundle.getString(getConstAction(), "").equals(getConstDataKeyGetAi3DResult())) {
                return new GetAi3DResult(bundle.getBoolean(getConstDataKeyOpenOrCloseAi3D(),
                        false
                ));
            }
            return null;

        }
        return null;
    }


    public static void createSwitchAi3D(
          boolean isOpen, int displayId,String uniqueID
    ) {
        try {
            Object channelData = createChannelDataInstance();
            setAction(channelData, getConstImagePreview());
            Bundle bundle = new Bundle();
            bundle.putString(getConstAction(), getConstDataKeySwitchAi3D());
            bundle.putString(getConstPackageName(), uniqueID);
            bundle.putInt(getConstDisplayID(), displayId);
            bundle.putBoolean(getConstDataKeyOpenOrCloseAi3D(), isOpen);
            setBundle(channelData, bundle);
            writeChannel(getConstTypeImageDisplay(), channelData);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, " " + e);
        }
    }


    public static void createGetAi3DResult(int displayId,String uniqueID
    ) {
        try {
            Object channelData = createChannelDataInstance();
            setAction(channelData, getConstImagePreview());
            Bundle bundle = new Bundle();
            bundle.putString(getConstAction(), getConstDataKeyGetAi3D());
            bundle.putString(getConstPackageName(), uniqueID);
            bundle.putInt(getConstDisplayID(), displayId);
            setBundle(channelData, bundle);
            writeChannel(getConstTypeImageDisplay(), channelData);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, " " + e);
        }
    }



    /**
     * 针对图库的
     */
    public static void createImagePreviewOpenForIMAGE(
            HardwareBuffer buffer,
            int dataType,
            int displayWidth,
            int displayHeight,
            int displayId,
            Rect imageContentArea

    ) {
        try {
            Object channelData = createChannelDataInstance();
            setAction(channelData, getConstImagePreview());
            Bundle bundle = new Bundle();
            bundle.putString(getConstAction(), getConstActionImageOpen());
            bundle.putString(getConstPackageName(), "IMAGE");
            bundle.putInt(getConstDisplayID(), displayId);
            bundle.putParcelable(getConstDataKeysHardwareBuffer(), buffer);
            bundle.putInt(getConstDataKeysDATATYPE(), dataType);
            bundle.putInt(getConstDataKeysResolutionWidth(), displayWidth);
            bundle.putInt(getConstDataKeysResolutionHeight(), displayHeight);
            bundle.putParcelable("IMAGE_CONTENT_AREA", imageContentArea);
            setBundle(channelData, bundle);
            writeChannel(getConstTypeImageDisplay(), channelData);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, " " + e);
        }
    }


    /**
     * 针对InairSpace
     * 显示类型 Int 0普通 1：3D 2：全景
     */
    public static void createImagePreviewOpenINAIRSPACE(
            HardwareBuffer buffer,
            Rect windowContent,
            int displayId
    ) {
        try {
            Object channelData = createChannelDataInstance();
            setAction(channelData, getConstImagePreview());
            Bundle bundle = new Bundle();
            bundle.putString(getConstAction(), getConstActionImageOpen());
            bundle.putParcelable(getConstDataKeysHardwareBuffer(), buffer);
            bundle.putInt(getConstDataKeysDATATYPE(), 0);
            bundle.putBoolean("IS_OVER", false);
            bundle.putInt(getConstDisplayID(), displayId);
            bundle.putParcelable("WINDOW_CONTENT_AREA", windowContent);
            bundle.putString(getConstPackageName(), "INAIRSPACE");
            setBundle(channelData, bundle);
            writeChannel(getConstTypeImageDisplay(), channelData);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, " " + e);
        }
    }

    public static void createImagePreviewOpen(
            HardwareBuffer buffer,
            Rect windowContent,
            int displayId,
            boolean isOver,
            DataType dataType,
            String packageName
    ) {
        try {
            Object channelData = createChannelDataInstance();
            setAction(channelData, getConstImagePreview());
            Bundle bundle = new Bundle();
            bundle.putString(getConstAction(), getConstActionImageOpen());
            bundle.putParcelable(getConstDataKeysHardwareBuffer(), buffer);
            bundle.putInt(getConstDataKeysDATATYPE(), dataType.type);
            bundle.putBoolean("IS_OVER", isOver);
            bundle.putInt(getConstDisplayID(), displayId);
            bundle.putParcelable("WINDOW_CONTENT_AREA", windowContent);
            bundle.putString(getConstPackageName(), packageName);
            bundle.getParcelable("WINDOW_CONTENT_AREA");
            setBundle(channelData, bundle);
            writeChannel(getConstTypeImageDisplay(), channelData);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, " " + e);
        }
    }


    public static void createImageRequestBuffer(
            int width,
            int height,
            int displayId,
            String uniqueID
    ) {
        try {
            Object channelData = createChannelDataInstance();
            setAction(channelData, getConstImagePreview());
            Bundle bundle = new Bundle();
            bundle.putString(getConstAction(), getConstActionImageRequestAlloc());
            bundle.putInt(getConstDataKeysResolutionWidth(), width);
            bundle.putInt(getConstDataKeysResolutionHeight(), height);
            bundle.putInt(getConstDisplayID(), displayId);
            bundle.putString(getConstPackageName(), uniqueID);
            setBundle(channelData, bundle);
            writeChannel(getConstTypeImageDisplay(), channelData);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, " " + e);
        }
    }


    public static void createImagePreviewDestroy(int displayID, String uniqueID) {
        try {
            Object channelData = createChannelDataInstance();
            setAction(channelData, getConstImagePreview());
            Bundle bundle = new Bundle();
            bundle.putString(getConstAction(), getConstActionImageDestroy());
            bundle.putInt(getConstDisplayID(), displayID);
            bundle.putString(getConstPackageName(), uniqueID);
            setBundle(channelData, bundle);
            writeChannel(getConstTypeImageDisplay(), channelData);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, " " + e);
        }
    }


    public static void createImageClearAll(int displayID) {
        try {
            Object channelData = createChannelDataInstance();
            setAction(channelData, getConstImagePreview());
            Bundle bundle = new Bundle();
            bundle.putString(getConstAction(), getConstActionImageClearAll());
            bundle.putInt(getConstDisplayID(), displayID);
            setBundle(channelData, bundle);
            writeChannel(getConstTypeImageDisplay(), channelData);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, " " + e);
        }
    }




    public static void createImageModelCreate(int displayId, String uniqueID) {
        try {

            Object channelData = createChannelDataInstance();
            setAction(channelData, getConstImagePreview());
            Bundle bundle = new Bundle();
            bundle.putString(getConstAction(), getConstImageModelCreate());
            bundle.putInt(getConstDisplayID(), displayId);
            bundle.putString(getConstPackageName(), uniqueID);

            setBundle(channelData, bundle);
            writeChannel(getConstTypeImageDisplay(), channelData);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, " " + e);
        }
    }

    public static void createImageModelMaterialType(int displayId, Uri uri) {
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


    @Nullable
    public static HardwareBuffer getBundleHardwareBuffer(Bundle bundle) {
        Parcelable parcelable = bundle.getParcelable(getConstDataKeysHardwareBuffer());
        if (parcelable instanceof HardwareBuffer) {
            return ((HardwareBuffer) parcelable);
        }
        return null;
    }


    public static class RequestFailMessage {
        public String requestAction;
        public int displayID;
        public String packageName;

        public RequestFailMessage(String requestAction, int displayID, String packageName) {
            this.requestAction = requestAction;
            this.displayID = displayID;
            this.packageName = packageName;
        }
    }

    @Nullable
    public static RequestFailMessage getRequestFailMessage(Bundle bundle) {
        if (bundle == null){
            return null;
        }
        if (!bundle.getString(getConstAction(),"").equals(getConstDataKeysREQUESTFAIL())) {
            return null;
        }
        String requestAction = bundle.getString(getConstDataKeysREQUESTACTION(), "");
        int displayID = bundle.getInt(getConstDisplayID(), -1);
        String packageName = bundle.getString(getConstPackageName(), "");
        if (!TextUtils.isEmpty(requestAction)) {
            return new RequestFailMessage(requestAction, displayID, packageName);
        }
        return null;
    }
    /**
     *    ChannelData channelData = ChannelData.createInstance();
     *     channelData.setAction(ChannelAction.UNITY_ACTION); // UNITY_ACTION 1999
     *     Bundle bundle = new Bundle();
     *     bundle.putString(ChannelDataKeys.ACTION, "UNITY_ACTIVE");
     *     bundle.putString("ACTIVE", true/false); // true 代表可以操作 Unity 虚拟屏 false 代表不
     *
     * */
    public static class UnityActiveMessage {
        public boolean isActive;

        public UnityActiveMessage(boolean isActive) {
            this.isActive = isActive;
        }
    }

    @Nullable
    public static UnityActiveMessage getUnityActiveMessage(Bundle bundle) {
        if (bundle == null){
            return null;
        }
        if (!bundle.getString(getConstAction(),"").equals(getConstUnityActive())) {
            return null;
        }
        boolean active = bundle.getBoolean(getConstDataKeysACTIVE(),false);
        return new UnityActiveMessage(active);
    }

    public static String bundle2String(Bundle bundle) {
        if (bundle == null || bundle.isEmpty()) {
            return "Bundle is null or empty";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Bundle{");
        // 获取Bundle所有键名集合
        Set<String> keySet = bundle.keySet();
        for (String key : keySet) {
            // 通用get方法获取值（适配所有类型）
            Object value = bundle.get(key);
            // 拼接键值对，特殊类型可单独格式化（如List/数组）
            sb.append(key).append("=").append(value).append(", ");
        }
        // 移除最后一个多余的逗号和空格
        if (sb.length() > 7) { // 大于"Bundle{"的长度
            sb.delete(sb.length() - 2, sb.length());
        }
        sb.append("}");
        return sb.toString();
    }


    @Nullable
    public static Bundle hasImageAllocBundle(
            Object readChannelData,
            int displayID,
            String uniqueID
    ) {
        int action = getAction(readChannelData);
        if (action == getConstImagePreview() || action == getConstUnityAction()) {
            Bundle bundle = getBundle(readChannelData);
            int displayOldID = bundle.getInt(getConstDisplayID(), -1);
            String packageOldName = bundle.getString(getConstPackageName(), "");
//            Lg.i(TAG, "hasImageAllocBundle DisplayID:%d UniqueID:%s", displayOldID, packageOldName);
            if (Objects.equals(displayOldID, displayID) && Objects.equals(
                    packageOldName,
                    uniqueID
            )) {
                invokeStaticMethod(
                        LG_Package,
                        "i",
                        new Class[]{String.class, String.class, Object[].class},
                        new Object[]{TAG, "Receive Bundle DisplayID:%d UniqueID:%s \n%s", new Object[]{displayOldID,packageOldName,bundle2String(bundle)}}
                );
                return bundle.getString(getConstAction(), "")
                        .equals(getConstActionImageAlloc()) ? bundle : null;
            }
            return null;

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

    public static String getBundleAction(Object readChannelData) {
        Bundle bundle = getBundle(readChannelData);
        return bundle.getString(getConstAction(), "");
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


    public static int getChannelDataDisplayID(Object channelData) {
        Bundle bundle = getBundle(channelData);
        return bundle.getInt(getConstDisplayID(), -1);
    }





    public static void createPDFCreate(int displayID) {
        try {
            Object channelData = createChannelDataInstance();
            setAction(channelData, getConstImagePreview());
            Bundle bundle = new Bundle();
            bundle.putString(getConstAction(), getConstActionPDFCreate());
            bundle.putInt(getConstDisplayID(), displayID);
            setBundle(channelData, bundle);
            writeChannel(getConstTypeImageDisplay(), channelData);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, " " + e);
        }
    }


    public static void createPDFDestroy(int displayID) {
        try {
            Object channelData = createChannelDataInstance();
            setAction(channelData, getConstImagePreview());
            Bundle bundle = new Bundle();
            bundle.putString(getConstAction(), getConstActionPDFDestroy());
            bundle.putInt(getConstDisplayID(), displayID);
            setBundle(channelData, bundle);
            writeChannel(getConstTypeImageDisplay(), channelData);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, " " + e);
        }
    }

    public static void createRequestPDFCount(int displayID, int pageCount, int pageSelect) {
        try {
            Object channelData = createChannelDataInstance();
            setAction(channelData, getConstImagePreview());
            Bundle bundle = new Bundle();
            bundle.putString(getConstAction(), getConstActionPDFInfo());
            bundle.putInt(getConstDisplayID(), displayID);
            bundle.putInt("count", pageCount);
            bundle.putInt("pageSelect", pageSelect);
            setBundle(channelData, bundle);
            writeChannel(getConstTypeImageDisplay(), channelData);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, " " + e);
        }
    }


    public static void createRequestPDFAlloc(
            int displayID, int hardwareWidth, int hardwareHeight, int index
    ) {
        try {
            Object channelData = createChannelDataInstance();
            setAction(channelData, getConstImagePreview());
            Bundle bundle = new Bundle();
            bundle.putString(getConstAction(), getConstActionPDFRequestAlloc());
            bundle.putInt(getConstDisplayID(), displayID);
            bundle.putInt(getConstDataKeysResolutionWidth(), hardwareWidth);
            bundle.putInt(getConstDataKeysResolutionHeight(), hardwareHeight);
            bundle.putInt("index", index);
            setBundle(channelData, bundle);
            writeChannel(getConstTypeImageDisplay(), channelData);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, " " + e);
        }
    }


    public static void createOpenPDF(
            int displayID, int dateType, int displayWidth, int displayHeight, int harwareIndex,
            int page
    ) {
        try {
            Object channelData = createChannelDataInstance();
            setAction(channelData, getConstImagePreview());
            Bundle bundle = new Bundle();
            bundle.putString(getConstAction(), getConstActionPDFOpen());
            bundle.putInt(getConstDataKeysDATATYPE(), dateType);
            bundle.putInt(getConstDisplayID(), displayID);
            bundle.putInt(getConstDataKeysResolutionWidth(), displayWidth);
            bundle.putInt(getConstDataKeysResolutionHeight(), displayHeight);
            bundle.putInt("index", harwareIndex);
            bundle.putInt("page", page);
            setBundle(channelData, bundle);
            writeChannel(getConstTypeImageDisplay(), channelData);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, " " + e);
        }
    }

    public static void createClosePDF(int displayID, int harwareIndex, int page) {
        try {
            Object channelData = createChannelDataInstance();
            setAction(channelData, getConstImagePreview());
            Bundle bundle = new Bundle();
            bundle.putString(getConstAction(), getConstActionPDFClose());
            bundle.putInt(getConstDisplayID(), displayID);
            bundle.putInt("index", harwareIndex);
            bundle.putInt("page", page);
            setBundle(channelData, bundle);
            writeChannel(getConstTypeImageDisplay(), channelData);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, " " + e);
        }
    }


    public static void createPageDownPDF(int displayID, int currentPage) {
        try {
            Object channelData = createChannelDataInstance();
            setAction(channelData, getConstImagePreview());
            Bundle bundle = new Bundle();
            bundle.putString(getConstAction(), getConstActionPDFPageDown());
            bundle.putInt(getConstDisplayID(), displayID);
            bundle.putInt("page", currentPage);
            setBundle(channelData, bundle);
            writeChannel(getConstTypeImageDisplay(), channelData);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, " " + e);
        }
    }



    public static void createPageUpPDF(int displayID, int currentPage) {
        try {
            Object channelData = createChannelDataInstance();
            setAction(channelData, getConstImagePreview());
            Bundle bundle = new Bundle();
            bundle.putString(getConstAction(), getConstActionPDFPageUp());
            bundle.putInt(getConstDisplayID(), displayID);
            bundle.putInt("page", currentPage);
            setBundle(channelData, bundle);
            writeChannel(getConstTypeImageDisplay(), channelData);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, " " + e);
        }
    }


    public static int getChannelDataIndex(Object readChannelData) {
        Bundle bundle = getBundle(readChannelData);
        return bundle.getInt("index", -1);
    }

    @Nullable
    public static Bundle hasPDFAllocBundle(Object readChannelData) {
        int action = getAction(readChannelData);
        if (action == getConstImagePreview()) {
            Bundle bundle = getBundle(readChannelData);
            return bundle.getString(getConstAction(), "").equals("pdf_alloc") ? bundle : null;
        }
        return null;
    }


    public static void updateDisplaySize(
            int displayID,
            int display_width,
            int display_height,
            int dpi
    ) {
        try {
            Object channelData = createChannelDataInstance();
            setAction(channelData, getConstInairSpaceWindowResize());
            Bundle bundle = new Bundle();
            bundle.putInt(getConstDisplayID(), displayID);
            bundle.putInt("display_width", display_width);
            bundle.putInt("display_height", display_height);
            bundle.putInt("display_dpi", dpi);
            setBundle(channelData, bundle);
            writeChannel(getConstTypeDisplay(), channelData);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, " " + e);
        }
    }
}
