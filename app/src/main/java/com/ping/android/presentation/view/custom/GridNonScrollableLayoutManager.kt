package com.ping.android.presentation.view.custom

import android.content.Context
import android.support.v7.widget.GridLayoutManager

class GridNonScrollableLayoutManager(context: Context, spanCount: Int): GridLayoutManager(context, spanCount) {
    override fun canScrollHorizontally(): Boolean {
        return false
    }

    override fun canScrollVertically(): Boolean {
        return false
    }
}