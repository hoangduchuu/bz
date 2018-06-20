package com.bzzzchat.flexibleadapter

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup

/**
 * Base item that is used in [FlexibleAdapter1]
 *
 * Created by tuanluong on 10/18/17.
 */

interface FlexibleItem<V> where V: RecyclerView.ViewHolder{

    val layoutId: Int

    fun onCreateViewHolder(parent: ViewGroup): V

    fun onBindViewHolder(holder: V, lastItem: Boolean)
}

interface HeaderItem<T, V> where T: FlexibleItem<V>, V: RecyclerView.ViewHolder {
    val childItems: ArrayList<FlexibleItem<V>>
}