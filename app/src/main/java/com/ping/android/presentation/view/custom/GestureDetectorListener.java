package com.ping.android.presentation.view.custom;

import android.os.Handler;
import android.os.Looper;
import android.view.GestureDetector;
import android.view.MotionEvent;

public class GestureDetectorListener implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {
    private String DEBUG_TAG = GestureDetectorListener.class.getSimpleName();
    private GestureDetectorCallback callback;
    private Handler handler = new Handler(Looper.getMainLooper());
    private int DELAY = 700;
    private boolean shouldHandleTouch = true;

    public GestureDetectorListener(GestureDetectorCallback callback) {
        this.callback = callback;
    }

    public void setCallback(GestureDetectorCallback callback) {
        this.callback = callback;
    }

    @Override
    public boolean onDown(MotionEvent event) {
        //Log.d("onDown: " + event.toString());
        return true;
    }

    @Override
    public boolean onFling(MotionEvent event1, MotionEvent event2,
                           float velocityX, float velocityY) {
        //Log.d("onFling: " + event1.toString() + event2.toString());
        return true;
    }

    @Override
    public void onLongPress(MotionEvent event) {
        //Log.d("onLongPress: " + event.toString());
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
        //Log.d("onShowPress: " + event.toString());
    }

    @Override
    public boolean onSingleTapUp(MotionEvent event) {
        //Log.d("onSingleTapUp: " + event.toString());
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent event) {
        //Log.d("onDoubleTap: " + event.toString());
        if (shouldHandleTouch) {
            callback.onDoubleTap();
            shouldHandleTouch = false;
            delayTouch();
        }
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent event) {
        //Log.d(DEBUG_TAG, "onDoubleTapEvent: " + event.toString());
        return true;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent event) {
        //Log.d("onSingleTapConfirmed: " + event.toString());
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
