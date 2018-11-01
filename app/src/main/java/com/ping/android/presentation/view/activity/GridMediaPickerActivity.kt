package com.ping.android.presentation.view.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import android.view.View
import android.widget.Toast
import com.bzzzchat.videorecorder.view.ImagesProvider
import com.ping.android.R
import com.ping.android.model.enums.Color
import com.ping.android.presentation.view.activity.ChatActivity.Companion.EXTRA_CONVERSATION_COLOR
import com.ping.android.presentation.view.adapter.MediaMultiSelectAdapter
import com.ping.android.presentation.view.adapter.MediaMultiSelectListener
import com.ping.android.presentation.view.custom.GridItemDecoration
import com.ping.android.utils.ThemeUtils
import kotlinx.android.synthetic.main.activity_grid_media_picker.*

class GridMediaPickerActivity : AppCompatActivity() {

    private lateinit var adapter: MediaMultiSelectAdapter
    private lateinit var imagesProvider: ImagesProvider
    private var maxItemCount = 5

    private val listMedia by lazy {
        list_media.layoutManager = androidx.recyclerview.widget.GridLayoutManager(this, 3)
        list_media.addItemDecoration(GridItemDecoration(3, R.dimen.grid_item_padding))
        list_media
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = intent.extras
        bundle.let {
            //originalConversation = bundle.getParcelable("CONVERSATION");
            var currentColor = Color.DEFAULT
            if (it.containsKey(EXTRA_CONVERSATION_COLOR)) {
                val color = it.getInt(EXTRA_CONVERSATION_COLOR)
                currentColor = Color.from(color)
                ThemeUtils.onActivityCreateSetTheme(this, currentColor)
            }
            maxItemCount = it.getInt(MAX_SELECTED_ITEM_COUNT)
        }
        setContentView(R.layout.activity_grid_media_picker)
        adapter = MediaMultiSelectAdapter(ArrayList(), object : MediaMultiSelectListener {
            override fun onCountChange(count: Int) {
                updateSelectedCount(count)
            }

            override fun onItemsExceeded() {
                Toast.makeText(this@GridMediaPickerActivity, "Maximum images is $maxItemCount", Toast.LENGTH_SHORT).show()
            }

        })
        adapter.updateMaxItemCount(maxItemCount)
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

    override fun onBackPressed() {
        val intent = Intent()
        intent.putExtra("data", false)
        setResult(Activity.RESULT_CANCELED, intent)
        finish()
    }

    companion object {
        const val MAX_SELECTED_ITEM_COUNT = "MAX_SELECTED_ITEM_COUNT"
    }
}
