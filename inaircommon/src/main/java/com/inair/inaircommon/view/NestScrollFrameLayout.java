package com.inair.inaircommon.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.inair.inaircommon.VibratorUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.NestedScrollingParent2;
import androidx.core.view.ViewCompat;

public class NestScrollFrameLayout extends FrameLayout implements NestedScrollingParent2 {
    public NestScrollFrameLayout(@NonNull Context context) {
        this(context, null);
    }

    private static final String TAG = "NestScrollFrameLayout";

    public NestScrollFrameLayout(
            @NonNull Context context, @Nullable AttributeSet attrs
    ) {
        this(context, attrs, -1);
    }

    public NestScrollFrameLayout(
            @NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr
    ) {
        this(context, attrs, defStyleAttr, -1);
    }

    private boolean nestedVibrator = true;

    public NestScrollFrameLayout(
            @NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr,
            int defStyleRes
    ) {
        super(context, attrs, defStyleAttr, defStyleRes);


    }


    @Override
    public boolean onStartNestedScroll(
            @NonNull View child, @NonNull View target, int axes, int type
    ) {
        if (type == ViewCompat.TYPE_TOUCH) {
            printScrollState = 0;
        }
        return true;
    }

    @Override
    public void onNestedScrollAccepted(
            @NonNull View child, @NonNull View target, int axes, int type
    ) {
    }

    @Override
    public void onStopNestedScroll(@NonNull View target, int type) {

    }

    private int printScrollState = 0;

    @Override
    public void onNestedScroll(
            @NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed,
            int dyUnconsumed, int type
    ) {
        if (dyUnconsumed != 0 && dyConsumed == 0) {
            if (printScrollState == 0 || printScrollState == 2) {
                printScrollState = 1;
                if (nestedVibrator) {
                    VibratorUtils.performTickClick(this);
                }
            }
        } else if (dyConsumed != 0) {
            if (printScrollState == 0 || printScrollState == 1) {
                printScrollState = 2;
            }
        }
    }

    @Override
    public void onNestedPreScroll(
            @NonNull View target, int dx, int dy, @NonNull int[] consumed, int type
    ) {


    }
}
