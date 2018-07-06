package com.ping.android.presentation.view.adapter.delegate

import android.support.v7.widget.RecyclerView
import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import com.bumptech.glide.request.RequestOptions
import com.bzzzchat.configuration.GlideApp
import com.bzzzchat.extensions.inflate
import com.bzzzchat.videorecorder.view.PhotoItem
import com.ping.android.R
import com.ping.android.presentation.view.adapter.MediaClickListener
import com.ping.android.presentation.view.adapter.ViewType
import com.ping.android.presentation.view.adapter.ViewTypeDelegateAdapter
import java.io.File

data class MediaItem(var photoItem: PhotoItem, var isSelected: Boolean = false) : ViewType {

    override fun getViewType(): Int {
        return 1
    }
}

class MediaItemDelegateAdapter(clickListener: MediaClickListener): ViewTypeDelegateAdapter {
    override fun createViewHolder(parent: ViewGroup): RecyclerView.ViewHolder = ViewHolder(parent, {

    }, {})

    override fun bindViewHolder(holder: RecyclerView.ViewHolder, item: ViewType) {
        (holder as ViewHolder).bindData(item as MediaItem)
    }

    class ViewHolder(parent: ViewGroup, clickListener: (PhotoItem) -> Unit, sendHandler: (PhotoItem) -> Unit) : RecyclerView.ViewHolder(
            parent.inflate(R.layout.item_media_send, false)
    ) {
        private lateinit var item: MediaItem
        private val image: ImageView = itemView.findViewById(R.id.image)
        private val btnSend: ImageView = itemView.findViewById(R.id.btnSend)
        private val overlayView: LinearLayout = itemView.findViewById(R.id.overlay)

        init {
            image.setOnClickListener {
                if (!item.isSelected) {
                    clickListener(item.photoItem)
                    item.isSelected = true
                    val anim = AnimationUtils.loadAnimation(itemView.context, R.anim.zoomin)
                    image.startAnimation(anim)
                    TransitionManager.beginDelayedTransition(itemView as ViewGroup?)
                    overlayView.visibility = View.VISIBLE
                } else {
                    item.isSelected = false
                    val anim = AnimationUtils.loadAnimation(itemView.context, R.anim.zoomout)
                    image.startAnimation(anim)
                    TransitionManager.beginDelayedTransition(itemView as ViewGroup?)
                    overlayView.visibility = View.GONE
                }
            }
            btnSend.setOnClickListener {
                clickListener(item.photoItem)
                sendHandler(item.photoItem)
            }
        }

        private fun loadImage() {
            // TODO: should use thumbnail here
            val imagePath = if (item.photoItem.thumbnailPath.isEmpty()) item.photoItem.imagePath else item.photoItem.thumbnailPath
            GlideApp.with(itemView.context)
                    .load(File(item.photoItem.imagePath))
                    .apply(RequestOptions.centerCropTransform().override(512))
                    .thumbnail(0.5f)
                    .into(image)
        }

        fun bindData(item: MediaItem) {
            this.item = item
            if (overlayView.visibility == View.VISIBLE) {
                if (!item.isSelected) {
                    TransitionManager.beginDelayedTransition(itemView as ViewGroup?)
                    //overlayView.visibility = View.GONE
                }
            }
            overlayView.visibility = if (item.isSelected) View.VISIBLE else View.GONE
            this.loadImage()
        }
    }
}

class MediaItemSelectableDelegateAdapter: ViewTypeDelegateAdapter {
    override fun createViewHolder(parent: ViewGroup): RecyclerView.ViewHolder = ViewHolder(parent)

    override fun bindViewHolder(holder: RecyclerView.ViewHolder, item: ViewType) {
        (holder as ViewHolder).bindData(item as PhotoItem)
    }

    class ViewHolder(parent: ViewGroup): RecyclerView.ViewHolder(
            parent.inflate(R.layout.item_media_selectable, false)
    ) {
        fun bindData(item: PhotoItem) {

        }
    }
}