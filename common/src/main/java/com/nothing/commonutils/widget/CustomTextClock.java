/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nothing.commonutils.widget;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.database.ContentObserver;
import android.icu.text.DateTimePatternGenerator;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.util.AttributeSet;

import com.nothing.commonutils.R;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.TimeZone;

import androidx.annotation.InspectableProperty;
import androidx.annotation.RequiresApi;

import static android.view.ViewDebug.ExportedProperty;

public class CustomTextClock extends androidx.appcompat.widget.AppCompatTextView {



    private CharSequence mFormat12;
    private CharSequence mFormat24;
    private CharSequence mDescFormat12;
    private CharSequence mDescFormat24;

    @ExportedProperty
    private CharSequence mFormat;
    @ExportedProperty
    private boolean mHasSeconds;

    private CharSequence mDescFormat;

    private boolean mRegistered;
    private boolean mShouldRunTicker;

    private Calendar mTime;
    private String mTimeZone;

    private boolean mShowCurrentUserTime;

    private ContentObserver mFormatChangeObserver;
    // Used by tests to stop time change events from triggering the text update
    private boolean mStopTicking;

    private class FormatChangeObserver extends ContentObserver {

        public FormatChangeObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            chooseFormat();
            onTimeChanged();
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            chooseFormat();
            onTimeChanged();
        }
    };

    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (mTimeZone == null && Intent.ACTION_TIMEZONE_CHANGED.equals(intent.getAction())) {
                final String timeZone = intent.getStringExtra(Intent.EXTRA_TIMEZONE);
                createTime(timeZone);
            } else if (!mShouldRunTicker && (Intent.ACTION_TIME_TICK.equals(intent.getAction())
                    || Intent.ACTION_TIME_CHANGED.equals(intent.getAction()))) {
                return;
            }
            onTimeChanged();
        }
    };

    private final Runnable mTicker = new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.O)
        public void run() {
            removeCallbacks(this);
            if (mStopTicking || !mShouldRunTicker) {
                return; // Test disabled the clock ticks
            }
            onTimeChanged();

            Instant now = mTime.toInstant();
            ZoneId zone = mTime.getTimeZone().toZoneId();

            ZonedDateTime nextTick;
            if (mHasSeconds) {
                nextTick = now.atZone(zone).plusSeconds(1).withNano(0);
            } else {
                nextTick = now.atZone(zone).plusMinutes(1).withSecond(0).withNano(0);
            }

            long millisUntilNextTick = Duration.between(now, nextTick.toInstant()).toMillis();
            if (millisUntilNextTick <= 0) {
                // This should never happen, but if it does, then tick again in a second.
                millisUntilNextTick = 1000;
            }

            postDelayed(this, millisUntilNextTick);
        }
    };

    /**
     * Creates a new clock using the default patterns for the current locale.
     *
     * @param context The Context the view is running in, through which it can
     *        access the current theme, resources, etc.
     */
    @SuppressWarnings("UnusedDeclaration")
    public CustomTextClock(Context context) {
        super(context);
        init();
    }

    /**
     * Creates a new clock inflated from XML. This object's properties are
     * intialized from the attributes specified in XML.
     *
     * This constructor uses a default style of 0, so the only attribute values
     * applied are those in the Context's Theme and the given AttributeSet.
     *
     * @param context The Context the view is running in, through which it can
     *        access the current theme, resources, etc.
     * @param attrs The attributes of the XML tag that is inflating the view
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    @SuppressWarnings("UnusedDeclaration")
    public CustomTextClock(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * Creates a new clock inflated from XML. This object's properties are
     * intialized from the attributes specified in XML.
     *
     * @param context The Context the view is running in, through which it can
     *        access the current theme, resources, etc.
     * @param attrs The attributes of the XML tag that is inflating the view
     * @param defStyleAttr An attribute in the current theme that contains a
     *        reference to a style resource that supplies default values for
     *        the view. Can be 0 to not look for defaults.
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    public CustomTextClock(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public CustomTextClock(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);

        final TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.CustomTextClock, defStyleAttr, defStyleRes);
        saveAttributeDataForStyleable(context, R.styleable.CustomTextClock,
                attrs, a, defStyleAttr, defStyleRes);
        try {
            mFormat12 = a.getText(R.styleable.CustomTextClock_format12Hour);
            mFormat24 = a.getText(R.styleable.CustomTextClock_format24Hour);
            mTimeZone = a.getString(R.styleable.CustomTextClock_timeZone);
        } finally {
            a.recycle();
        }

        init();
    }

    private void init() {
        if (mFormat12 == null) {
            mFormat12 = getBestDateTimePattern("hm");
        }
        if (mFormat24 == null) {
            mFormat24 = getBestDateTimePattern("Hm");
        }

        createTime(mTimeZone);
        chooseFormat();
    }

    private void createTime(String timeZone) {
        if (timeZone != null) {
            mTime = Calendar.getInstance(TimeZone.getTimeZone(timeZone));
        } else {
            mTime = Calendar.getInstance();
        }
    }

    /**
     * Returns the formatting pattern used to display the date and/or time
     * in 12-hour mode. The formatting pattern syntax is described in
     * {@link DateFormat}.
     *
     * @return A {@link CharSequence} or null.
     *
     * @see #setFormat12Hour(CharSequence)
     * @see #is24HourModeEnabled()
     */
    @InspectableProperty
    @ExportedProperty
    public CharSequence getFormat12Hour() {
        return mFormat12;
    }

    public void setFormat12Hour(CharSequence format) {
        mFormat12 = format;

        chooseFormat();
        onTimeChanged();
    }

    /**
     * Like setFormat12Hour, but for the content description.
     * @hide
     */
    public void setContentDescriptionFormat12Hour(CharSequence format) {
        mDescFormat12 = format;

        chooseFormat();
        onTimeChanged();
    }

    /**
     * Returns the formatting pattern used to display the date and/or time
     * in 24-hour mode. The formatting pattern syntax is described in
     * {@link DateFormat}.
     *
     * @return A {@link CharSequence} or null.
     *
     * @see #setFormat24Hour(CharSequence)
     * @see #is24HourModeEnabled()
     */
    @InspectableProperty
    @ExportedProperty
    public CharSequence getFormat24Hour() {
        return mFormat24;
    }


    public void setFormat24Hour(CharSequence format) {
        mFormat24 = format;

        chooseFormat();
        onTimeChanged();
    }

    /**
     * Like setFormat24Hour, but for the content description.
     * @hide
     */
    public void setContentDescriptionFormat24Hour(CharSequence format) {
        mDescFormat24 = format;

        chooseFormat();
        onTimeChanged();
    }

    /**
     * Sets whether this clock should always track the current user and not the user of the
     * current process. This is used for single instance processes like the systemUI who need
     * to display time for different users.
     *
     * @hide
     */
    public void setShowCurrentUserTime(boolean showCurrentUserTime) {
        mShowCurrentUserTime = showCurrentUserTime;

        chooseFormat();
        onTimeChanged();
        unregisterObserver();
        registerObserver();
    }

    /**
     * Update the displayed time if necessary and invalidate the view.
     */
    public void refreshTime() {
        onTimeChanged();
        invalidate();
    }

    /**
     * Indicates whether the system is currently using the 24-hour mode.
     *
     * When the system is in 24-hour mode, this view will use the pattern
     * returned by {@link #getFormat24Hour()}. In 12-hour mode, the pattern
     * returned by {@link #getFormat12Hour()} is used instead.
     *
     * If either one of the formats is null, the other format is used. If
     * both formats are null, the default formats for the current locale are used.
     *
     * @return true if time should be displayed in 24-hour format, false if it
     *         should be displayed in 12-hour format.
     *
     * @see #setFormat12Hour(CharSequence)
     * @see #getFormat12Hour()
     * @see #setFormat24Hour(CharSequence)
     * @see #getFormat24Hour()
     */
    @InspectableProperty(hasAttributeId = false)
    public boolean is24HourModeEnabled() {
        if (mShowCurrentUserTime) {
            return DateFormat.is24HourFormat(getContext());
        } else {
            return DateFormat.is24HourFormat(getContext());
        }
    }

    /**
     * Indicates which time zone is currently used by this view.
     *
     * @return The ID of the current time zone or null if the default time zone,
     *         as set by the user, must be used
     *
     * @see TimeZone
     * @see TimeZone#getAvailableIDs()
     * @see #setTimeZone(String)
     */
    @InspectableProperty
    public String getTimeZone() {
        return mTimeZone;
    }

    public void setTimeZone(String timeZone) {
        mTimeZone = timeZone;

        createTime(timeZone);
        onTimeChanged();
    }


    public CharSequence getFormat() {
        return mFormat;
    }
    public  static final char    SECONDS                =    's';

    /**
     * Selects either one of {@link #getFormat12Hour()} or {@link #getFormat24Hour()}
     * depending on whether the user has selected 24-hour format.
     */
    private void chooseFormat() {
        final boolean format24Requested = is24HourModeEnabled();

        if (format24Requested) {
            mFormat = abc(mFormat24, mFormat12, getBestDateTimePattern("Hm"));
            mDescFormat = abc(mDescFormat24, mDescFormat12, mFormat);
        } else {
            mFormat = abc(mFormat12, mFormat24, getBestDateTimePattern("hm"));
            mDescFormat = abc(mDescFormat12, mDescFormat24, mFormat);
        }

        boolean hadSeconds = mHasSeconds;
        mHasSeconds = hasDesignator(mFormat,SECONDS);

        if (mShouldRunTicker && hadSeconds != mHasSeconds) {
            mTicker.run();
        }
    }
    public  static final char    QUOTE                  =    '\'';

    public static boolean hasDesignator(CharSequence inFormat, char designator) {
           if (inFormat == null) return false;

           final int length = inFormat.length();

           boolean insideQuote = false;
           for (int i = 0; i < length; i++) {
               final char c = inFormat.charAt(i);
               if (c == QUOTE) {
                   insideQuote = !insideQuote;
               } else if (!insideQuote) {
                   if (c == designator) {
                       return true;
                   }
               }
           }

           return false;
       }


    private String getBestDateTimePattern(String skeleton) {
        DateTimePatternGenerator dtpg = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            dtpg = DateTimePatternGenerator.getInstance(
                    getContext().getResources().getConfiguration().locale);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return dtpg.getBestPattern(skeleton);
        }
        return skeleton;
    }

    /**
     * Returns a if not null, else return b if not null, else return c.
     */
    private static CharSequence abc(CharSequence a, CharSequence b, CharSequence c) {
        return a == null ? (b == null ? c : b) : a;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (!mRegistered) {
            mRegistered = true;

            registerReceiver();
            registerObserver();

            createTime(mTimeZone);
        }
    }

    @Override
    public void onVisibilityAggregated(boolean isVisible) {
        super.onVisibilityAggregated(isVisible);

        if (!mShouldRunTicker && isVisible) {
            mShouldRunTicker = true;
            mTicker.run();
        } else if (mShouldRunTicker && !isVisible) {
            mShouldRunTicker = false;
            removeCallbacks(mTicker);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (mRegistered) {
            unregisterReceiver();
            unregisterObserver();

            mRegistered = false;
        }
    }


    private void registerReceiver() {
        final IntentFilter filter = new IntentFilter();

        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);

        // OK, this is gross but needed. This class is supported by the
        // remote views mechanism and as a part of that the remote views
        // can be inflated by a context for another user without the app
        // having interact users permission - just for loading resources.
        // For example, when adding widgets from a managed profile to the
        // home screen. Therefore, we register the receiver as the user
        // the app is running as not the one the context is for.
        getContext().registerReceiver(mIntentReceiver,filter );
    }

    private void registerObserver() {
        if (mRegistered) {
            if (mFormatChangeObserver == null) {
                mFormatChangeObserver = new FormatChangeObserver(getHandler());
            }
            final ContentResolver resolver = getContext().getContentResolver();
            Uri uri = Settings.System.getUriFor(Settings.System.TIME_12_24);
            if (mShowCurrentUserTime) {
                resolver.registerContentObserver(uri, true,
                        mFormatChangeObserver);
            } else {
                // UserHandle.myUserId() is needed. This class is supported by the
                // remote views mechanism and as a part of that the remote views
                // can be inflated by a context for another user without the app
                // having interact users permission - just for loading resources.
                // For example, when adding widgets from a managed profile to the
                // home screen. Therefore, we register the ContentObserver with the user
                // the app is running (e.g. the launcher) and not the user of the
                // context (e.g. the widget's profile).
                resolver.registerContentObserver(uri, true,
                        mFormatChangeObserver);
            }
        }
    }

    private void unregisterReceiver() {
        getContext().unregisterReceiver(mIntentReceiver);
    }

    private void unregisterObserver() {
        if (mFormatChangeObserver != null) {
            final ContentResolver resolver = getContext().getContentResolver();
            resolver.unregisterContentObserver(mFormatChangeObserver);
        }
    }

    /**
     * Update the displayed time if this view and its ancestors and window is visible
     */
    private void onTimeChanged() {
        mTime.setTimeInMillis(System.currentTimeMillis());
        setText(DateFormat.format(mFormat, mTime));
        setContentDescription(DateFormat.format(mDescFormat, mTime));
    }


}
