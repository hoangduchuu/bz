package com.ping.android.presentation.view.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import com.bumptech.glide.request.RequestOptions
import com.bzzzchat.configuration.GlideApp
import com.bzzzchat.extensions.inflate
import com.bzzzchat.videorecorder.view.PhotoItem
import com.ping.android.R
import java.io.File

interface MediaMultiSelectListener {
    fun onCountChange(count: Int)
    fun onItemsExceeded()
}

class MediaMultiSelectAdapter(var data: List<PhotoItem>, val listener: MediaMultiSelectListener): androidx.recyclerview.widget.RecyclerView.Adapter<MediaMultiSelectAdapter.ViewHolder>() {
    private val selectedItems: MutableList<PhotoItem> = ArrayList()
    private var maxItemCount = 5

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(parent)

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(data[position])
        holder.itemView.setOnClickListener {
            val size= selectedItems.size
            val selectedItem = data[position]
            if (size < maxItemCount || (size == maxItemCount && selectedItem.isSelected)) {
                holder.toggleCheck()
                if (data[position].isSelected) {
                    selectedItems.add(data[position])
                } else {
                    selectedItems.remove(data[position])
                }
                listener.onCountChange(selectedItems.size)
            } else {
                listener.onItemsExceeded()
            }
        }
    }

    fun updateData(data: List<PhotoItem>) {
        this.data = data
        notifyDataSetChanged()
    }

    fun updateMaxItemCount(maxItemCount: Int) {
        this.maxItemCount = maxItemCount
    }

    fun getSelectedItems(): List<PhotoItem> = selectedItems

    class ViewHolder(parent: ViewGroup): androidx.recyclerview.widget.RecyclerView.ViewHolder(
            parent.inflate(R.layout.item_media_selectable)
    ) {
        private val image: ImageView = itemView.findViewById(R.id.image)
        private val checkbox: CheckBox = itemView.findViewById(R.id.checkbox)

        private lateinit var item: PhotoItem

        init {
            itemView.setOnClickListener {
                toggleCheck()
            }
            checkbox.isClickable = false
        }

        fun toggleCheck() {
            val isSelected = item.isSelected
            checkbox.isChecked = !isSelected
            checkbox.isSelected = !isSelected
            item.isSelected = !isSelected
        }

        fun bindData(item: PhotoItem) {
            this.item = item
            checkbox.isChecked = item.isSelected
            checkbox.isSelected = item.isSelected
            loadImage()
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
    }
}