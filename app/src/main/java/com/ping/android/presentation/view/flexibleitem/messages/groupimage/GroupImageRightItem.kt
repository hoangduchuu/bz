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
import com.bumptech.glide.signature.ObjectKey
import com.bzzzchat.configuration.GlideApp
import com.bzzzchat.extensions.inflate
import com.google.firebase.storage.FirebaseStorage
import com.ping.android.R
import com.ping.android.model.Message
import com.ping.android.presentation.view.custom.GridItemDecoration
import com.ping.android.presentation.view.flexibleitem.messages.MessageBaseItem
import com.ping.android.utils.BitmapEncode
import kotlinx.android.synthetic.main.item_gallery_image.view.*

class GroupImageAdapter(var data: List<String>): RecyclerView.Adapter<GroupImageAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(parent)

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(data[position])
    }

    fun updateData(imageGroup: List<String>) {
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

        fun bindData(s: String) {
            val gsReference = FirebaseStorage.getInstance().getReferenceFromUrl(s)
            val target = object : SimpleTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    imageView.setImageBitmap(resource)
                }

            }
            GlideApp.with(itemView.context)
                    .asBitmap()
                    .load(gsReference)
                    .override(100)
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
        private var groupImageAdapter: GroupImageAdapter = GroupImageAdapter(ArrayList())

        init {
            groupImage.clipToOutline = true
            groupImage.layoutManager = GridLayoutManager(itemView.context, 3)
            groupImage.adapter = groupImageAdapter
            groupImage.addItemDecoration(GridItemDecoration(3, R.dimen.grid_item_padding_small))
        }

        override fun getClickableView(): View? = null

        override fun bindData(item: MessageBaseItem<*>?, lastItem: Boolean) {
            super.bindData(item, lastItem)
            item?.let {
                groupImageAdapter.updateData(it.message.imageGroup)
            }
        }
    }
}