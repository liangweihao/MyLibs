package com.nothing.commonutils.utils;

import android.util.Printer;

public class ApplicationMessagePrinter implements Printer {
    private static final String TAG = "ApplicationMessagePrint";

    @Override
    public void println(String x) {
        if (x.contains("<<<<") && x.contains("android.view.View$PerformClick")) {
            Lg.i(TAG, "Do Click %s", getHash(x));
        }
        if (x.contains("<<<<") && x.contains("android.view.View$CheckForLongPress")) {
            Lg.i(TAG, "Do LongClick %s", getHash(x));
        }
    }


    private String getHash(String x) {
        StringBuilder hashString = new StringBuilder();
        try {
            int atIndex = x.indexOf("@");
            if (atIndex >= 0) {
                while (true) {
                    atIndex++;
                    if (atIndex >= x.length()) {
                        break;
                    }
                    char charAt = x.charAt(atIndex);
                    if (String.valueOf(charAt).equals(" ")) {
                        // end
                        break;
                    }
                    hashString.append(charAt);
                }
            }
        } catch (Throwable t) {

        }
        return hashString.toString();
    }

}
