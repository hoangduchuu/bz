package com.ping.android.presentation.view.adapter.delegate

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.bzzzchat.extensions.inflate
import com.ping.android.activity.R
import com.ping.android.model.GalleryItem
import com.ping.android.presentation.view.adapter.ViewType
import com.ping.android.presentation.view.adapter.ViewTypeDelegateAdapter

class GalleryImageDelegateAdapter: ViewTypeDelegateAdapter {

    override fun createViewHolder(parent: ViewGroup): RecyclerView.ViewHolder = ViewHolder(parent)

    override fun bindViewHolder(holder: RecyclerView.ViewHolder, item: ViewType) {
        (holder as ViewHolder).bindData(item as GalleryItem)
    }

    class ViewHolder(parent: ViewGroup): RecyclerView.ViewHolder(
            parent.inflate(R.layout.item_gallery_image)
    ) {
        fun bindData(galleryItem: GalleryItem) {

        }
    }
}

