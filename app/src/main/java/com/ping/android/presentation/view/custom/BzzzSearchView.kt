package com.ping.android.presentation.view.custom

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.support.v7.widget.AppCompatEditText
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import com.ping.android.activity.R

class BzzzSearchView : AppCompatEditText {
    private var leftDrawable: Drawable? = null
    private var xDrawable: Drawable? = null
    private var l: OnTouchListener? = null

    constructor(context: Context) : super(context) {
        this.initView()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        this.initView()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        this.initView()
    }

    private fun initView() {
        background = ContextCompat.getDrawable(context, R.drawable.bg_search)
        leftDrawable = ContextCompat.getDrawable(context, R.drawable.ic_search)
        this.setClearIconVisibility(false)
        setOnTouchListener { v, event ->
            if (event?.action == MotionEvent.ACTION_UP && xDrawable != null) {
                if (event.x > width - paddingRight - xDrawable!!.bounds.width()) {
                    setText("")
                    //event.action = MotionEvent.ACTION_CANCEL
                    return@setOnTouchListener false
                }
            }
            return@setOnTouchListener false
        }
    }

    override fun setCompoundDrawables(left: Drawable?, top: Drawable?, right: Drawable?, bottom: Drawable?) {
        xDrawable = right
        super.setCompoundDrawables(left, top, right, bottom)
    }

    override fun onTextChanged(text: CharSequence?, start: Int, lengthBefore: Int, lengthAfter: Int) {
        if (text != null && text.isNotEmpty()) {
            // show x button
            this.setClearIconVisibility(true)
        } else {
            // hide x button
            this.setClearIconVisibility(false)
        }
        super.onTextChanged(text, start, lengthBefore, lengthAfter)
    }

    private fun setClearIconVisibility(visible: Boolean) {
        val clearDrawable = when (visible) {
            true -> ContextCompat.getDrawable(context, R.drawable.ic_search_close)
            else -> null
        }
        setCompoundDrawablesWithIntrinsicBounds(leftDrawable, null, clearDrawable, null)
    }
}