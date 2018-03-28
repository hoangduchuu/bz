/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ping.android.presentation.view.custom;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link FrameLayout} that allows the user to drag and reposition child views.
 */
public class DragFrameLayout extends FrameLayout {
    private static final float MAX_X_MOVE = 70;
    private static final float MAX_Y_MOVE = 70;
    private final double AUTO_OPEN_SPEED_LIMIT = 800.0;

    /**
     * The list of {@link View}s that will be draggable.
     */
    private List<View> mDragViews;

    /**
     * The {@link DragFrameLayoutController} that will be notify on drag.
     */
    private DragFrameLayoutController mDragFrameLayoutController;

    private ViewDragHelper mDragHelper;

    private View draggableView;
    private int mDraggingState = 0;
    private int mDraggingLeft;
    private int mDraggingTop;
    private int mVerticalRange;
    private int mHorizontalRange;

    private float deltaX;
    private float deltaY;

    public DragFrameLayout(Context context) {
        this(context, null, 0, 0);
    }

    public DragFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0, 0);
    }

    public DragFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public DragFrameLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mDragViews = new ArrayList<View>();

        /**
         * Create the {@link ViewDragHelper} and set its callback.
         */
        mDragHelper = ViewDragHelper.create(this, 1.0f, new ViewDragHelper.Callback() {
            @Override
            public void onViewDragStateChanged(int state) {
                if (state == mDraggingState) { // no change
                    return;
                }
//                if ((mDraggingState == ViewDragHelper.STATE_DRAGGING || mDraggingState == ViewDragHelper.STATE_SETTLING) &&
//                        state == ViewDragHelper.STATE_IDLE) {
//                    // the view stopped from moving.
//
//                    if (mDraggingLeft == 0) {
//                        onStopDraggingToClosed();
//                    } else if (mDraggingLeft == mVerticalRange) {
//                        mIsOpen = true;
//                    }
//                }
//                if (state == ViewDragHelper.STATE_DRAGGING) {
//                    onStartDragging();
//                }
                mDraggingState = state;
            }

            @Override
            public boolean tryCaptureView(View child, int pointerId) {
                return mDragViews.contains(child);
            }

            @Override
            public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
                draggableView = changedView;
                mVerticalRange = getHeight() - changedView.getHeight();
                mHorizontalRange = getWidth() - changedView.getWidth();
                super.onViewPositionChanged(changedView, left, top, dx, dy);
                mDraggingLeft = left;
                mDraggingTop = top;
            }

            @Override
            public int clampViewPositionHorizontal(View child, int left, int dx) {
                final int leftBound = getPaddingStart();
                final int rightBound = mHorizontalRange;
                return Math.min(Math.max(left, leftBound), rightBound);
            }

            @Override
            public int clampViewPositionVertical(View child, int top, int dy) {
                //return top;
                final int topBound = getPaddingTop();
                final int bottomBound = mVerticalRange;
                return Math.min(Math.max(top, topBound), bottomBound);
            }

            @Override
            public void onViewCaptured(View capturedChild, int activePointerId) {
                super.onViewCaptured(capturedChild, activePointerId);
                if (mDragFrameLayoutController != null) {
                    mDragFrameLayoutController.onDragDrop(true);
                }
            }

            @Override
            public void onViewReleased(View releasedChild, float xvel, float yvel) {
                final float rangeToCheck = mHorizontalRange;
                boolean settleToOpen = false;
                if (mDraggingLeft > rangeToCheck / 2) {
                    settleToOpen = true;
                } else if (mDraggingLeft < rangeToCheck / 2) {
                    settleToOpen = false;
                }

                int margin = ((LayoutParams) releasedChild.getLayoutParams()).leftMargin;
                final int settleDestX = settleToOpen ? mHorizontalRange - margin : margin;
                final int settleDestY = mDraggingTop < margin ? margin
                        : (mDraggingTop > (mVerticalRange - margin) ? mVerticalRange - margin : mDraggingTop);
                if (mDragHelper.settleCapturedViewAt(settleDestX, settleDestY)) {
                    ViewCompat.postInvalidateOnAnimation(DragFrameLayout.this);
                }
                if (mDragFrameLayoutController != null) {
                    mDragFrameLayoutController.onDragDrop(false);
                }
            }

            @Override
            public int getViewVerticalDragRange(@NonNull View child) {
                return mVerticalRange;
            }

            @Override
            public int getViewHorizontalDragRange(@NonNull View child) {
                return mHorizontalRange;
            }
        });
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mVerticalRange = h;
        if (draggableView != null) {
            mVerticalRange = h - draggableView.getHeight();
        }
        mHorizontalRange = w;
        if (draggableView != null) {
            mHorizontalRange = w - draggableView.getWidth();
        }
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (draggableView != null) {
            int margin = ((LayoutParams) draggableView.getLayoutParams()).leftMargin;
            int rangeToCheck = mHorizontalRange / 2;
            int settleX = mDraggingLeft - margin;
            int settleY = mDraggingTop - margin;
            if (rangeToCheck > margin && settleX > rangeToCheck) {
                settleX = getWidth() - draggableView.getWidth() - 2 * margin;
            } else {
                settleX = 0;
            }
            int verticalPoint = mVerticalRange - 2 * margin;
            if (verticalPoint > margin && settleY > verticalPoint) {
                settleY = verticalPoint;
            }
            draggableView.offsetLeftAndRight(settleX);
            draggableView.offsetTopAndBottom(settleY);
        }
    }

    @Override
    public void computeScroll() {
        if (mDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = ev.getActionMasked();
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            mDragHelper.cancel();
            return false;
        }
        return mDragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        mDragHelper.processTouchEvent(ev);
        final int action = ev.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN) {
            deltaX = ev.getX();
            deltaY = ev.getY();
        }
        if (action == MotionEvent.ACTION_MOVE) {
            if (Math.abs(ev.getX() - deltaX) > MAX_X_MOVE && Math.abs(ev.getY() - deltaY) > MAX_Y_MOVE) {
                deltaX = Float.MAX_VALUE;
                deltaY = Float.MAX_VALUE;
            }
        }
        if (action == MotionEvent.ACTION_UP) {
            if (Math.abs(ev.getX() - deltaX) < MAX_X_MOVE && Math.abs(ev.getY() - deltaY) < MAX_Y_MOVE) {
                performClick();
            }
        }
        return true;
    }

    /**
     * Adds a new {@link View} to the list of views that are draggable within the container.
     *
     * @param dragView the {@link View} to make draggable
     */
    public void addDragView(View dragView) {
        mDragViews.add(dragView);
    }

    /**
     * Sets the {@link DragFrameLayoutController} that will receive the drag events.
     *
     * @param dragFrameLayoutController a {@link DragFrameLayoutController}
     */
    public void setDragFrameController(DragFrameLayoutController dragFrameLayoutController) {
        mDragFrameLayoutController = dragFrameLayoutController;
    }

    /**
     * A controller that will receive the drag events.
     */
    public interface DragFrameLayoutController {

        public void onDragDrop(boolean captured);
    }
}
