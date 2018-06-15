package com.ping.android.presentation.view.custom

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout

interface KeyboardListener {
    fun onVisibleChange(visible: Boolean)
}

class KeyboardAwaredView @JvmOverloads constructor(
        context: Context,
        attributeSet: AttributeSet?,
        defStyleAttr: Int = 0,
        defStyleRes: Int = 0
) : RelativeLayout(context, attributeSet, defStyleAttr, defStyleRes) {
    var listener: KeyboardListener? = null

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val proposeHeight = MeasureSpec.getSize(heightMeasureSpec)
        if (proposeHeight != height) {
            val diff = height - proposeHeight
            if (Math.abs(diff) > 300) {
                listener?.onVisibleChange(proposeHeight < height)
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }
}