package com.ping.android.presentation.view.custom

import android.content.Context
import androidx.recyclerview.widget.GridLayoutManager

class GridNonScrollableLayoutManager(context: Context, spanCount: Int): androidx.recyclerview.widget.GridLayoutManager(context, spanCount) {
    override fun canScrollHorizontally(): Boolean {
        return false
    }

    override fun canScrollVertically(): Boolean {
        return false
    }

    override fun isAutoMeasureEnabled(): Boolean {
        return true
    }
}