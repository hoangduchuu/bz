package com.ping.android.presentation.view.custom

import android.graphics.Rect
import android.support.annotation.DimenRes
import android.support.v7.widget.RecyclerView
import android.view.View
import com.bzzzchat.extensions.px

/**
 * Spacing between grid's items
 * The first row will have default spacing
 */
class GridItemDecoration(val spanCount: Int, @DimenRes val itemOffset: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect?, view: View?, parent: RecyclerView?, state: RecyclerView.State?) {
        super.getItemOffsets(outRect, view, parent, state)
        val itemPadding = parent?.context?.resources?.getDimensionPixelSize(itemOffset) ?: 0
        val position = parent?.getChildLayoutPosition(view) ?: -1
        if (position != -1) {
            var top = itemPadding
            if (position < spanCount) {
                top = 30.px
            }
            outRect?.set(itemPadding, top, itemPadding, itemPadding)
        }
    }
}
