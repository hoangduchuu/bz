package com.ping.android.presentation.view.flexibleitem.messages

import android.graphics.Outline
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.FrameLayout
import android.widget.ImageView
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.ImageViewTarget
import com.bumptech.glide.request.target.Target
import com.bzzzchat.configuration.GlideApp
import com.bzzzchat.configuration.GlideRequest
import com.bzzzchat.configuration.GlideRequests
import com.google.firebase.storage.FirebaseStorage
import com.ping.android.R
import com.ping.android.model.Message
import com.ping.android.model.enums.MessageType
import com.ping.android.utils.ResourceUtils
import com.ping.android.utils.configs.Constant
import java.io.File

/**
 * Created by tuanluong on 3/2/18.
 */

abstract class StickerMessageBaseItem(message: Message) : MessageBaseItem<StickerMessageBaseItem.ViewHolder>(message) {

    override fun onCreateViewHolder(parent: ViewGroup): StickerMessageBaseItem.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return StickerMessageBaseItem.ViewHolder(view)
    }

    class ViewHolder(itemView: View?) : MessageBaseItem.ViewHolder(itemView) {
        private val content: FrameLayout
        private val imageView: ImageView?
        private var isUpdated: Boolean = false
        private val loadingView: View

        init {
            content = itemView!!.findViewById(R.id.content)
            imageView = itemView.findViewById(R.id.sticker)
            loadingView = itemView.findViewById(R.id.loading_container)
            initGestureListener()
            val radius = ResourceUtils.dpToPx(20)
            imageView!!.clipToOutline = true
            imageView.outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View, outline: Outline) {
                    outline.setRoundRect(0, 0, view.width, view.height, radius.toFloat())
                }
            }
        }

        override fun getClickableView(): View? {
            return imageView
        }

        override fun onDoubleTap() {
            if (item.isEditMode) {
                return
            }
        }

        override fun onSingleTap() {
            if (item.isEditMode) {
                return
            }
        }

        override fun bindData(item: MessageBaseItem<*>, lastItem: Boolean) {
            isUpdated = false
            if (this.item != null) {
                isUpdated = item.message.key == this.item.message.key
                if (isUpdated && !TextUtils.isEmpty(this.item.message.localFilePath)) {
                    item.message.localFilePath = this.item.message.localFilePath
                }
            }
            super.bindData(item, lastItem)
            setImageMessage(item.message)
        }

        override fun getSlideView(): View {
            return content
        }

        private fun loadGif(url: String) {
            var request = if (url.startsWith("gs://")) {
                val gsReference = FirebaseStorage.getInstance().getReferenceFromUrl(url)
                (this.glide as GlideRequests).asGif().load(gsReference)
            } else {
                (this.glide as GlideRequests).asGif().load(File(url))
            }
            request.into(object : ImageViewTarget<GifDrawable>(imageView) {
                        override fun setResource(resource: GifDrawable?) {
                            loadingView.visibility = View.GONE
                            imageView?.setImageDrawable(resource)
                        }
                    })
        }

        private fun setImageMessage(message: Message) {
            val bitmapMark = maskStatus
            if (imageView == null) return
            //Drawable placeholder = ContextCompat.getDrawable(imageView.getContext(), R.drawable.img_loading_image);
            if (!isUpdated) {
                loadingView.visibility = View.VISIBLE
            }
            var url = item.message.localFilePath
            if (!TextUtils.isEmpty(url)) {
                // should preload remote image
                if (!TextUtils.isEmpty(message.mediaUrl) && message.mediaUrl.startsWith("gs://")) {
                    val gsReference = FirebaseStorage.getInstance().getReferenceFromUrl(message.mediaUrl)
                    (this.glide as GlideRequests).load(gsReference)
                            .messageImage(message.key, bitmapMark)
                            .preload()
                }
                //loadingView.visibility = View.GONE
            }

            if (TextUtils.isEmpty(url)) {
                url = message.mediaUrl
            }
            if (TextUtils.isEmpty(url)) {
                imageView.setImageResource(0)
                return
            }
            if (isGif(url)) {
                loadGif(url)
            } else {
                var request: GlideRequest<Drawable>? = null
                request = if (url.startsWith("gs://")) {
                    val gsReference = FirebaseStorage.getInstance().getReferenceFromUrl(url)
                    (this.glide as GlideRequests)
                            .load(gsReference)
                } else {
                    (this.glide as GlideRequests)
                            .load(File(url))
                }

                request
                        .messageImage(message.key, bitmapMark)
                        .dontAnimate()
                        .listener(object : RequestListener<Drawable> {
                            override fun onLoadFailed(e: GlideException?, model: Any, target: Target<Drawable>, isFirstResource: Boolean): Boolean {
                                loadingView.visibility = View.GONE
                                return false
                            }

                            override fun onResourceReady(resource: Drawable, model: Any, target: Target<Drawable>, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                                loadingView.visibility = View.GONE
                                return false
                            }
                        })
                        .into(imageView)
            }
        }

        private fun isGif(url: String): Boolean {
            url.split("/").last {
                val extension = it.split(".").last()
                return extension == "gif"
            }
            return false
        }
    }
}
