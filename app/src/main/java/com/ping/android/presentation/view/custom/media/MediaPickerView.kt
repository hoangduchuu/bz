package com.ping.android.presentation.view.custom.media

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import com.bzzzchat.videorecorder.view.ImagesProvider
import com.ping.android.presentation.view.adapter.MediaAdapter

class MediaPickerView @JvmOverloads constructor(
        context: Context,
        attributeSet: AttributeSet?,
        defStyleAttr: Int = 0) : RecyclerView(context, attributeSet, defStyleAttr) {
    private var myAdapter: MediaAdapter? = null

    init {
        layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        initData()
    }

    private fun initData() {
        val data = ImagesProvider.getImages(context)
        myAdapter = MediaAdapter(data)
        adapter = myAdapter
    }

}