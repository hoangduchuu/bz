package com.ping.android.presentation.view.flexibleitem.messages

import android.app.Activity
import android.graphics.Outline
import android.os.Build
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bzzzchat.configuration.GlideApp
import com.ping.android.R
import com.ping.android.model.Message
import com.ping.android.utils.BzLog
import com.ping.android.utils.ResourceUtils

/**
 * Created by tuanluong on 3/2/18.
 */
class SystemMessageItem(message: Message) : MessageBaseItem<SystemMessageItem.ViewHolder>(message) {
    override val layoutId: Int get() = R.layout.item_message_system

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return ViewHolder(view)
    }

    class ViewHolder(itemView: View?) : MessageBaseItem.ViewHolder(itemView) {
        private val content: FrameLayout
        private var isUpdated: Boolean = false
        private val loadingView: View
        private var width: Int? =null
        private var revealable_view : TextView?
        private var tvSystemMessage: TextView?
        private var itemChatStatus: TextView?

        init {
            content = itemView!!.findViewById(R.id.content)
            loadingView = itemView.findViewById(R.id.loading_container)
            revealable_view = itemView.findViewById(R.id.revealable_view);
            tvSystemMessage = itemView.findViewById(R.id.tvSystemMessage);
            itemChatStatus = itemView.findViewById(R.id.item_chat_status)
            loadingView.visibility = View.GONE
            revealable_view?.visibility = View.GONE
            itemChatStatus?.visibility = View.GONE
            tvMessageInfo.visibility = View.GONE
            initGestureListener()
            val radius = ResourceUtils.dpToPx(20)


            width =  getFullWidth()
        }

        /**
         * get Width of device
         */
        private fun getFullWidth(): Int {
            val displayMetrics = DisplayMetrics()
            ( itemView.context as Activity).windowManager.defaultDisplay.getMetrics(displayMetrics)
            val height = displayMetrics.heightPixels
            return displayMetrics.widthPixels
        }
        override fun getClickableView(): View? {
            return tvSystemMessage
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

        @RequiresApi(Build.VERSION_CODES.M)
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



        @RequiresApi(Build.VERSION_CODES.M)
        private fun setImageMessage(message: Message) {
            tvSystemMessage?.setText(message.message)
            BzLog.d("HHH: ${message.message}")
        }


    }
}
