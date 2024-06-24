package com.nothing.commonutils.lifecycle;

import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;

public interface  ObserverNonNull<T> extends Observer<T> {
    @Override
    void onChanged(@NonNull T t);
}
