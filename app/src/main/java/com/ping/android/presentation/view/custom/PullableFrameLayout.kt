package com.ping.android.presentation.view.custom

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.widget.FrameLayout
import com.ping.android.utils.Log

class PullableFrameLayout @JvmOverloads constructor(
        context: Context,
        attributeSet: AttributeSet?,
        defStyleAttr: Int = 0,
        defStyleRes: Int = 0
) : FrameLayout(context, attributeSet, defStyleAttr, defStyleRes) {
    private var mTouchSlop: Int = 0
    private var mIsBeingDragged: Boolean = false
    private var mInitialMotionY: Float = 0.toFloat()

    private var listener: PullListener? = null

    init {
        mTouchSlop = ViewConfiguration.get(context).scaledTouchSlop
    }

    fun setListener(listener: PullListener) {
        this.listener = listener
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        val action = ev?.action
        // Always handle the case of the touch gesture being complete.
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            // Release the scroll.
            mIsBeingDragged = false

            return false // Do not intercept touch event, let the child handle it
        }
        when (action) {
            MotionEvent.ACTION_DOWN -> if (!mIsBeingDragged) {
                mInitialMotionY = ev.y
            }
            MotionEvent.ACTION_MOVE -> {
                if (mIsBeingDragged) {
                    // We're currently scrolling, so yes, intercept the
                    // touch event!
                    Log.d("Is being drag")
                    return true
                }
                // If the user has dragged her finger horizontally more than
                // the touch slop, start the scroll

                // left as an exercise for the reader
                val y = ev.y
                val yDiff = mInitialMotionY - y
                // Touch slop should be calculated using ViewConfiguration
                // constants.
                if (Math.abs(yDiff) > mTouchSlop) {
                    // Start scrolling!
                    mIsBeingDragged = true
                    Log.d("Is being drag $yDiff")
                    return true
                }
                Log.d("Is being drag false $yDiff")
            }
        }

        return super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val action = event?.action
        if (action == MotionEvent.ACTION_DOWN && !mIsBeingDragged) {
            mIsBeingDragged = true
            mInitialMotionY = event.y
            return true
        } else if (action == MotionEvent.ACTION_MOVE) {
            if (mIsBeingDragged) {
                val y = event.y
                val yDiff = y - mInitialMotionY
                listener?.onPullProgress(yDiff)
                //updateFrames(xDiff)
            }
        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            mIsBeingDragged = false
            listener?.onPullEnd()
//            if (callback != null) {
//                callback.onReset()
//            }
        }

        return super.onTouchEvent(event)
    }
}

interface PullListener {
    fun onPullStart()
    fun onPullProgress(diff: Float)
    fun onPullEnd()
}