package com.nothing.commonutils.services;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.accessibilityservice.GestureDescription;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Path;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;

import com.nothing.commonutils.utils.Lg;

import java.util.List;

import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;

/*
*
*      <service
            android:label="@string/accessibility_service_label"
            android:name="PermissionAccessibilityService"
            android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE"
            android:exported="true"
            android:process="accessibilityinput"
            android:description="@string/accessibility_service_description">
            <intent-filter>
                <action android:name="android.accessibilityservice.AccessibilityService"/>
            </intent-filter>
            <meta-data
                android:name="android.accessibilityservice"
                android:resource="@xml/accessibility_service_info"/>
        </service>
        *
        *
        * accessibility_service_info -->
        *
        * <?xml version="1.0" encoding="utf-8"?>
<accessibility-service xmlns:android="http://schemas.android.com/apk/res/android"
    android:description="@string/accessibility_service_description"
    android:settingsActivity=".ServiceSettingsActivity"
    android:accessibilityEventTypes="typeAllMask"
    android:packageNames="com.android.systemui"
    android:accessibilityFeedbackType="feedbackGeneric"
    android:notificationTimeout="100"
    android:accessibilityFlags="flagDefault"
    android:canRetrieveWindowContent="true"/>
*
* */
public class BaseAccessibilityService extends AccessibilityService {
    private static final String TAG = "BaseAccessibilityService";
    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
    }

    @Override
    public void onInterrupt() {
    }

    public static boolean isAccessibilityServiceEnabled(Context context,String serviceClassName) {
        AccessibilityManager
                accessibilityManager = (AccessibilityManager) context.getSystemService(ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> enabledServices = accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC);

        for (AccessibilityServiceInfo service : enabledServices) {
            if (service.getId().equals(serviceClassName)) {
                return true;
            }
        }

        return false;
    }



    public static void requestAccessibilityService(Context context) {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }


    public void performViewClick(AccessibilityNodeInfo accessibilityNodeInfo) {
        if (accessibilityNodeInfo == null) {
            return;
        }
        while (accessibilityNodeInfo != null) {
            if (accessibilityNodeInfo.isClickable()) {
                accessibilityNodeInfo.performAction(16);
                return;
            }
            accessibilityNodeInfo = accessibilityNodeInfo.getParent();
        }
    }

    public void performScrollBackward() {
        try {
            Thread.sleep(100L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        performGlobalAction(8192);
    }

    public void performScrollForward() {
        try {
            Thread.sleep(100L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        performGlobalAction(4096);
    }

    public AccessibilityNodeInfo findViewByText(String str) {
        return findViewByText(str, false);
    }

    public AccessibilityNodeInfo findViewByText(String str, boolean z) {
        AccessibilityNodeInfo rootInActiveWindow = getRootInActiveWindow();
        if (rootInActiveWindow == null) {
            return null;
        }
        Lg.i(TAG, "window>>> empty");
        List<AccessibilityNodeInfo> findAccessibilityNodeInfosByText = rootInActiveWindow.findAccessibilityNodeInfosByText(str);
        if (findAccessibilityNodeInfosByText == null || findAccessibilityNodeInfosByText.isEmpty()) {
            return null;
        }
        for (AccessibilityNodeInfo accessibilityNodeInfo : findAccessibilityNodeInfosByText) {
            if (accessibilityNodeInfo != null && accessibilityNodeInfo.isClickable() == z) {
                return accessibilityNodeInfo;
            }
        }
        return null;
    }

    public AccessibilityNodeInfo findViewByID(String str) {
        List<AccessibilityNodeInfo> findAccessibilityNodeInfosByViewId;
        AccessibilityNodeInfo rootInActiveWindow = getRootInActiveWindow();
        if (rootInActiveWindow != null && (findAccessibilityNodeInfosByViewId = rootInActiveWindow.findAccessibilityNodeInfosByViewId(str)) != null && !findAccessibilityNodeInfosByViewId.isEmpty()) {
            for (AccessibilityNodeInfo accessibilityNodeInfo : findAccessibilityNodeInfosByViewId) {
                if (accessibilityNodeInfo != null) {
                    return accessibilityNodeInfo;
                }
            }
        }
        return null;
    }

    public void clickTextViewByText(String str) {
        List<AccessibilityNodeInfo> findAccessibilityNodeInfosByText;
        AccessibilityNodeInfo rootInActiveWindow = getRootInActiveWindow();
        if (rootInActiveWindow == null || (findAccessibilityNodeInfosByText = rootInActiveWindow.findAccessibilityNodeInfosByText(str)) == null || findAccessibilityNodeInfosByText.isEmpty()) {
            return;
        }
        for (AccessibilityNodeInfo accessibilityNodeInfo : findAccessibilityNodeInfosByText) {
            if (accessibilityNodeInfo != null) {
                performViewClick(accessibilityNodeInfo);
                return;
            }
        }
    }

    public void clickTextViewByID(String str) {
        List<AccessibilityNodeInfo> findAccessibilityNodeInfosByViewId;
        AccessibilityNodeInfo rootInActiveWindow = getRootInActiveWindow();
        if (rootInActiveWindow == null || (findAccessibilityNodeInfosByViewId = rootInActiveWindow.findAccessibilityNodeInfosByViewId(str)) == null || findAccessibilityNodeInfosByViewId.isEmpty()) {
            return;
        }
        for (AccessibilityNodeInfo accessibilityNodeInfo : findAccessibilityNodeInfosByViewId) {
            if (accessibilityNodeInfo != null) {
                performViewClick(accessibilityNodeInfo);
                return;
            }
        }
    }

    // 模拟点击屏幕某个点
    public void clickOnScreen(int x, int y) {
        Path path = new Path();
        path.moveTo(x, y);
        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(path, 0, 50));
        dispatchGesture(gestureBuilder.build(), null, null);
    }


    public void inputText(AccessibilityNodeInfo accessibilityNodeInfo, String str) {
        if (Build.VERSION.SDK_INT >= 21) {
            Bundle bundle = new Bundle();
            bundle.putCharSequence(AccessibilityNodeInfoCompat.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, str);
            accessibilityNodeInfo.performAction(2097152, bundle);
        } else if (Build.VERSION.SDK_INT >= 18) {
            ((ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("label", str));
            accessibilityNodeInfo.performAction(1);
            accessibilityNodeInfo.performAction(32768);
        }
    }
}
