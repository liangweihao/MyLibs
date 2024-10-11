package com.nothing.commonutils.utils;


import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.util.Predicate;

public class ViewUtils {

    private static final String TAG = "ViewUtils";

    public interface HoverListener {

        HoverListener doOnEnter(Predicate<MotionEvent> event);

        HoverListener doOnExit(Predicate<MotionEvent> event);

        HoverListener doOnMove(Predicate<MotionEvent> event);
    }

    public static HoverListener listenHover(View view) {
        final Predicate<MotionEvent>[] enterEvent = new Predicate[1];
        final Predicate<MotionEvent>[] moveEvent = new Predicate[1];
        final Predicate<MotionEvent>[] exitEvent = new Predicate[1];
        String viewInfo = view.toString();
        HoverListener listener = new HoverListener() {

            @Override
            public HoverListener doOnEnter(Predicate<MotionEvent> event) {
                enterEvent[0] = event;
                return this;
            }

            @Override
            public HoverListener doOnExit(Predicate<MotionEvent> event) {
                exitEvent[0] = event;
                return this;
            }

            @Override
            public HoverListener doOnMove(Predicate<MotionEvent> event) {
                moveEvent[0] = event;
                return this;
            }

            @Override
            protected void finalize() throws Throwable {
                super.finalize();
                Lg.i(TAG, "%s listener hover finalize", viewInfo);
            }
        };
        view.setOnHoverListener(new View.OnHoverListener() {
            @Override
            public boolean onHover(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_HOVER_ENTER) {
                    return enterEvent[0] != null && enterEvent[0].test(event);
                } else if (event.getAction() == MotionEvent.ACTION_HOVER_MOVE) {
                    return moveEvent[0] != null && moveEvent[0].test(event);
                } else if (event.getAction() == MotionEvent.ACTION_HOVER_EXIT) {
                    return exitEvent[0] != null && exitEvent[0].test(event);
                }
                return false;
            }
        });
        return listener;
    }


    public interface TouchListener {

        TouchListener doOnDown(Predicate<MotionEvent> event);

        TouchListener doOnMove(Predicate<MotionEvent> event);

        TouchListener doOnUp(Predicate<MotionEvent> event);

        TouchListener doOnCancel(Predicate<MotionEvent> event);
    }

    public static TouchListener listenTouch(@NonNull View view) {
        final Predicate<MotionEvent>[] downEvent = new Predicate[1];
        final Predicate<MotionEvent>[] moveEvent = new Predicate[1];
        final Predicate<MotionEvent>[] upEvent = new Predicate[1];
        final Predicate<MotionEvent>[] cancelEvent = new Predicate[1];
        String viewInfo = view.toString();
        TouchListener listener = new TouchListener() {

            @Override
            public TouchListener doOnDown(Predicate<MotionEvent> event) {
                downEvent[0] = event;
                return this;
            }

            @Override
            public TouchListener doOnMove(Predicate<MotionEvent> event) {
                moveEvent[0] = event;
                return this;
            }

            @Override
            public TouchListener doOnUp(Predicate<MotionEvent> event) {
                upEvent[0] = event;
                return this;
            }

            @Override
            public TouchListener doOnCancel(Predicate<MotionEvent> event) {
                cancelEvent[0] = event;
                return this;
            }

            @Override
            protected void finalize() throws Throwable {
                super.finalize();
                Lg.i(TAG, "%s listener touch finalize", viewInfo);
            }
        };
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    return downEvent[0] != null && downEvent[0].test(event);
                } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    return moveEvent[0] != null && moveEvent[0].test(event);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    return upEvent[0] != null && upEvent[0].test(event);
                } else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
                    return cancelEvent[0] != null && cancelEvent[0].test(event);
                }
                return false;
            }
        });
        return listener;
    }


}
