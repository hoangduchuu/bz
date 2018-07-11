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
import com.ping.android.model.enums.Color
import com.ping.android.presentation.view.activity.ChatActivity.EXTRA_CONVERSATION_COLOR
import com.ping.android.presentation.view.adapter.MediaMultiSelectAdapter
import com.ping.android.presentation.view.custom.GridItemDecoration
import com.ping.android.utils.ThemeUtils
import kotlinx.android.synthetic.main.activity_grid_media_picker.*

class GridMediaPickerActivity : AppCompatActivity() {

    private lateinit var adapter: MediaMultiSelectAdapter
    private lateinit var imagesProvider: ImagesProvider

    private val listMedia by lazy {
        list_media.layoutManager = GridLayoutManager(this, 3)
        list_media.addItemDecoration(GridItemDecoration(3, R.dimen.grid_item_padding))
        list_media
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = intent.extras
        if (bundle != null) {
            //originalConversation = bundle.getParcelable("CONVERSATION");
            var currentColor = Color.DEFAULT
            if (bundle.containsKey(EXTRA_CONVERSATION_COLOR)) {
                val color = bundle.getInt(EXTRA_CONVERSATION_COLOR)
                currentColor = Color.from(color)
                ThemeUtils.onActivityCreateSetTheme(this, currentColor)
            }
        }
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
