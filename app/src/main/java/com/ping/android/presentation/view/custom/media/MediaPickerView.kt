package com.ping.android.presentation.view.custom.media

import android.app.Activity
import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import com.bzzzchat.videorecorder.view.ImagesProvider
import com.bzzzchat.videorecorder.view.PhotoItem
import com.ping.android.presentation.view.adapter.MediaAdapter
import com.ping.android.presentation.view.adapter.MediaClickListener

class MediaPickerView @JvmOverloads constructor(
        context: Context,
        attributeSet: AttributeSet?,
        defStyleAttr: Int = 0) : RecyclerView(context, attributeSet, defStyleAttr), MediaClickListener {

    private lateinit var myAdapter: MediaAdapter
    private lateinit var imagesProvider: ImagesProvider

    var listener: ((PhotoItem) -> Unit)? = null

    init {
        layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
    }

    private fun initData() {
        myAdapter = MediaAdapter(ArrayList(), this)
        adapter = myAdapter
    }

    fun initProvider(activity: Activity) {
        imagesProvider = ImagesProvider(activity)
        initData()
        getData()
    }

    private fun getData() {
        imagesProvider.getPhotoDirs {
            myAdapter.updateData(it)
        }
    }

    override fun onSendPress(photoItem: PhotoItem) {
        listener?.invoke(photoItem)
    }

    fun refreshData() {
        getData()
    }
}