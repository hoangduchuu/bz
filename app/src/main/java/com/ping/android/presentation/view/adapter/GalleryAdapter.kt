package com.ping.android.presentation.view.adapter

import android.support.v4.util.SparseArrayCompat
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.ping.android.presentation.view.adapter.delegate.GalleryImageDelegateAdapter
import com.ping.android.presentation.view.adapter.delegate.LoadingDelegateAdapter

object AdapterConstants {
    const val LOADING = 1
    const val IMAGE = 2
}

class GalleryAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val items: ArrayList<ViewType>

    private val delegateAdapters = SparseArrayCompat<ViewTypeDelegateAdapter>()

    init {
        delegateAdapters.put(AdapterConstants.LOADING, LoadingDelegateAdapter())
        delegateAdapters.put(AdapterConstants.IMAGE, GalleryImageDelegateAdapter())
        items = ArrayList()
    }

    fun registerItemType(type: Int, viewTypeDelegate: ViewTypeDelegateAdapter) {
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
}
