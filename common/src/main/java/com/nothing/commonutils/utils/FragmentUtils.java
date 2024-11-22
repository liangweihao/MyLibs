package com.nothing.commonutils.utils;


import android.os.Handler;
import android.os.Looper;

import com.nothing.commonutils.lifecycle.ObserverRunner;
import com.nothing.commonutils.livedata.TransformationExt;

import java.util.ArrayList;
import java.util.Objects;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.LifecycleOwner;

/**
 * replace :  生命周期是上一个先 onCreate 然后再 onDestroy 如果是同一个 fragment 业务的会出现 onCreate 月以后会被 ondestroy 销毁了
 * remove + add : 他会先把上一个 fragment 的生命周期走完再走新的生命周期
 */
// TODO:LWH  2024/8/15 注意 工具类中如果同时执行了多个 commitNow 就会导致系统错误异常 注意
public class FragmentUtils {

    private static final String TAG = "FragmentUtils";

    @Nullable
    public static FragmentManager.BackStackEntry getTopBackEntry(FragmentManager fragmentManager) {
        if (fragmentManager.getBackStackEntryCount() > 0) {
            return fragmentManager.getBackStackEntryAt(
                    fragmentManager.getBackStackEntryCount() - 1);
        }
        return null;
    }


    @NonNull
    public static ArrayList<FragmentManager.BackStackEntry> getBackEntryList(FragmentManager fragmentManager){
        ArrayList<FragmentManager.BackStackEntry> backStackEntries = new ArrayList<>();
        for (int i = 0; i < fragmentManager.getBackStackEntryCount(); i++) {

            FragmentManager.BackStackEntry backStackEntryAt = fragmentManager.getBackStackEntryAt(
                    i);
            backStackEntries.add(backStackEntryAt);
        }
        return backStackEntries;
    }



    public static boolean isRootStackEntry(FragmentManager fragmentManager) {
        return fragmentManager.getBackStackEntryCount() == 1;
    }


    public static void remove(
            FragmentManager fragmentManager, LifecycleOwner lifecycle, Fragment fragment
    ) {
        TransformationExt.runOnLifecycleSafe(lifecycle, new ObserverRunner() {
            @Override
            public void run() {
                fragmentManager.beginTransaction().remove(fragment).commit();
            }

            @Override
            public void dispose() {

            }
        });
    }

    public static void remove(
            FragmentManager fragmentManager, LifecycleOwner lifecycle, String tag
    ) {
        TransformationExt.runOnLifecycleSafe(lifecycle, new ObserverRunner() {
            @Override
            public void run() {
                Fragment fragmentByTag = fragmentManager.findFragmentByTag(tag);
                if (fragmentByTag != null) {
                    fragmentManager.beginTransaction().remove(fragmentByTag).commit();
                }
            }

            @Override
            public void dispose() {

            }
        });
    }
    // TODO:LWH  2024/10/25 会推到指定的tag  如果使用POP_BACK_STACK_INCLUSIVE 就会回退到的fragment同时也会被销毁

    public static void popBackStack(FragmentManager fragmentManager, LifecycleOwner lifecycle) {
        TransformationExt.runOnLifecycleSafe(lifecycle, new ObserverRunner() {
            @Override
            public void run() {
                fragmentManager.popBackStack();
            }

            @Override
            public void dispose() {

            }
        });
    }

    // TODO:LWH  2024/10/25 会推到指定的tag  如果使用POP_BACK_STACK_INCLUSIVE 就会回退到的fragment同时也会被销毁
    public static void popBackStack(FragmentManager fragmentManager, LifecycleOwner lifecycle,String tag) {
        TransformationExt.runOnLifecycleSafe(lifecycle, new ObserverRunner() {
            @Override
            public void run() {
                // TODO:LWH  2024/8/15 这里注意 如果 popBackStackImmediate(tag, 0);  的方式 恰巧遇到当前 tag 在栈顶会导致退栈失败
                if (fragmentManager.getBackStackEntryCount() > 0) {
                    FragmentManager.BackStackEntry entry = fragmentManager.getBackStackEntryAt(fragmentManager.getBackStackEntryCount() - 1);
                    if (Objects.equals(entry.getName(),tag)) {
                        fragmentManager.popBackStack();
                    } else {
                        fragmentManager.popBackStack(tag,0);
                    }
                }
            }

            @Override
            public void dispose() {

            }
        });
    }
    // TODO:LWH  2024/10/25 会推到指定的tag  如果使用POP_BACK_STACK_INCLUSIVE 就会回退到的fragment同时也会被销毁

