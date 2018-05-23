package com.ping.android.presentation.view.adapter.delegate

import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.ViewGroup
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bzzzchat.extensions.inflate
import com.google.firebase.storage.FirebaseStorage
import com.ping.android.R
import com.ping.android.model.FirebaseImageItem
import com.ping.android.presentation.view.adapter.ViewType
import com.ping.android.presentation.view.adapter.ViewTypeDelegateAdapter
import com.bzzzchat.configuration.GlideApp
import kotlinx.android.synthetic.main.item_gallery_image.view.*;

class FirebaseBackgroundDelegateAdapter(var clickListener: (FirebaseImageItem) -> Unit): ViewTypeDelegateAdapter {
    override fun createViewHolder(parent: ViewGroup): RecyclerView.ViewHolder = ViewHolder(parent, clickListener)

    override fun bindViewHolder(holder: RecyclerView.ViewHolder, item: ViewType) {
        (holder as ViewHolder).bindData(item as FirebaseImageItem)
    }

    class ViewHolder(parent: ViewGroup, var clickListener: (FirebaseImageItem) -> Unit): RecyclerView.ViewHolder(
            parent.inflate(R.layout.item_gallery_image)
    ) {
        private lateinit var item: FirebaseImageItem
        init {
            itemView.card_view.setOnClickListener { clickListener(item) }
        }

        fun bindData(item: FirebaseImageItem) {
            this.item = item
            if (TextUtils.isEmpty(item.imageUrl) || !item.imageUrl.startsWith("gs://")) {
                return
            }
            val gsReference = FirebaseStorage.getInstance().getReferenceFromUrl(item.imageUrl)
            GlideApp.with(itemView.context)
                    .load(gsReference)
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .into(itemView.image)
        }
    }
}