package com.ping.android.presentation.view.adapter

import android.support.v4.util.SparseArrayCompat
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.ping.android.presentation.view.adapter.delegate.LoadingDelegateAdapter

object AdapterConstants {
    const val LOADING = 1
    const val IMAGE = 2
    const val GALLERY = 3
    const val CAMERA = 4
    const val BLANK = 5
}

class FlexibleAdapterV2: RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val items: ArrayList<ViewType>

    private val delegateAdapters = SparseArrayCompat<ViewTypeDelegateAdapter>()

    init {
        delegateAdapters.put(AdapterConstants.LOADING, LoadingDelegateAdapter())
        items = ArrayList()
    }

    fun registerItemType(type: Int, viewTypeDelegate: ViewTypeDelegateAdapter) {
        if (delegateAdapters[type] != null) {
            throw RuntimeException("Register same view type for multiple delegate")
        }
        delegateAdapters.put(type, viewTypeDelegate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return delegateAdapters[viewType].createViewHolder(parent)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        delegateAdapters[getItemViewType(position)].bindViewHolder(holder, this.items[position])
    }

    override fun getItemViewType(position: Int): Int {
        return items[position].getViewType()
    }

    fun addItems(newItems: List<ViewType>) {
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}
