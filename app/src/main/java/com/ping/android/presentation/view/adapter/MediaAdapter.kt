package com.ping.android.presentation.view.adapter

import android.support.v7.widget.RecyclerView
import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import com.bumptech.glide.signature.ObjectKey
import com.bzzzchat.configuration.GlideApp
import com.bzzzchat.extensions.inflate
import com.bzzzchat.videorecorder.view.PhotoItem
import com.ping.android.R
import jp.wasabeef.glide.transformations.BlurTransformation
import java.io.File


class MediaAdapter(private var items: List<PhotoItem>) : RecyclerView.Adapter<MediaAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(parent)

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(items[position])
    }

    class ViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
            parent.inflate(R.layout.item_media_send, false)
    ) {
        private lateinit var item: PhotoItem
        private val image: ImageView = itemView.findViewById(R.id.image)
        private val btnSend: ImageView = itemView.findViewById(R.id.btnSend)
        private var isBlurred = false

        init {
            image.setOnClickListener {
                if (!isBlurred) {
                    blurImage()
                    val anim = AnimationUtils.loadAnimation(itemView.context, R.anim.zoomin)
                    image.startAnimation(anim)
                    TransitionManager.beginDelayedTransition(itemView as ViewGroup?)
                    btnSend.visibility = View.VISIBLE
                } else {
                    loadImage()
                    val anim = AnimationUtils.loadAnimation(itemView.context, R.anim.zoomout)
                    image.startAnimation(anim)
                    TransitionManager.beginDelayedTransition(itemView as ViewGroup?)
                    btnSend.visibility = View.GONE
                }
            }
            btnSend.setOnClickListener {

            }
        }

        private fun blurImage() {
            isBlurred = true
            val objectKey = ObjectKey("${item.thumbnailPath}_blur")
            GlideApp.with(itemView.context)
                    .load(File(item.thumbnailPath))
                    .transform(BlurTransformation())
                    .signature(objectKey)
                    .into(image)
        }

        private fun loadImage() {
            isBlurred = false
            val objectKey = ObjectKey(item.thumbnailPath)
            GlideApp.with(itemView.context)
                    .load(File(item.thumbnailPath))
                    .signature(objectKey)
                    .into(image)
        }

        fun bindData(item: PhotoItem) {
            this.item = item
            btnSend.visibility = View.GONE
            this.loadImage()
        }
    }
}