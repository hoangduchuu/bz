package com.ping.android.presentation.view.flexibleitem.messages;

import android.annotation.SuppressLint;
import androidx.annotation.Nullable;
import androidx.core.view.GestureDetectorCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;

import com.ping.android.presentation.view.custom.GestureDetectorListener;

/**
 * Created by tuanluong on 1/19/18.
 */

public abstract class BaseMessageViewHolder extends RecyclerView.ViewHolder implements GestureDetectorListener.GestureDetectorCallback {
    protected GestureDetectorListener gestureDetectorListener;
    GestureDetectorCompat mDetector;

    public BaseMessageViewHolder(View itemView) {
        super(itemView);
    }

    @SuppressLint("ClickableViewAccessibility")
    protected void initGestureListener() {
        gestureDetectorListener = new GestureDetectorListener(this);
        mDetector = new GestureDetectorCompat(itemView.getContext(), gestureDetectorListener);
        View clickableView = getClickableView();
        if (clickableView != null) {
            clickableView.setOnTouchListener((view, motionEvent) -> {
                //view.performClick();
                return handleTouchEvent(motionEvent);
            });
        }
    }

    protected boolean handleTouchEvent(MotionEvent motionEvent) {
        return mDetector.onTouchEvent(motionEvent);
    }

    protected abstract @Nullable View getClickableView();
}
