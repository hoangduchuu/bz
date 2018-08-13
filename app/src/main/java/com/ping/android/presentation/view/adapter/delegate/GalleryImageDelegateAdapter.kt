package com.ping.android.presentation.view.adapter.delegate

import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import com.bzzzchat.extensions.inflate
import com.ping.android.R
import com.ping.android.presentation.view.adapter.ViewType
import com.ping.android.presentation.view.adapter.ViewTypeDelegateAdapter
import kotlinx.android.synthetic.main.item_media.view.*

class GalleryItemDelegateAdapter(var clickListener: () -> Unit) : ViewTypeDelegateAdapter {

    override fun createViewHolder(parent: ViewGroup): androidx.recyclerview.widget.RecyclerView.ViewHolder = ViewHolder(parent, clickListener)

    override fun bindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, item: ViewType) {

    }

    class ViewHolder(parent: ViewGroup, clickListener: () -> Unit) : androidx.recyclerview.widget.RecyclerView.ViewHolder(
            parent.inflate(R.layout.item_media)
    ) {

        init {
            itemView.image.setImageResource(R.drawable.ic_conversation_background)
            itemView.card_view.setOnClickListener { clickListener() }
        }
    }
}

class CameraItemDelegateAdapter(var clickListener: () -> Unit) : ViewTypeDelegateAdapter {

    override fun createViewHolder(parent: ViewGroup): androidx.recyclerview.widget.RecyclerView.ViewHolder = ViewHolder(parent, clickListener)

    override fun bindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, item: ViewType) {
    }

    class ViewHolder(parent: ViewGroup, clickListener: () -> Unit) : androidx.recyclerview.widget.RecyclerView.ViewHolder(
            parent.inflate(R.layout.item_media)
    ) {

        init {
            itemView.image.setImageResource(R.drawable.ic_camera)
            itemView.card_view.setOnClickListener { clickListener() }
        }
    }
}