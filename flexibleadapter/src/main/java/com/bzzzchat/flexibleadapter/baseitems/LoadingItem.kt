package com.bzzzchat.flexibleadapter.baseitems

import android.view.View
import android.view.ViewGroup
import com.bzzzchat.extensions.inflate
import com.bzzzchat.flexibleadapter.FlexibleItem
import com.bzzzchat.flexibleadapter.R

/**
 * Created by tuanluong on 10/18/17.
 */

class LoadingItem: FlexibleItem<LoadingItem.ViewHolder> {
    override var layoutId: Int = R.layout.loading_item

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        return ViewHolder(parent.inflate(layoutId))
    }

    override fun onBindViewHolder(holder: ViewHolder, lastItem: Boolean) {

    }

    class ViewHolder(view: View): androidx.recyclerview.widget.RecyclerView.ViewHolder(view)
}