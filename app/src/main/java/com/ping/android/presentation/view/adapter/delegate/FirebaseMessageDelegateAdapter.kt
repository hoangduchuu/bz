package com.ping.android.presentation.view.adapter.delegate

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.bzzzchat.extensions.inflate
import com.ping.android.R
import com.ping.android.model.ImageMessage
import com.ping.android.presentation.view.adapter.ViewType
import com.ping.android.presentation.view.adapter.ViewTypeDelegateAdapter

class FirebaseMessageDelegateAdapter(var clickListener: (ImageMessage) -> Unit): ViewTypeDelegateAdapter {
    override fun createViewHolder(parent: ViewGroup): RecyclerView.ViewHolder = ViewHolder(parent, clickListener)

    override fun bindViewHolder(holder: RecyclerView.ViewHolder, item: ViewType) {
        (holder as ViewHolder).bindData(item as ImageMessage)
    }

    class ViewHolder(parent: ViewGroup, var clickListener: (ImageMessage) -> Unit): RecyclerView.ViewHolder(
            parent.inflate(R.layout.item_gallery_image)
    ) {
        private lateinit var item: ImageMessage
        init {
            itemView.setOnClickListener { clickListener(item) }
        }

        fun bindData(item: ImageMessage) {
            this.item = item
//            if (TextUtils.isEmpty(item.imageUrl) || !item.imageUrl.startsWith("gs://")) {
//                return
//            }
//            val gsReference = FirebaseStorage.getInstance().getReferenceFromUrl(item.imageUrl)
//            GlideApp.with(itemView.context)
//                    .load(gsReference)
//                    .diskCacheStrategy(DiskCacheStrategy.ALL)
//                    .into(itemView.image)
        }
    }
}