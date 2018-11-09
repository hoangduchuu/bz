package com.ping.android.presentation.view.flexibleitem.messages

import android.graphics.Outline
import android.text.TextUtils
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bzzzchat.configuration.GlideApp
import com.ping.android.R
import com.ping.android.model.Message
import com.ping.android.utils.Log
import com.ping.android.utils.ResourceUtils

/**
 * Created by tuanluong on 3/2/18.
 */

abstract class GifMessageBaseItem(message: Message) : MessageBaseItem<GifMessageBaseItem.ViewHolder>(message) {

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return ViewHolder(view)
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



        private fun setImageMessage(message: Message) {
            val circularProgressDrawable = CircularProgressDrawable(itemView.context)
            circularProgressDrawable.strokeWidth = 10f
            circularProgressDrawable.centerRadius = 50f
            circularProgressDrawable.setColorSchemeColors(fetchAccentColor())
            circularProgressDrawable.start()
            loadingView.visibility = View.GONE
            GlideApp.with(itemView.context)
                    .asGif()
                    .load(message.mediaUrl)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .placeholder(circularProgressDrawable)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .override(400, 400)
                    .error(R.drawable.ic_error_outline)
                    .fitCenter()
                    .into(imageView!!)
        }

        private fun fetchAccentColor(): Int {
            val typedValue = TypedValue()

            val a = itemView.context.obtainStyledAttributes(typedValue.data, intArrayOf(R.attr.colorAccent))
            val color = a.getColor(0, 0)

            a.recycle()

            return color
        }
    }
}
