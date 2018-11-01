package com.ping.android.presentation.view.custom

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.util.AttributeSet
import android.widget.ScrollView
import com.ping.android.R

class MaxHeightScrollView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyle: Int = 0
) : ScrollView(context, attrs, defStyle) {

    private var maxHeight: Int = 0

    init {
        attrs.let {
            val typeArray = context.obtainStyledAttributes(it, R.styleable.MaxHeightScrollView, 0, 0)
            maxHeight = typeArray.getLayoutDimension(typeArray.getResourceId(R.styleable.MaxHeightScrollView_maxScrollViewHeight, 0), 0)
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