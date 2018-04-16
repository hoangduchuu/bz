package com.ping.android.presentation.view.adapter.delegate

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.bzzzchat.extensions.inflate
import com.ping.android.activity.R
import com.ping.android.model.LocalBackgroundItem
import com.ping.android.presentation.view.adapter.ViewType
import com.ping.android.presentation.view.adapter.ViewTypeDelegateAdapter
import com.ping.android.utils.GlideApp
import kotlinx.android.synthetic.main.item_gallery_image.view.*;

class LocalBackgroundDelegateAdapter: ViewTypeDelegateAdapter {
    override fun createViewHolder(parent: ViewGroup): RecyclerView.ViewHolder = ViewHolder(parent)

    override fun bindViewHolder(holder: RecyclerView.ViewHolder, item: ViewType) {
        (holder as ViewHolder).bindData(item as LocalBackgroundItem)
    }

    class ViewHolder(parent: ViewGroup): RecyclerView.ViewHolder(
            parent.inflate(R.layout.item_gallery_image)
    ) {
        fun bindData(item: LocalBackgroundItem) {
            GlideApp.with(itemView.context)
                    .load(item.resId)
                    .into(itemView.image)
        }
    }
}