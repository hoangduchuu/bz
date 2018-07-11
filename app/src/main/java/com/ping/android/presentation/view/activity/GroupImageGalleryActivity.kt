package com.ping.android.presentation.view.activity

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.view.ViewPager
import com.ping.android.R
import com.ping.android.model.Message
import com.ping.android.model.enums.Color
import com.ping.android.presentation.view.adapter.ImagePagerAdapter
import com.ping.android.utils.ThemeUtils
import kotlinx.android.synthetic.main.activity_group_image_gallery.*

class GroupImageGalleryActivity : AppCompatActivity() {
    lateinit var messages: MutableList<Message>

    lateinit var adapter: ImagePagerAdapter
    private var currentPosition: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent.extras.let {
            if (it.containsKey(ChatActivity.EXTRA_CONVERSATION_COLOR)) {
                val color = Color.from(it.getInt(ChatActivity.EXTRA_CONVERSATION_COLOR))
                ThemeUtils.onActivityCreateSetTheme(this, color)
            }
            messages = it.getParcelableArrayList(IMAGES_EXTRA)
            currentPosition = it.getInt(POSITION_EXTRA)
        }

        setContentView(R.layout.activity_group_image_gallery)
        btn_back.setOnClickListener { onBackPressed() }

        adapter = ImagePagerAdapter(supportFragmentManager, messages)
        viewpager.adapter = adapter
        viewpager.currentItem = currentPosition
        val listener = object : ViewPager.OnPageChangeListener {
            override fun onPageSelected(position: Int) {
                currentPosition = position
                val message = messages[position]

            }

            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }
        }
        viewpager.addOnPageChangeListener(listener)
    }

    companion object {
        const val IMAGES_EXTRA = "IMAGES_EXTRA"
        const val POSITION_EXTRA = "POSITION_EXTRA"
    }
}
