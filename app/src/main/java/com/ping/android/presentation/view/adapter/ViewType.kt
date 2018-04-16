package com.ping.android.presentation.view.adapter

import android.support.v7.widget.RecyclerView
import android.view.ViewGroup

interface ViewType {
    fun getViewType(): Int
}

interface ViewTypeDelegateAdapter {
    fun createViewHolder(parent: ViewGroup): RecyclerView.ViewHolder

    fun bindViewHolder(holder: RecyclerView.ViewHolder, item: ViewType)
}