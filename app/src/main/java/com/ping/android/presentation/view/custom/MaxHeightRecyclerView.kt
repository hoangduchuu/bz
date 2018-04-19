package com.ping.android.presentation.view.custom

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import com.ping.android.R

class MaxHeightRecyclerView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyle: Int = 0
) : RecyclerView(context, attrs, defStyle) {

    private var maxHeight: Int = 0

    init {
        attrs.let {
            val typeArray = context.obtainStyledAttributes(it, R.styleable.MaxHeightRecyclerView, 0, 0)
            maxHeight = typeArray.getLayoutDimension(typeArray.getResourceId(R.styleable.MaxHeightRecyclerView_maxHeight, 0), 0)
            typeArray.recycle()
        }
    }

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        var newHeight = heightSpec
        if (maxHeight > 0) {
            newHeight = MeasureSpec.makeMeasureSpec(maxHeight, heightSpec)
        }
        super.onMeasure(widthSpec, newHeight)
    }
}