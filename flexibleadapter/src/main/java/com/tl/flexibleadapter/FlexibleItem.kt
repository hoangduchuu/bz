package com.tl.flexibleadapter

import android.support.v7.widget.RecyclerView
import android.view.SurfaceHolder
import android.view.ViewGroup

/**
 * Base item that is used in [FlexibleAdapter]
 *
 * Created by tuanluong on 10/18/17.
 */

interface FlexibleItem<V> where V: RecyclerView.ViewHolder{

    val layoutId: Int

    fun onCreateViewHolder(parent: ViewGroup): V

    fun onBindViewHolder(holder: V)
}
