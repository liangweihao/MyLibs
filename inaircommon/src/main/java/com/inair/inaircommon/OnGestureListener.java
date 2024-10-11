package com.inair.inaircommon;


import android.view.MotionEvent;

import androidx.annotation.NonNull;

public interface OnGestureListener {

    boolean onDown(@NonNull MotionEvent e);

    void onShowPress(@NonNull MotionEvent e);

    boolean onSingleTapUp(@NonNull MotionEvent e);

    boolean onScroll(
            @NonNull MotionEvent e1, @NonNull MotionEvent e2, float distanceX, float distanceY
    );

    void onLongPress(@NonNull MotionEvent e);

    boolean onFling(
            @androidx.annotation.NonNull MotionEvent e1, @NonNull MotionEvent e2, float velocityX,
            float velocityY
    );
}