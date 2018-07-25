package com.ping.android.presentation.view.adapter.delegate

import android.graphics.Bitmap
import android.support.v4.util.Pair
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.bumptech.glide.signature.ObjectKey
import com.bzzzchat.extensions.inflate
import com.google.firebase.storage.FirebaseStorage
import com.ping.android.R
import com.ping.android.model.ImageMessage
import com.ping.android.presentation.view.adapter.ViewType
import com.ping.android.presentation.view.adapter.ViewTypeDelegateAdapter
import com.ping.android.utils.configs.Constant
import com.ping.android.utils.BitmapEncode
import com.bzzzchat.configuration.GlideApp
import kotlinx.android.synthetic.main.item_gallery_image.view.*

class FirebaseMessageDelegateAdapter(var listener: FirebaseMessageListener) : ViewTypeDelegateAdapter {
    override fun createViewHolder(parent: ViewGroup): RecyclerView.ViewHolder = ViewHolder(parent, listener)

    override fun bindViewHolder(holder: RecyclerView.ViewHolder, item: ViewType) {
        (holder as ViewHolder).bindData(item as ImageMessage)
    }

    class ViewHolder(parent: ViewGroup, var listener: FirebaseMessageListener) : RecyclerView.ViewHolder(
            parent.inflate(R.layout.item_gallery_image)
    ) {
        private lateinit var item: ImageMessage

        init {
            itemView.card_view.setOnClickListener {
                itemView.image.transitionName = item.message.key
                val map = HashMap<String, View>()
                map[item.message.key] = itemView.image
                val pair: Pair<View, String> = Pair.create(itemView.image, item.message.key)
                listener.onClick(it, adapterPosition, pair)
            }
        }

        fun bindData(item: ImageMessage) {
            this.item = item
            itemView.sender.visibility = View.VISIBLE
            itemView.sender.text = item.message.senderName
            val url: String = item.message.mediaUrl
            itemView.image.transitionName = item.message.key
            if (TextUtils.isEmpty(url) || !url.startsWith("gs://")) {
                return
            }
            val gsReference = FirebaseStorage.getInstance().getReferenceFromUrl(url)
            val listener = object : RequestListener<Bitmap> {
                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap>?, isFirstResource: Boolean): Boolean {
                    listener.onLoaded(adapterPosition)
                    return false
                }

                override fun onResourceReady(resource: Bitmap?, model: Any?, target: Target<Bitmap>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                    listener.onLoaded(adapterPosition)
                    return false
                }
            }
            val target = object : SimpleTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    itemView.image.setImageBitmap(resource)
                }

            }
            GlideApp.with(itemView.context)
                    .asBitmap()
                    .load(gsReference)
                    .override(100)
                    .skipMemoryCache(false)
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .transform(BitmapEncode(item.message.isMask))
                    .signature(ObjectKey(String.format("%s%s", item.message.key, if (item.message.isMask) "encoded" else "decoded")))
                    .listener(listener)
                    .into(target)
        }
    }

    interface FirebaseMessageListener {
        fun onClick(view: View, position: Int, pair: Pair<View, String>)
        fun onLoaded(position: Int)
    }
}