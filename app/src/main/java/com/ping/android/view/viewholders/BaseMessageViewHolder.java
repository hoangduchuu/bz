package com.ping.android.view.viewholders;

import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.ping.android.view.GestureDetectorListener;

/**
 * Created by tuanluong on 1/19/18.
 */

public abstract class BaseMessageViewHolder extends RecyclerView.ViewHolder implements GestureDetectorListener.GestureDetectorCallback {
    protected GestureDetectorListener gestureDetectorListener;

    public BaseMessageViewHolder(View itemView) {
        super(itemView);
    }

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