    public static void popBackStackAll(FragmentManager fragmentManager, LifecycleOwner lifecycle,String tag) {
        TransformationExt.runOnLifecycleSafe(lifecycle, new ObserverRunner() {
            @Override
            public void run() {
                fragmentManager.popBackStack(tag,FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }

            @Override
            public void dispose() {

            }
        });
    }

    // TODO:LWH  2024/10/25 会推到指定的tag  如果使用POP_BACK_STACK_INCLUSIVE 就会回退到的fragment同时也会被销毁

    public static void popBackStackImmediate(FragmentManager fragmentManager, LifecycleOwner lifecycle){
        TransformationExt.runOnLifecycleSafe(lifecycle, new ObserverRunner() {
            @Override
            public void run() {
                fragmentManager.popBackStackImmediate();
            }

            @Override
            public void dispose() {

            }
        });
    }
    // TODO:LWH  2024/10/25 会推到指定的tag  如果使用POP_BACK_STACK_INCLUSIVE 就会回退到的fragment同时也会被销毁

    public static void popBackStackImmediate(FragmentManager fragmentManager, LifecycleOwner lifecycle,@Nullable final String tag){
        TransformationExt.runOnLifecycleSafe(lifecycle, new ObserverRunner() {
            @Override
            public void run() {
                // TODO:LWH  2024/8/15 这里注意 如果 popBackStackImmediate(tag, 0);  的方式 恰巧遇到当前 tag 在栈顶会导致退栈失败
                if (fragmentManager.getBackStackEntryCount() > 0) {
                    FragmentManager.BackStackEntry entry = fragmentManager.getBackStackEntryAt(fragmentManager.getBackStackEntryCount() - 1);
                    if (Objects.equals(entry.getName(),tag)) {
                        fragmentManager.popBackStackImmediate();  // 弹出栈顶的 Fragment
                    } else {
                        fragmentManager.popBackStackImmediate(tag, 0);  // 其他情况下执行 pop 操作
                    }
                }
            }

            @Override
            public void dispose() {

            }
        });
    }

    public static void show(FragmentManager fragmentManager, LifecycleOwner lifecycle,@Nullable final String name){
        TransformationExt.runOnLifecycleSafe(lifecycle, new ObserverRunner() {
            @Override
            public void run() {
                Fragment fragmentByTag = fragmentManager.findFragmentByTag(name);
                if (fragmentByTag != null) {
                    fragmentManager.beginTransaction().show(fragmentByTag).commit();
                }
            }

            @Override
            public void dispose() {

            }
        });
    }


    public static void hide(FragmentManager fragmentManager, LifecycleOwner lifecycle,@Nullable final String name){
        TransformationExt.runOnLifecycleSafe(lifecycle, new ObserverRunner() {
            @Override
            public void run() {
                Fragment fragmentByTag = fragmentManager.findFragmentByTag(name);
                if (fragmentByTag != null) {
                    fragmentManager.beginTransaction().hide(fragmentByTag).commit();
                }
            }

            @Override
            public void dispose() {

            }
        });
    }

    /**
     * 退出所有 name 的 Fragment 栈
     * */
    public static void popBackStackImmediateAll(FragmentManager fragmentManager, LifecycleOwner lifecycle,@Nullable final String name){
        TransformationExt.runOnLifecycleSafe(lifecycle, new ObserverRunner() {
            @Override
            public void run() {
                fragmentManager.popBackStackImmediate(name, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }

            @Override
            public void dispose() {

            }
        });
    }

    public static void replace(
            FragmentManager fragmentManager, LifecycleOwner lifecycle, @IdRes int containerViewId,
            @NonNull Fragment fragment, @Nullable String tag
    ) {
        TransformationExt.runOnLifecycleSafe(lifecycle, new ObserverRunner() {
            @Override
            public void run() {
                fragmentManager.beginTransaction()
                               .replace(containerViewId, fragment, tag)
                               .commit();

            }

            @Override
            public void dispose() {

            }
        });
    }

    public static void addFragment(
            FragmentManager fragmentManager, LifecycleOwner lifecycle, @IdRes int containerViewId,
            @NonNull Fragment fragment, String tag
    ) {
        TransformationExt.runOnLifecycleSafe(lifecycle, new ObserverRunner() {
            @Override
            public void run() {
                fragmentManager.beginTransaction()
                               .add(containerViewId, fragment, tag)
                               .commit();
            }

            @Override
            public void dispose() {

            }
        });
    }

    public static void hide(
            FragmentManager fragmentManager, LifecycleOwner lifecycle, Fragment fragment
    ) {
        TransformationExt.runOnLifecycleSafe(lifecycle, new ObserverRunner() {
            @Override
            public void run() {
                fragmentManager.beginTransaction().hide(fragment).commit();
            }

            @Override
            public void dispose() {

            }
        });
    }

    public static void show(
            FragmentManager fragmentManager, LifecycleOwner lifecycle, Fragment fragment
    ) {
        TransformationExt.runOnLifecycleSafe(lifecycle, new ObserverRunner() {
            @Override
            public void run() {
                fragmentManager.beginTransaction().show(fragment).commit();
            }

            @Override
            public void dispose() {

            }
        });
    }

    public static void addPop(
            FragmentManager fragmentManager, LifecycleOwner lifecycle, @IdRes int containerViewId,
            @NonNull Fragment fragment, @Nullable String tag
    ) {
        TransformationExt.runOnLifecycleSafe(lifecycle, new ObserverRunner() {
            @Override
            public void run() {
                fragmentManager.beginTransaction()
                               .add(containerViewId, fragment, tag)
                               .addToBackStack(tag)
                               .commit();

            }

            @Override
            public void dispose() {

            }
        });
    }

    public static void replaceAddPop(
            FragmentManager fragmentManager, LifecycleOwner lifecycle, @IdRes int containerViewId,
            @NonNull Fragment fragment, @Nullable String tag
    ) {
        TransformationExt.runOnLifecycleSafe(lifecycle, new ObserverRunner() {
            @Override
            public void run() {
                fragmentManager.beginTransaction()
                               .replace(containerViewId, fragment, tag)
                               .addToBackStack(tag)
                               .commit();

            }

            @Override
            public void dispose() {

            }
        });
    }


    public static boolean checkRootFragmentAndFinish(FragmentActivity activity) {
        if (activity.getSupportFragmentManager().getBackStackEntryCount() == 0 &&
            !activity.isFinishing()) {
            activity.finish();
            return true;
        }
        return false;
    }

    public static boolean findTag(FragmentManager fragmentManager, String tag) {
        return fragmentManager.findFragmentByTag(tag) != null;
    }

    private static Handler handler = new Handler(Looper.getMainLooper());

    public static void printFragment(FragmentManager fragmentManager) {
        Lg.d(TAG, "printFragment " + fragmentManager);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    for (Fragment fragment : fragmentManager.getFragments()) {
                        Lg.d(TAG, "fragment entry " + fragment);

                    }

                    for (int i = 0; i < fragmentManager.getBackStackEntryCount(); i++) {
                        FragmentManager.BackStackEntry backStackEntryAt
                                = fragmentManager.getBackStackEntryAt(i);

                        Lg.d(TAG, "back entry " + backStackEntryAt);
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }

            }
        }, 0);

    }
}
