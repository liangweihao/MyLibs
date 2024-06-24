package com.nothing.commonutils.utils;


import android.os.Handler;
import android.os.Looper;

import com.nothing.commonutils.lifecycle.ObserverRunner;
import com.nothing.commonutils.livedata.TransformationExt;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.LifecycleOwner;

public class FragmentUtils {

    private static final String TAG = "FragmentUtils";
    public static void remove(FragmentManager fragmentManager, LifecycleOwner lifecycle, Fragment fragment){
        TransformationExt.runOnLifecycleSafe(lifecycle, new ObserverRunner() {
            @Override
            public void run() {
                Try.catchSelf(() -> fragmentManager.beginTransaction().remove(fragment).commit());

            }

            @Override
            public void dispose() {

            }
        });

    }

    public static void popBackStack(FragmentManager fragmentManager, LifecycleOwner lifecycle){
        TransformationExt.runOnLifecycleSafe(lifecycle, new ObserverRunner() {
            @Override
            public void run() {
                Try.catchSelf(() -> fragmentManager.popBackStackImmediate());

            }

            @Override
            public void dispose() {

            }
        });
    }


    public static void replace(FragmentManager fragmentManager,LifecycleOwner lifecycle,@IdRes int containerViewId, @NonNull Fragment fragment,
                               @Nullable String tag){
        TransformationExt.runOnLifecycleSafe(lifecycle, new ObserverRunner() {
            @Override
            public void run() {
                Try.catchSelf(() -> fragmentManager.beginTransaction().replace(containerViewId,
                        fragment,
                        tag).commit());

            }

            @Override
            public void dispose() {

            }
        });
    }

    public static void addPop(FragmentManager fragmentManager,LifecycleOwner lifecycle,@IdRes int containerViewId, @NonNull Fragment fragment,
                                     @Nullable String tag){
        TransformationExt.runOnLifecycleSafe(lifecycle, new ObserverRunner() {
            @Override
            public void run() {
                Try.catchSelf(() -> fragmentManager.beginTransaction().add(containerViewId,
                        fragment,
                        tag).addToBackStack(tag).commit());

            }

            @Override
            public void dispose() {

            }
        });
    }
    public static void replaceAddPop(FragmentManager fragmentManager,LifecycleOwner lifecycle,@IdRes int containerViewId, @NonNull Fragment fragment,
                               @Nullable String tag){
        TransformationExt.runOnLifecycleSafe(lifecycle, new ObserverRunner() {
            @Override
            public void run() {
                Try.catchSelf(() -> fragmentManager.beginTransaction().replace(containerViewId,
                        fragment,
                        tag).addToBackStack(tag).commit());

            }

            @Override
            public void dispose() {

            }
        });
    }

    public static boolean findTag(FragmentManager fragmentManager,String tag){
        return fragmentManager.findFragmentByTag(tag) != null;
    }
    private static Handler handler = new Handler(Looper.getMainLooper());
    public static void printFragment(FragmentManager fragmentManager){
        Lg.d(TAG,"printFragment " + fragmentManager);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try{
                    for (Fragment fragment : fragmentManager.getFragments()) {
                        Lg.d(TAG,"fragment entry " + fragment);

                    }

                    for (int i = 0; i < fragmentManager.getBackStackEntryCount(); i++) {
                        FragmentManager.BackStackEntry backStackEntryAt = fragmentManager.getBackStackEntryAt(
                                i);

                        Lg.d(TAG,"back entry " + backStackEntryAt);
                    }
                }catch (Throwable e){
                    e.printStackTrace();
                }

            }
        },0);

    }
}
