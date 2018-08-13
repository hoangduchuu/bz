package com.ping.android.presentation.view.adapter.delegate

import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import com.bzzzchat.configuration.GlideApp
import com.bzzzchat.extensions.inflate
import com.ping.android.R
import com.ping.android.model.LocalBackgroundItem
import com.ping.android.presentation.view.adapter.ViewType
import com.ping.android.presentation.view.adapter.ViewTypeDelegateAdapter
import kotlinx.android.synthetic.main.item_gallery_image.view.*

class LocalBackgroundDelegateAdapter: ViewTypeDelegateAdapter {
    override fun createViewHolder(parent: ViewGroup): androidx.recyclerview.widget.RecyclerView.ViewHolder = ViewHolder(parent)

    override fun bindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, item: ViewType) {
        (holder as ViewHolder).bindData(item as LocalBackgroundItem)
    }

    class ViewHolder(parent: ViewGroup): androidx.recyclerview.widget.RecyclerView.ViewHolder(
            parent.inflate(R.layout.item_gallery_image)
    ) {
        fun bindData(item: LocalBackgroundItem) {
            GlideApp.with(itemView.context)
                    .load(item.resId)
                    .into(itemView.image)
        }
    }
}