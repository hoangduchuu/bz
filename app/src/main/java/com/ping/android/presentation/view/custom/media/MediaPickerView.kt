package com.ping.android.presentation.view.custom.media

import android.app.Activity
import android.content.Context
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.util.AttributeSet
import android.widget.ImageView
import com.bzzzchat.extensions.inflate
import com.bzzzchat.videorecorder.view.ImagesProvider
import com.bzzzchat.videorecorder.view.PhotoItem
import com.ping.android.R
import com.ping.android.presentation.view.adapter.MediaAdapter
import com.ping.android.presentation.view.adapter.MediaClickListener

interface MediaPickerListener {
    fun openGridMediaPicker()
    fun sendImage(item: PhotoItem)
}

class MediaPickerView @JvmOverloads constructor(
        context: Context,
        attributeSet: AttributeSet? = null,
        defStyleAttr: Int = 0) : ConstraintLayout(context, attributeSet, defStyleAttr), MediaClickListener {

    private var myAdapter = MediaAdapter(ArrayList(), this)
    private lateinit var imagesProvider: ImagesProvider
    var listener: MediaPickerListener? = null

    init {
        inflate(R.layout.view_media_picker, true)
        val recyclerView: androidx.recyclerview.widget.RecyclerView = findViewById(R.id.list_photos)
        recyclerView.adapter = myAdapter
        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context, androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false)
        val button: ImageView = findViewById(R.id.btn_grid)
        button.setOnClickListener {
            listener?.openGridMediaPicker()
        }
    }

    fun initProvider(activity: Activity) {
        imagesProvider = ImagesProvider(activity)
        getData()
    }

    private fun getData() {
        imagesProvider.getPhotoDirs {
            myAdapter.updateData(it)
        }
    }

    override fun onSendPress(photoItem: PhotoItem) {
        listener?.sendImage(photoItem)
    }

    fun refreshData() {
        getData()
    }
}