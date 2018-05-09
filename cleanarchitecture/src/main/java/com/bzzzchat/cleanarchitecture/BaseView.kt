package com.bzzzchat.cleanarchitecture

/**
 * Created by tuanluong on 10/18/17.
 */

interface BaseView {
    fun showLoading() {}
    fun hideLoading() {}
}

interface AdapterView<in T> {
    fun updateData(data: List<T>)
    fun notifyDataSetChange()
    fun notifyItemInserted(position: Int)
    fun notifyItemChange(position: Int)
    fun notifyItemMoved(fromPosition: Int, toPosition: Int)
}