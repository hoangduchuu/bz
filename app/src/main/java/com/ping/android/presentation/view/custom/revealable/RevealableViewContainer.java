package com.ping.android.presentation.view.custom.revealable;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.ping.android.utils.Log;

public class RevealableViewContainer extends RelativeLayout {
    private int mTouchSlop;
    private boolean mIsBeingDragged;
    private float mInitialMotionX;

    private RevealableCallback callback;

    public RevealableViewContainer(Context context) {
        super(context);
        init(context);
    }

    public RevealableViewContainer(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public RevealableViewContainer(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public void init(Context context) {
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        //mTouchSlop = 50;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        final int action = e.getAction();
        // Always handle the case of the touch gesture being complete.
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            // Release the scroll.
            mIsBeingDragged = false;

            if (callback != null) {
                callback.onReset();
            }
            return false; // Do not intercept touch event, let the child handle it
        }
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (!mIsBeingDragged) {
                    mInitialMotionX = e.getX();
                }
                break;
            case MotionEvent.ACTION_MOVE: {
                if (mIsBeingDragged) {
                    // We're currently scrolling, so yes, intercept the
                    // touch event!
                    return true;
                }
                // If the user has dragged her finger horizontally more than
                // the touch slop, start the scroll

                // left as an exercise for the reader
                final float x = e.getX();
                final float xDiff = mInitialMotionX - x;
                // Touch slop should be calculated using ViewConfiguration
                // constants.
                if (xDiff > mTouchSlop) {
                    // Start scrolling!
                    mIsBeingDragged = true;
                    return true;
                }
                break;
            }
        }

        return false;
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent ev) {
        int action = ev.getAction();
        Log.e("HEHEHE action " + action);
//        if (action == MotionEvent.ACTION_DOWN && !mIsBeingDragged) {
//            mIsBeingDragged = true;
//            mInitialMotionX = ev.getX();
//            return true;
//        } else
        if (action == MotionEvent.ACTION_MOVE) {
            if (mIsBeingDragged) {
                final float x = ev.getX();
                final float xDiff = x - mInitialMotionX;
                updateFrames(xDiff);
            }
        } else if (action == MotionEvent.ACTION_UP) {
            mIsBeingDragged = false;
            if (callback != null) {
                callback.onReset();
            }
        } else if (action == MotionEvent.ACTION_CANCEL) {
            mIsBeingDragged = false;
            if (callback != null) {
                callback.onReset();
            }
        }

        return true;
    }

    public void setCallback(RevealableCallback callback) {
        this.callback = callback;
    }

    private void updateFrames(float xDiff) {
        if (callback != null) {
            callback.onDragged(xDiff);
        }
    }

    public interface RevealableCallback {
        void onDragged(float xDiff);

        void onReset();
    }
}
