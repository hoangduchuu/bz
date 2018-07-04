package com.ping.android.presentation.view.adapter

import android.support.v7.widget.RecyclerView
import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import com.bzzzchat.configuration.GlideApp
import com.bzzzchat.extensions.inflate
import com.bzzzchat.videorecorder.view.PhotoItem
import com.ping.android.R
import java.io.File

interface MediaClickListener {
    fun onSendPress(photoItem: PhotoItem)
}

class MediaAdapter(private var items: List<PhotoItem>, val clickListener: MediaClickListener) : RecyclerView.Adapter<MediaAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(parent, clickListener = {
        // Reset select state
        val item = items.find { it.isSelected }
        item?.apply {
            this.isSelected = false
            notifyItemChanged(items.indexOf(this))
        }
    }, sendHandler = {
        this.clickListener.onSendPress(it)
    })

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(items[position])
    }

    fun updateData(data: List<PhotoItem>) {
        this.items = data
        notifyDataSetChanged()
    }

    class ViewHolder(parent: ViewGroup, clickListener: (PhotoItem) -> Unit, sendHandler: (PhotoItem) -> Unit) : RecyclerView.ViewHolder(
            parent.inflate(R.layout.item_media_send, false)
    ) {
        private lateinit var item: PhotoItem
        private val image: ImageView = itemView.findViewById(R.id.image)
        private val btnSend: ImageView = itemView.findViewById(R.id.btnSend)
        private val overlayView: LinearLayout = itemView.findViewById(R.id.overlay)

        init {
            image.setOnClickListener {
                if (!item.isSelected) {
                    clickListener(item)
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
                clickListener(item)
                sendHandler(item)
            }
        }

        private fun loadImage() {
            // TODO: should use thumbnail here
            val imagePath = if (item.thumbnailPath.isEmpty()) item.imagePath else item.thumbnailPath
            GlideApp.with(itemView.context)
                    .load(File(item.imagePath))
                    .apply(RequestOptions.centerCropTransform().override(512))
                    .thumbnail(0.5f)
                    .into(image)
        }

        fun bindData(item: PhotoItem) {
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