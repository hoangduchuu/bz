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
import com.bumptech.glide.signature.ObjectKey
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

interface GroupImageAdapterListener {
    fun onSingleClick(position: Int, sharedElements: Pair<View, String>)
    fun onDoubleClick(position: Int, isMask: Boolean)
}

class GroupImageAdapter(var data: List<Message>, var listener: GroupImageAdapterListener?): RecyclerView.Adapter<GroupImageAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(parent)

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(data[position], position, data.size)
        holder.listener = listener
    }

    fun updateData(imageGroup: List<Message>) {
        this.data = imageGroup
        notifyDataSetChanged()
    }

    class ViewHolder(parent: ViewGroup, var listener: GroupImageAdapterListener? = null) : BaseMessageViewHolder (
            parent.inflate(R.layout.item_image_group)
    ) {
        val imageView: ImageView = itemView as ImageView
        val curveRadius = 20F
        private val imageDimension = 90.px
        private lateinit var message: Message

        init {
            imageView.clipToOutline = true
            initGestureListener()
        }

        override fun getClickableView(): View? = imageView

        override fun onSingleTap() {
            val position = adapterPosition
            val map = HashMap<String, View>()
            map[message.key] = imageView
            val pair: Pair<View, String> = Pair.create(imageView, message.key)
            listener?.onSingleClick(position, pair)
        }

        override fun onDoubleTap() {
            listener?.onDoubleClick(adapterPosition, !message.isMask)
        }

        override fun onLongPress() {

        }

        fun bindData(message: Message, position: Int, total: Int) {
            this.message = message
            var top = -curveRadius.toInt()
            var left = -curveRadius.toInt()
            var right = (imageDimension + curveRadius).toInt()
            var bottom = (imageDimension + curveRadius).toInt()
            if (total > 2) {
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
                    left = 0
                    top = 0
                    bottom = imageDimension
                } else {
                    // Right item
                    top = 0
                    right = imageDimension
                    bottom = imageDimension
                }
            }
            imageView.outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View?, outline: Outline?) {
                    Log.d("Left: $left, Top: $top, Right: $right, Bottom: $bottom")
                    outline?.setRoundRect(left, top, right, bottom, curveRadius)
                }
            }
            imageView.clipToOutline = true
            if (message.localFilePath != null && !message.localFilePath.isEmpty()) {
                UiUtils.loadImageFromFile(imageView, message.localFilePath, message.key, message.isMask)
                return
            }
            val url = if (message.thumbUrl != null && !message.thumbUrl.isEmpty()) message.thumbUrl else message.photoUrl
            if (url == null || !url.startsWith("gs://")) return
            val gsReference = FirebaseStorage.getInstance().getReferenceFromUrl(url)
            val key = ObjectKey(String.format("%s%s", message.key, if (message.isMask) "encoded" else "decoded"))
            GlideApp.with(itemView.context)
                    .asBitmap()
                    .load(gsReference)
                    .override(128)
                    .skipMemoryCache(false)
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .transform(BitmapEncode(message.isMask))
                    .signature(key)
                    .into(imageView)
        }
    }
}

abstract class GroupImageMessageBaseItem(message: Message): MessageBaseItem<GroupImageMessageBaseItem.ViewHolder>(message) {
    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder = ViewHolder(parent.inflate(layoutId))

    class ViewHolder(itemView: View): MessageBaseItem.ViewHolder(itemView), GroupImageAdapterListener {
        private val groupImage: RecyclerView = itemView.findViewById(R.id.group_images)
        private var groupImageAdapter: GroupImageAdapter = GroupImageAdapter(ArrayList(), this)
        private val gridLayoutManager = GridNonScrollableLayoutManager(itemView.context, 3)
        private val gridItemDecoration = GridItemDecoration(3, R.dimen.grid_item_padding_small, topSpace = 0)

        init {
            groupImage.clipToOutline = true
            groupImage.isNestedScrollingEnabled = false
            groupImage.layoutManager = gridLayoutManager
            groupImage.addItemDecoration(gridItemDecoration)
            groupImage.adapter = groupImageAdapter
        }

        fun setRecycledViewPool(viewPool: RecyclerView.RecycledViewPool) {
            groupImage.recycledViewPool = viewPool
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

        override fun onSingleClick(position: Int, sharedElements: Pair<View, String>) {
            if (item.isEditMode) return
            messageListener?.onGroupImageItemPress(this, item.message.childMessages, position, sharedElements)
        }

        override fun onDoubleClick(position: Int, isMask: Boolean) {
            if (item.isEditMode) return
            val childMessage = item.message.childMessages[position]
            // TODO should check to see whether all child messages are masked, then mask parent messages too

            messageListener?.updateChildMessageMask(childMessage, isMask)
        }
    }
}