package com.nothing.commonutils.utils;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class ServiceUtils {

    public static void bindNotification(
            int id, Service service, String channelid, String name, int icon, String title, String content
    ) {
        try {
            NotificationManager notificationManager = getNotificationManager(service, channelid, name);
            Notification build = new NotificationCompat.Builder(service, channelid).setDefaults(4)
                                                                             .setSmallIcon(icon)
                                                                             .setContentTitle(title)
                                                                             .setContentText(content)
                                                                             .build();
            if (Build.VERSION.SDK_INT > 29) {
                service.startForeground(id, build,
                                        ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION);
            } else if (Build.VERSION.SDK_INT >= 26) {
                service.startForeground(id, build);
            } else if (notificationManager != null) {
                notificationManager.notify(id, build);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static NotificationManager getNotificationManager(
            Context context, String id, String name
    ) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(
                Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel notificationChannel = new NotificationChannel(id, name, 3);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(-16776961);
            notificationChannel.setLockscreenVisibility(1);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }
        return notificationManager;
    }

    public static boolean isChannelCanUse(Context context, String id, String name) {
        return Build.VERSION.SDK_INT < 26 ||
               getNotificationManager(context, id, name).getNotificationChannel(id)
                                                         .getImportance() != 0;
    }

    public static void openNotificationChannel(Context context, String channelID) {
        if (Build.VERSION.SDK_INT < 26 || context == null) {
            return;
        }
        Intent intent = new Intent("android.settings.CHANNEL_NOTIFICATION_SETTINGS");
        intent.putExtra("android.provider.extra.APP_PACKAGE", context.getPackageName());
        intent.putExtra("android.provider.extra.CHANNEL_ID", channelID);
        context.startActivity(intent);
    }

    public static void cancelNotification(int i, Service service, String id, String name) {
        NotificationManager notificationManager = getNotificationManager(service, id, name);
        if (notificationManager != null) {
            notificationManager.cancel(i);
        }
    }
}