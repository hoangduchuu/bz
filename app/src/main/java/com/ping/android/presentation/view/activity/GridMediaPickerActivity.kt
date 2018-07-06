package com.ping.android.presentation.view.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.view.View
import com.bzzzchat.videorecorder.view.ImagesProvider
import com.bzzzchat.videorecorder.view.PhotoItem
import com.ping.android.R
import com.ping.android.presentation.view.adapter.MediaMultiSelectAdapter
import kotlinx.android.synthetic.main.activity_grid_media_picker.*

class GridMediaPickerActivity : AppCompatActivity() {

    private lateinit var adapter: MediaMultiSelectAdapter
    private lateinit var imagesProvider: ImagesProvider

    private val listMedia by lazy {
        list_media.layoutManager = GridLayoutManager(this, 3)
        list_media
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_grid_media_picker)
        adapter = MediaMultiSelectAdapter(ArrayList()) {
            updateSelectedCount(it)
        }
        listMedia.adapter = adapter
        btn_back.setOnClickListener { onBackPressed() }

        imagesProvider = ImagesProvider(this)
        imagesProvider.getPhotoDirs {
            adapter.updateData(it)
        }
        btnSend.setOnClickListener {
            val selectedPhotos = ArrayList(adapter.getSelectedItems())
            if (selectedPhotos.isNotEmpty()) {
                val intent = Intent()
                intent.putParcelableArrayListExtra("data", selectedPhotos)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }
    }

    private fun updateSelectedCount(count: Int) {
        tvCount.text = "$count"
        if (count > 0) {
            tvCount.visibility = View.VISIBLE
        } else {
            tvCount.visibility = View.GONE
        }
    }
}
