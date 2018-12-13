package com.ping.android.presentation.view.custom.gifs;

import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by Huu Hoang on 13/12/2018
 */
class GifItemDecorator(var space: Int) : RecyclerView.ItemDecoration() {

    /**
     * @space is  margin space
     */
    override fun getItemOffsets(outRect: Rect, view: View, recyclerview: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, recyclerview, state)
        if (recyclerview.getChildAdapterPosition(view) == 0) {
            return
        }
        outRect.left = space


    }
}