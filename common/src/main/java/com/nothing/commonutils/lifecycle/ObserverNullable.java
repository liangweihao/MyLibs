package com.nothing.commonutils.lifecycle;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;

public interface ObserverNullable<T> extends Observer<T> {

    @Override
    void onChanged(@Nullable T t);
}
