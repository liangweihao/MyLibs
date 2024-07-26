package com.nothing.commonutils.utils;

import android.text.TextUtils;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
 
public class RootTools {
    private static final String ID_UID_GID_REGEX = ".*uid=(\\d*).*gid=(\\d*).*";
    public static final String LINUX_CMD_EXIT = "exit\n";
    private static final String LINUX_CMD_EXPORT_LIB = "export LD_LIBRARY_PATH=/vendor/lib:/system/lib\n";
    public static final String LINUX_CMD_ID = "id\n";
    public static final String LINUX_CMD_SH = "sh";
    public static final String LINUX_CMD_SU = "su";
    private static Process mSuProcess;
    private static ProcessBuilder mSuProcessBuilder;

    private static final String TAG = "RootTools";
    private RootTools() {
    }

    public static boolean isRootAvailable() {
        String[] strArr = {"/sbin/", "/system/bin/", "/system/xbin/", "/bin/", "/"};
        for (int i = 0; i < 5; i++) {
            String str = strArr[i];
            if (new File(str + LINUX_CMD_SU).exists()) {
                return true;
            }
        }
        return false;
    }

    public static boolean isRootPermission() {
        return requestSuPermission();
    }

 
    public static Process checkGlobalSuPermission(boolean z) {
        int i = 0;
        int i2 = 0;
        boolean z2 = false;
        try {
            if (mSuProcessBuilder == null) {
                mSuProcessBuilder = new ProcessBuilder(LINUX_CMD_SU);
            }
            mSuProcessBuilder.redirectErrorStream(true);
            if (mSuProcess == null) {
                mSuProcess = mSuProcessBuilder.start();
                Lg.d(TAG, "------ [debug] checkGlobalSuPermission, ProcessBuilder start");
            }
            DataOutputStream dataOutputStream = new DataOutputStream(mSuProcess.getOutputStream());
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(mSuProcess.getInputStream()));
            dataOutputStream.writeBytes(LINUX_CMD_ID);
            if (z) {
                dataOutputStream.writeBytes(LINUX_CMD_EXIT);
            }
            dataOutputStream.flush();
            while (true) {
                String readLine = bufferedReader.readLine();
                i = -1;
                if (readLine == null) {
                    i2 = -1;
                    break;
                } else if (!TextUtils.isEmpty(readLine.trim())) {
                    Matcher matcher = Pattern.compile(ID_UID_GID_REGEX).matcher(readLine);
                    if (matcher.find()) {
                        break;
                    }
                }
            }
            if (i == 0 && i2 == 0) {
                z2 = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!z2) {
            mSuProcess = null;
        }
        return mSuProcess;
    }

    /* JADX WARN: Code restructure failed: missing block: B:14:0x005c, code lost:
        r4 = r7.group(1);
        java.util.Objects.requireNonNull(r4);
        r5 = r4;
        r4 = java.lang.Integer.parseInt(r4);
        r7 = r7.group(2);
        java.util.Objects.requireNonNull(r7);
        r5 = r7;
        r7 = java.lang.Integer.parseInt(r7);
     */
    /* JADX WARN: Removed duplicated region for block: B:27:0x008b  */
    /* JADX WARN: Removed duplicated region for block: B:39:? A[RETURN, SYNTHETIC] */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
    */
    public static Process checkSuPermission(boolean z) {
        Process process;
        int i = 0;
        int i2 = 0;
        boolean z2 = false;
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(LINUX_CMD_SU);
            processBuilder.redirectErrorStream(true);
            process = processBuilder.start();
        } catch (Exception e) {
            e.printStackTrace();
            process = null;
        }
        try {
            DataOutputStream dataOutputStream = new DataOutputStream(process.getOutputStream());
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            dataOutputStream.writeBytes(LINUX_CMD_ID);
            if (z) {
                dataOutputStream.writeBytes(LINUX_CMD_EXIT);
            }
            dataOutputStream.flush();
            while (true) {
                String readLine = bufferedReader.readLine();
                i = -1;
                if (readLine == null) {
                    i2 = -1;
                    break;
                } else if (!TextUtils.isEmpty(readLine.trim())) {
                    Matcher matcher = Pattern.compile(ID_UID_GID_REGEX).matcher(readLine);
                    if (matcher.find()) {
                        break;
                    }
                }
            }
        } catch (Exception e) {

            e.printStackTrace();
        }
        return process;
    }

    public static boolean requestSuPermission() {
        Exception e;
        boolean z = true;
        try {
            Process checkSuPermission = checkSuPermission(true);
            if (checkSuPermission != null) {
                try {
                    checkSuPermission.waitFor();
                    checkSuPermission.destroy();
                } catch (Exception e2) {
                    e = e2;
                    e.printStackTrace();

                    Lg.i(TAG, "[root] request su permission, result:" + z);
                    return z;
                }
            } else {
                z = false;
            }
        } catch (Exception e3) {
            e = e3;
            z = false;
        }
        Lg.i(TAG, "[root] request su permission, result:" + z);
        return z;
    }
}
