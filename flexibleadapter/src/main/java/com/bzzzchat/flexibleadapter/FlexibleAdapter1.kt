package com.bzzzchat.flexibleadapter

import android.view.ViewGroup
import com.bzzzchat.flexibleadapter.baseitems.LoadingItem
import java.util.*

/**
 * Base adapter using [FlexibleItem] for generate dynamic view
 *
 * Created by tuanluong on 10/18/17.
 */

class FlexibleAdapter1<V, in T> : androidx.recyclerview.widget.RecyclerView.Adapter<V>() where V: androidx.recyclerview.widget.RecyclerView.ViewHolder, T: FlexibleItem<V> {
    private val items = ArrayList<T>()
    private val viewTypes = androidx.collection.SparseArrayCompat<T>()
    private val loadingItem: LoadingItem by lazy {
        LoadingItem()
    }

    // region setup
    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: V, position: Int) {
        val isLastPosition = position == itemCount - 1
        items[position].onBindViewHolder(holder, isLastPosition)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): V {
        return viewTypes.get(viewType)!!.onCreateViewHolder(parent)
    }

    override fun getItemViewType(position: Int): Int {
        val key = items[position].layoutId
        val item = viewTypes.get(key)
        if (item == null) {
            viewTypes.put(key, items[position])
        }
        return key
    }
    // endregion

    // region functions

    fun add(item: T) {
        this.hideLoading()
        val position = items.size
        this.items.add(item)
        this.notifyItemInserted(position)
    }

    fun addAll(items: List<T>) {
        this.hideLoading()
        var position = this.items.size
        this.items.addAll(items)
        notifyItemRangeChanged(position, items.size)
    }

    fun hideLoading() {
        if (this.items.isNotEmpty()) {
            if (this.items.last() is LoadingItem) {
                // remove if last item is LoadingItem
                val position = this.items.size - 1
                this.items.removeAt(position)
                notifyItemRemoved(position)
            }
        }
    }

    fun showLoading() {
        //add(loadingItem)
    }

    // endregion
}