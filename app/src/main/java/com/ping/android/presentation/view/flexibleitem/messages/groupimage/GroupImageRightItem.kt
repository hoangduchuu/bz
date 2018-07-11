package com.ping.android.presentation.view.flexibleitem.messages.groupimage

import android.graphics.Bitmap
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.bzzzchat.configuration.GlideApp
import com.bzzzchat.extensions.inflate
import com.google.firebase.storage.FirebaseStorage
import com.ping.android.R
import com.ping.android.model.Message
import com.ping.android.presentation.view.custom.GridItemDecoration
import com.ping.android.presentation.view.flexibleitem.messages.MessageBaseItem
import com.ping.android.utils.BitmapEncode
import com.ping.android.utils.Log

class GroupImageAdapter(var data: List<Message>, var listener: MessageBaseItem.MessageListener?): RecyclerView.Adapter<GroupImageAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(parent)

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(data[position])
        holder.itemView.setOnClickListener {
            listener?.onGroupImageItemPress(data, position)
        }
    }

    fun updateData(imageGroup: List<Message>) {
        this.data = imageGroup
        notifyDataSetChanged()
    }

    class ViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
            parent.inflate(R.layout.item_image_group)
    ) {
        private val imageView: ImageView = itemView as ImageView

        init {
            imageView.clipToOutline = true
        }

        fun bindData(message: Message) {
            val url = if (!message.thumbUrl.isEmpty()) message.thumbUrl else message.photoUrl
            Log.e(url)
            val gsReference = FirebaseStorage.getInstance().getReferenceFromUrl(url)
            val target = object : SimpleTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    imageView.setImageBitmap(resource)
                }
            }
            GlideApp.with(itemView.context)
                    .asBitmap()
                    .load(gsReference)
                    .override(128)
                    .skipMemoryCache(false)
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .transform(BitmapEncode(false))
                    .into(target)
        }
    }
}

class GroupImageRightItem(message: Message): MessageBaseItem<GroupImageRightItem.ViewHolder>(message) {
    override val layoutId: Int = R.layout.item_chat_right_img_group

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder = GroupImageRightItem.ViewHolder(parent.inflate(layoutId))

    override fun onBindViewHolder(holder: ViewHolder, lastItem: Boolean) {
        super.onBindViewHolder(holder, lastItem)
    }

    class ViewHolder(itemView: View): MessageBaseItem.ViewHolder(itemView) {
        private val groupImage: RecyclerView = itemView.findViewById(R.id.group_images)
        private var groupImageAdapter: GroupImageAdapter = GroupImageAdapter(ArrayList(), messageListener)
        private val gridLayoutManager = GridLayoutManager(itemView.context, 3)
        private val gridItemDecoration = GridItemDecoration(3, R.dimen.grid_item_padding_small, topSpace = 0)

        init {
            groupImage.clipToOutline = true
            groupImage.layoutManager = gridLayoutManager
            groupImage.addItemDecoration(gridItemDecoration)
            groupImage.adapter = groupImageAdapter
        }

        override fun getClickableView(): View? = null

        override fun setMessageListener(messageListener: MessageListener?) {
            super.setMessageListener(messageListener)
            groupImageAdapter.listener = messageListener
        }

        override fun getSlideView(): View = groupImage

        private fun updateSpanCount(spanCount: Int) {
            gridLayoutManager.spanCount = spanCount
            gridItemDecoration.spanCount = spanCount
        }

        override fun bindData(item: MessageBaseItem<*>?, lastItem: Boolean) {
            super.bindData(item, lastItem)
            item?.let {
                if (it.message.childMessages != null) {
                    val count = if (it.message.childMessages.size >= 3) 3 else it.message.childMessages.size
                    if (count < 1) return
                    updateSpanCount(count)
                    groupImageAdapter.updateData(it.message.childMessages)
                }
            }
        }
    }
}