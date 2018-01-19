package com.tl.flexibleadapter.baseitems

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.tl.extensions.inflate
import com.tl.flexibleadapter.FlexibleItem
import com.tl.flexibleadapter.R

/**
 * Created by tuanluong on 10/18/17.
 */

class LoadingItem: FlexibleItem<LoadingItem.ViewHolder> {
    override var layoutId: Int = R.layout.loading_item

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        return ViewHolder(parent.inflate(layoutId))
    }

    override fun onBindViewHolder(holder: ViewHolder) {

    }

    class ViewHolder(view: View): RecyclerView.ViewHolder(view)
}