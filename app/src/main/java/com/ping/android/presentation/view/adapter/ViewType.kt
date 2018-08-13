package com.ping.android.presentation.view.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup

interface ViewType {
    fun getViewType(): Int
}

interface ViewTypeDelegateAdapter {
    fun createViewHolder(parent: ViewGroup): androidx.recyclerview.widget.RecyclerView.ViewHolder

    fun bindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, item: ViewType)
}

abstract class SelectableViewType: ViewType {
    var isSelected: Boolean = false
    var isSingleSelect: Boolean = true
}

interface SelectableListener {
    fun deselectOthers()
}

abstract class SelectableViewTypeDelegateAdapter: ViewTypeDelegateAdapter {
    var listener: SelectableListener? = null

    override fun bindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, item: ViewType) {
        (holder as SelectableViewHolder).listener = listener
    }
}

abstract class SelectableViewHolder(itemView: ViewGroup): androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
    lateinit var item: SelectableViewType
    var listener: SelectableListener? = null

    init {
        itemView.setOnClickListener {
            if (item.isSelected) {
                // Deselect
                toggleSelect(false)
            } else {
                // Select this item
                toggleSelect(true)
                if (item.isSingleSelect) {
                    // Deselect other items
                    listener?.deselectOthers()
                }
            }
        }
    }

    abstract fun toggleSelect(isSelected: Boolean)
}

