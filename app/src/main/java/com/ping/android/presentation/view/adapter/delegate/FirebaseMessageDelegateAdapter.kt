package com.ping.android.presentation.view.adapter.delegate

import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.ViewGroup
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.signature.ObjectKey
import com.bzzzchat.extensions.inflate
import com.google.firebase.storage.FirebaseStorage
import com.ping.android.R
import com.ping.android.model.ImageMessage
import com.ping.android.presentation.view.adapter.ViewType
import com.ping.android.presentation.view.adapter.ViewTypeDelegateAdapter
import com.ping.android.ultility.Constant
import com.ping.android.utils.BitmapEncode
import com.ping.android.utils.GlideApp
import kotlinx.android.synthetic.main.item_gallery_image.view.*

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
            var url: String = when (item.message.messageType) {
                Constant.MSG_TYPE_IMAGE -> item.message.photoUrl
                else -> item.message.gameUrl
            }
            if (TextUtils.isEmpty(url) || !url.startsWith("gs://")) {
                return
            }
            val gsReference = FirebaseStorage.getInstance().getReferenceFromUrl(url)
            GlideApp.with(itemView.context)
                    .load(gsReference)
                    .override(100)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .transform(BitmapEncode(item.message.isMask))
                    .signature(ObjectKey(String.format("%s%s", item.message.key, if (item.message.isMask) "encoded" else "decoded")))
                    .into(itemView.image)
        }
    }
}