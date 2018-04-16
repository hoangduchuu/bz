package com.ping.android.presentation.view.custom

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout

class SquareLayout @JvmOverloads constructor(
        context: Context,
        attributeSet: AttributeSet?,
        defStyleAttr: Int = 0,
        defStyleRes: Int = 0
): LinearLayout(context, attributeSet, defStyleAttr, defStyleRes) {
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (widthMeasureSpec > heightMeasureSpec) {
            super.onMeasure(heightMeasureSpec, heightMeasureSpec)
        } else {
            super.onMeasure(widthMeasureSpec, widthMeasureSpec)
        }
    }
}