package com.ping.android.presentation.view.adapter

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import com.bumptech.glide.request.RequestOptions
import com.bzzzchat.configuration.GlideApp
import com.bzzzchat.extensions.inflate
import com.bzzzchat.videorecorder.view.PhotoItem
import com.ping.android.R
import java.io.File

class MediaMultiSelectAdapter(var data: List<PhotoItem>, val listener: (Int) -> Unit): RecyclerView.Adapter<MediaMultiSelectAdapter.ViewHolder>() {
    private val selectedItems: MutableList<PhotoItem> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(parent) {
        if (it.isSelected) {
            selectedItems.add(it)
        } else {
            selectedItems.remove(it)
        }
        listener(selectedItems.size)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(data[position])
    }

    fun updateData(data: List<PhotoItem>) {
        this.data = data
        notifyDataSetChanged()
    }

    fun getSelectedItems(): List<PhotoItem> = selectedItems

    class ViewHolder(parent: ViewGroup, val clickListener: (PhotoItem) -> Unit): RecyclerView.ViewHolder(
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

        private fun toggleCheck() {
            val isSelected = item.isSelected
            checkbox.isChecked = !isSelected
            checkbox.isSelected = !isSelected
            item.isSelected = !isSelected
            clickListener(item)
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