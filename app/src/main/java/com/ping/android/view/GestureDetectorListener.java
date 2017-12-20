package com.ping.android.view;

import android.os.Handler;
import android.os.Looper;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import java.util.Timer;
import java.util.TimerTask;

public class GestureDetectorListener implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {
    private String DEBUG_TAG = GestureDetectorListener.class.getSimpleName();
    private GestureDetectorCallback callback;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Timer timer = null;  //at class level;
    private int DELAY = 700;
    private boolean shouldHandleTouch = true;

    private static final long DOUBLE_CLICK_TIME_DELTA = 300;//milliseconds

    long lastClickTime = 0;

    public GestureDetectorListener(GestureDetectorCallback callback) {
        this.callback = callback;
    }

    public void setCallback(GestureDetectorCallback callback) {
        this.callback = callback;
    }
//
//    @Override
//    public void onClick(View v) {
//        if (shouldHandleDoubleClick()) {
//            long clickTime = System.currentTimeMillis();
//            if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
//                processDoubleClickEvent(v);
//            } else {
//                processSingleClickEvent(v);
//            }
//            lastClickTime = clickTime;
//        } else {
//            callback.(v);
//        }
//    }
//
//
//    public void processSingleClickEvent(final View v) {
//        final Handler handler = new Handler();
//        final Runnable mRunnable = () -> {
//            onSingleClick(v); //Do what ever u want on single click
//        };
//
//        TimerTask timertask = new TimerTask() {
//            @Override
//            public void run() {
//                handler.post(mRunnable);
//            }
//        };
//        timer = new Timer();
//        timer.schedule(timertask, DELAY);
//
//    }
//
//
//    public void processDoubleClickEvent(View v) {
//        if (timer != null) {
//            timer.cancel(); //Cancels Running Tasks or Waiting Tasks.
//            timer.purge();  //Frees Memory by erasing cancelled Tasks.
//        }
//        onDoubleClick(v);//Do what ever u want on Double Click
//    }

    @Override
    public boolean onDown(MotionEvent event) {
        Log.d(DEBUG_TAG,"onDown: " + event.toString());
        return true;
    }

    @Override
    public boolean onFling(MotionEvent event1, MotionEvent event2,
                           float velocityX, float velocityY) {
        Log.d(DEBUG_TAG, "onFling: " + event1.toString() + event2.toString());
        return true;
    }

    @Override
    public void onLongPress(MotionEvent event) {
        Log.d(DEBUG_TAG, "onLongPress: " + event.toString());
        if (shouldHandleTouch) {
            callback.onLongPress();
            shouldHandleTouch = false;
            delayTouch();
        }
    }

    private void delayTouch() {
        handler.postDelayed(() -> shouldHandleTouch = true, DELAY);
    }

    @Override
    public boolean onScroll(MotionEvent event1, MotionEvent event2, float distanceX,
                            float distanceY) {
        return true;
    }

    @Override
    public void onShowPress(MotionEvent event) {
        Log.d(DEBUG_TAG, "onShowPress: " + event.toString());
    }

    @Override
    public boolean onSingleTapUp(MotionEvent event) {
        Log.d(DEBUG_TAG, "onSingleTapUp: " + event.toString());
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent event) {
        Log.d(DEBUG_TAG, "onDoubleTap: " + event.toString());
        if (shouldHandleTouch) {
            callback.onDoubleTap();
            shouldHandleTouch = false;
            delayTouch();
        }
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent event) {
        Log.d(DEBUG_TAG, "onDoubleTapEvent: " + event.toString());
        return true;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent event) {
        Log.d(DEBUG_TAG, "onSingleTapConfirmed: " + event.toString());
        if (shouldHandleTouch) {
            callback.onSingleTap();
            shouldHandleTouch = false;
            delayTouch();
        }
        return false;
    }

    public interface GestureDetectorCallback {
        void onSingleTap();
        void onDoubleTap();
        void onLongPress();
    }
}
