package com.ping.android.presentation.view.flexibleitem.messages;

import android.annotation.SuppressLint;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.ping.android.presentation.view.custom.GestureDetectorListener;

/**
 * Created by tuanluong on 1/19/18.
 */

public abstract class BaseMessageViewHolder extends RecyclerView.ViewHolder implements GestureDetectorListener.GestureDetectorCallback {
    protected GestureDetectorListener gestureDetectorListener;

    public BaseMessageViewHolder(View itemView) {
        super(itemView);
    }

    @SuppressLint("ClickableViewAccessibility")
    protected void initGestureListener() {
        gestureDetectorListener = new GestureDetectorListener(this);
        GestureDetectorCompat mDetector = new GestureDetectorCompat(itemView.getContext(), gestureDetectorListener);
        View clickableView = getClickableView();
        if (clickableView != null) {
            clickableView.setOnTouchListener((view, motionEvent) -> mDetector.onTouchEvent(motionEvent));
        }
    }

    protected abstract View getClickableView();
}
