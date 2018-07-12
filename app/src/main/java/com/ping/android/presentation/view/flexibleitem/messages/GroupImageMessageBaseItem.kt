package com.ping.android.presentation.view.flexibleitem.messages

import android.graphics.Bitmap
import android.graphics.Outline
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.ShapeDrawable
import android.support.v4.util.Pair
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.ImageView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.bzzzchat.configuration.GlideApp
import com.bzzzchat.extensions.inflate
import com.bzzzchat.extensions.px
import com.google.firebase.storage.FirebaseStorage
import com.ping.android.R
import com.ping.android.model.Message
import com.ping.android.presentation.view.custom.GridItemDecoration
import com.ping.android.presentation.view.custom.GridNonScrollableLayoutManager
import com.ping.android.utils.BitmapEncode
import com.ping.android.utils.CommonMethod
import com.ping.android.utils.Log
import com.ping.android.utils.UiUtils

class GroupImageAdapter(var data: List<Message>, var listener: ((Int, Pair<View, String>) -> Unit)?): RecyclerView.Adapter<GroupImageAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(parent)

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(data[position], position, data.size)
        holder.itemView.setOnClickListener {
            val map = HashMap<String, View>()
            map[data[position].key] = holder.imageView
            val pair: Pair<View, String> = Pair.create(holder.imageView, data[position].key)
            listener?.let { it1 -> it1(position, pair) }
        }
    }

    fun updateData(imageGroup: List<Message>) {
        this.data = imageGroup
        notifyDataSetChanged()
    }

    class ViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
            parent.inflate(R.layout.item_image_group)
    ) {
        val imageView: ImageView = itemView as ImageView
        val curveRadius = 20F
        val imageDimension = 90.px

        init {
            imageView.clipToOutline = true
        }

        fun bindData(message: Message, position: Int, total: Int) {
            var top = -curveRadius.toInt()
            var left = -curveRadius.toInt()
            var right = (imageDimension + curveRadius).toInt()
            var bottom = (imageDimension + curveRadius).toInt()
            if (total >= 2) {
                val isLeftItem = (position + 1) % 3 == 1
                val isRightItem = (position + 1) % 3 == 0
                val isFirstRow = position < 3
                val isLastRow = (total - 1) / 3 == position / 3
                // There is more than 3 items
                if (isFirstRow && isLastRow) {
                    // Just 1 row
                    if (isLeftItem) {
                        left = 0
                        top = 0
                        bottom = imageDimension
                    } else if (isRightItem) {
                        // Right item
                        top = 0
                        right = imageDimension
                        bottom = imageDimension
                    }
                } else if (isFirstRow) {
                    if (isLeftItem) {
                        left = 0
                        top = 0
                    } else if (isRightItem) {
                        top = 0
                        right = imageDimension
                    }
                } else if (isLastRow) {
                    if (isLeftItem) {
                        left = 0
                        bottom = imageDimension
                    } else if (isRightItem) {
                        bottom = imageDimension
                        right = imageDimension
                    }
                }
            } else {
                // Just 2 items
                if (position == 0) {
                    // Left item
                    right = (imageView.width + curveRadius).toInt()
                } else {
                    // Right item
                    left = curveRadius.toInt()
                }
            }
            imageView.outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View?, outline: Outline?) {
                    Log.d("Left: $left, Top: $top, Right: $right, Bottom: $bottom")
                    outline?.setRoundRect(left, top, right, bottom, curveRadius)
                }
            }
            imageView.clipToOutline = true
            val maskStatus = CommonMethod.getBooleanFrom(message.markStatuses, message.currentUserId)
            if (message.localFilePath != null && !message.localFilePath.isEmpty()) {
                UiUtils.loadImageFromFile(imageView, message.localFilePath, message.key, maskStatus)
                return
            }
            val url = if (message.thumbUrl != null && !message.thumbUrl.isEmpty()) message.thumbUrl else message.photoUrl
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

abstract class GroupImageMessageBaseItem(message: Message): MessageBaseItem<GroupImageMessageBaseItem.ViewHolder>(message) {
    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder = ViewHolder(parent.inflate(layoutId))

    class ViewHolder(itemView: View): MessageBaseItem.ViewHolder(itemView) {
        private val groupImage: RecyclerView = itemView.findViewById(R.id.group_images)
        private var groupImageAdapter: GroupImageAdapter = GroupImageAdapter(ArrayList()) { selectedPosition, pair ->
            messageListener?.onGroupImageItemPress(this, item.message.childMessages, selectedPosition, pair)
        }
        private val gridLayoutManager = GridNonScrollableLayoutManager(itemView.context, 3)
        private val gridItemDecoration = GridItemDecoration(3, R.dimen.grid_item_padding_small, topSpace = 0)

        init {
            groupImage.clipToOutline = true
            groupImage.isNestedScrollingEnabled = false
            groupImage.layoutManager = gridLayoutManager
            groupImage.addItemDecoration(gridItemDecoration)
            groupImage.adapter = groupImageAdapter
        }

        override fun getClickableView(): View? = null

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

        fun getShareElementForPosition(position: Int): View? {
            val selectedViewHolder = groupImage.findViewHolderForAdapterPosition(position)
            if (selectedViewHolder?.itemView == null) {
                return null
            }
            return selectedViewHolder.itemView
        }
    }
}