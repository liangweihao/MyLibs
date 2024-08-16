package com.nothing.commonutils.utils;


import android.app.Activity;
import android.app.Application;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

public class ApplicationLivecycleListener
        implements Application.ActivityLifecycleCallbacks, ComponentCallbacks2 {

    public static boolean Enable = true;
    private static final String TAG = "ApplicationLivecycle";
    FragmentManager.FragmentLifecycleCallbacks callback
            = new FragmentManager.FragmentLifecycleCallbacks() {
        @Override
        public void onFragmentPreAttached(
                @NonNull FragmentManager fm, @NonNull Fragment f, @NonNull Context context
        ) {
            super.onFragmentPreAttached(fm, f, context);
        }

        @Override
        public void onFragmentAttached(
                @NonNull FragmentManager fm, @NonNull Fragment f, @NonNull Context context
        ) {
            super.onFragmentAttached(fm, f, context);
            if (Enable) Lg.i(TAG, "FragmentChanged: %s@%d%s:%s", f.getClass().getSimpleName(),
                             f.hashCode(), String.valueOf(f.getTag()), "Attached");
        }

        @Override
        public void onFragmentPreCreated(
                @NonNull FragmentManager fm, @NonNull Fragment f,
                @Nullable Bundle savedInstanceState
        ) {
            super.onFragmentPreCreated(fm, f, savedInstanceState);
        }

        @Override
        public void onFragmentCreated(
                @NonNull FragmentManager fm, @NonNull Fragment f,
                @Nullable Bundle savedInstanceState
        ) {
            super.onFragmentCreated(fm, f, savedInstanceState);
            if (Enable) Lg.i(TAG, "FragmentChanged: %s@%d%s:%s", f.getClass().getSimpleName(),
                             f.hashCode(), String.valueOf(f.getTag()), "Created");
        }

        @Override
        public void onFragmentActivityCreated(
                @NonNull FragmentManager fm, @NonNull Fragment f,
                @Nullable Bundle savedInstanceState
        ) {
            super.onFragmentActivityCreated(fm, f, savedInstanceState);
        }

        @Override
        public void onFragmentViewCreated(
                @NonNull FragmentManager fm, @NonNull Fragment f, @NonNull View v,
                @Nullable Bundle savedInstanceState
        ) {
            super.onFragmentViewCreated(fm, f, v, savedInstanceState);
            if (Enable) Lg.i(TAG, "FragmentChanged: %s@%d:%s:%s", f.getClass().getSimpleName(),
                             f.hashCode(), String.valueOf(f.getTag()), "ViewCreated");
        }

        @Override
        public void onFragmentStarted(@NonNull FragmentManager fm, @NonNull Fragment f) {
            super.onFragmentStarted(fm, f);
            if (Enable) Lg.i(TAG, "FragmentChanged: %s@%d:%s:%s", f.getClass().getSimpleName(),
                             f.hashCode(), String.valueOf(f.getTag()), "Started");
        }

        @Override
        public void onFragmentResumed(@NonNull FragmentManager fm, @NonNull Fragment f) {
            super.onFragmentResumed(fm, f);
            if (Enable) Lg.i(TAG, "FragmentChanged: %s@%d:%s:%s", f.getClass().getSimpleName(),
                             f.hashCode(), String.valueOf(f.getTag()), "Resumed");
        }

        @Override
        public void onFragmentPaused(@NonNull FragmentManager fm, @NonNull Fragment f) {
            super.onFragmentPaused(fm, f);
            if (Enable) Lg.i(TAG, "FragmentChanged: %s@%d:%s:%s", f.getClass().getSimpleName(),
                             f.hashCode(), String.valueOf(f.getTag()), "Paused");
        }

        @Override
        public void onFragmentStopped(@NonNull FragmentManager fm, @NonNull Fragment f) {
            super.onFragmentStopped(fm, f);
            if (Enable) Lg.i(TAG, "FragmentChanged: %s@%d:%s:%s", f.getClass().getSimpleName(),
                             f.hashCode(), String.valueOf(f.getTag()), "Stopped");
        }

        @Override
        public void onFragmentSaveInstanceState(
                @NonNull FragmentManager fm, @NonNull Fragment f, @NonNull Bundle outState
        ) {
            super.onFragmentSaveInstanceState(fm, f, outState);
            if (Enable) Lg.i(TAG, "FragmentChanged: %s@%d:%s:%s", f.getClass().getSimpleName(),
                             f.hashCode(), String.valueOf(f.getTag()), "SaveInstanceState");
        }

        @Override
        public void onFragmentViewDestroyed(
                @NonNull FragmentManager fm, @NonNull Fragment f
        ) {
            super.onFragmentViewDestroyed(fm, f);
            if (Enable) Lg.i(TAG, "FragmentChanged: %s@%d:%s:%s", f.getClass().getSimpleName(),
                             f.hashCode(), String.valueOf(f.getTag()), "ViewDestroyed");
        }

        @Override
        public void onFragmentDestroyed(@NonNull FragmentManager fm, @NonNull Fragment f) {
            super.onFragmentDestroyed(fm, f);
            if (Enable) Lg.i(TAG, "FragmentChanged: %s@%d:%s:%s", f.getClass().getSimpleName(),
                             f.hashCode(), String.valueOf(f.getTag()), "Destroyed");
        }

        @Override
        public void onFragmentDetached(@NonNull FragmentManager fm, @NonNull Fragment f) {
            super.onFragmentDetached(fm, f);
            if (Enable) Lg.i(TAG, "FragmentChanged: %s@%d:%s:%s", f.getClass().getSimpleName(),
                             f.hashCode(), String.valueOf(f.getTag()), "Detached");
        }
    };
    private Map<Activity,FragmentManager.OnBackStackChangedListener> stackListeners = new HashMap<>();

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        if (activity instanceof FragmentActivity) {
            FragmentManager supportFragmentManager
                    = ((FragmentActivity) activity).getSupportFragmentManager();
            supportFragmentManager.registerFragmentLifecycleCallbacks(callback, true);
            FragmentManager.OnBackStackChangedListener stackListener = new FragmentManager.OnBackStackChangedListener() {
                @Override
                public void onBackStackChanged() {
                    if (supportFragmentManager.getBackStackEntryCount() > 0) {
                        FragmentManager.BackStackEntry topEntry
                                = supportFragmentManager.getBackStackEntryAt(
                                supportFragmentManager.getBackStackEntryCount() - 1);
                        Lg.i(TAG,"BackStackChanged:%s:%s",activity.getLocalClassName(),String.valueOf(topEntry.getName()));
                    }
                }
            };
            stackListeners.put(activity,stackListener);
            supportFragmentManager.addOnBackStackChangedListener(stackListener);
        }
        if (Enable) {
            Lg.i(TAG, "ActivityCreate: %s@%d", activity.getLocalClassName(), activity.hashCode());
        }
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        if (Enable) {
            Lg.i(TAG, "ActivityStarted: %s@%d", activity.getLocalClassName(), activity.hashCode());
        }
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        if (Enable) {
            Lg.i(TAG, "ActivityResumed: %s@%d", activity.getLocalClassName(), activity.hashCode());
        }
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        if (Enable) {
            Lg.i(TAG, "ActivityPaused: %s@%d", activity.getLocalClassName(), activity.hashCode());
        }
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        if (Enable) {
            Lg.i(TAG, "ActivityStopped: %s@%d", activity.getLocalClassName(), activity.hashCode());
        }
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
        if (Enable) {
            Lg.i(TAG, "ActivitySaveInstanceState: %s@%d", activity.getLocalClassName(),
                 activity.hashCode());
        }
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        if (Enable) {
            Lg.i(TAG, "ActivityDestroy: %s@%d", activity.getLocalClassName(), activity.hashCode());
        }
        if (activity instanceof FragmentActivity) {
            FragmentManager supportFragmentManager
                    = ((FragmentActivity) activity).getSupportFragmentManager();
            supportFragmentManager.unregisterFragmentLifecycleCallbacks(callback);
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        if (Enable) {
            Lg.i(TAG, "ConfigurationChanged: %s@%d", newConfig.toString(), newConfig.hashCode());
        }
    }

    @Override
    public void onLowMemory() {
        if (Enable) {
            Lg.i(TAG, "LowMemory: ");
        }
    }

    @Override
    public void onTrimMemory(int level) {
        if (Enable) {
            Lg.i(TAG, "TrimMemory: %d", level);
        }
    }
}
