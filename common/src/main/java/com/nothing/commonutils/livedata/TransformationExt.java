package com.nothing.commonutils.livedata;


import android.os.Looper;

import com.nothing.commonutils.lifecycle.ObserverDispose;
import com.nothing.commonutils.lifecycle.ObserverRunner;
import com.nothing.commonutils.utils.Try;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.util.Predicate;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

public class TransformationExt {


    public static <T> LiveData<T> filter(LiveData<List<T>> source, Predicate<T> function) {
        final MediatorLiveData<T> result = new MediatorLiveData<>();
        result.addSource(source, list -> {
            for (T t : list) {
                if (function.test(t)) {
                    result.setValue(t);
                }
            }
        });
        return result;
    }


    public static <T> void observerOnlyOnce(LiveData<T> source,
                                            LifecycleOwner owner,
                                            Observer<T> observer) {
        source.observe(owner, new Observer<T>() {
            @Override
            public void onChanged(T list) {
                source.removeObserver(this);
                observer.onChanged(list);
            }
        });
    }

    public static <T> void observerOnlyOnce(LiveData<T> source,

                                            Observer<T> observer) {
        source.observeForever(new Observer<T>() {
            @Override
            public void onChanged(T list) {
                source.removeObserver(this);
                observer.onChanged(list);
            }
        });
    }


    public static void addObserverOnDestroy(@NonNull Lifecycle lifecycle,
                                            @NonNull Runnable runnable) {
        lifecycle.addObserver((LifecycleEventObserver) (source, event) -> {
            if (event == Lifecycle.Event.ON_DESTROY) {
                runnable.run();
            }
        });
    }


    /**
     *
     * 解决粘性问题的新Livedata订阅
     * */
    public static <T> MediatorLiveData<T> switchNewLiveData(LiveData<T> source) {
        MediatorLiveData<T> mediatorLiveData = new MediatorLiveData<>();

        final boolean[] lastHasValue = {source.getValue() != null};

        mediatorLiveData.addSource(source, new Observer<T>() {
            @Override
            public void onChanged(T t) {
                if (lastHasValue[0]) {
                    lastHasValue[0] = false;
                } else {
                    mediatorLiveData.setValue(t);
                }

            }
        });
        return mediatorLiveData;
    }





    public static ObserverDispose runOnLifecycleSafe(LifecycleOwner owner, ObserverRunner runner){
        MutableLiveData<Object> objectMutableLiveData = new MutableLiveData<>();

        final boolean[] isDispose = {false};
        Observer<Object> observer = new Observer<Object>() {
            @Override
            public void onChanged(Object o) {
                objectMutableLiveData.removeObserver(this);
                Try.catchSelf(runner::run);
            }
        };

        ObserverDispose observerDispose = new ObserverDispose() {
            @Override
            public boolean isDispose() {
                return isDispose[0];
            }

            @Override
            public void dispose() {
                objectMutableLiveData.removeObserver(observer);
                if (!isDispose[0]) {
                    isDispose[0] = true;
                    Try.catchSelf(runner::dispose);
                }
            }
        };
        if (owner == null){
            objectMutableLiveData.observeForever( observer);
        }else {
            objectMutableLiveData.observe(owner, observer);
        }
        if (Looper.myLooper() == Looper.getMainLooper()){
            objectMutableLiveData.setValue(new Object());
        }else {
            objectMutableLiveData.postValue(new Object());
        }
        return observerDispose;
    }

}
