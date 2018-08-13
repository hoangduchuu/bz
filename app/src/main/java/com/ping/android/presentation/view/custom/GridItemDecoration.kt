package com.ping.android.presentation.view.custom

import android.graphics.Rect
import androidx.annotation.DimenRes
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import com.bzzzchat.extensions.px

/**
 * Spacing between grid's items
 * The first row will have default spacing
 */
class GridItemDecoration(var spanCount: Int, @DimenRes val itemOffset: Int, val topSpace: Int = 30) : androidx.recyclerview.widget.RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect?, view: View?, parent: androidx.recyclerview.widget.RecyclerView?, state: androidx.recyclerview.widget.RecyclerView.State?) {
        super.getItemOffsets(outRect, view, parent, state)
        val itemPadding = parent?.context?.resources?.getDimensionPixelSize(itemOffset) ?: 0
        val position = parent?.getChildLayoutPosition(view) ?: -1
        if (position != -1) {
            var top = itemPadding
            if (position < spanCount) {
                top = topSpace.px
            }
            outRect?.set(itemPadding, top, itemPadding, itemPadding)
        }
    }
}
