package com.ping.android.presentation.view.activity

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.SharedElementCallback
import android.support.v4.view.ViewPager
import android.view.View
import com.ping.android.R
import com.ping.android.dagger.loggedin.groupimage.GroupImageComponent
import com.ping.android.model.Message
import com.ping.android.model.enums.Color
import com.ping.android.presentation.view.adapter.ImagePagerAdapter
import com.ping.android.utils.ThemeUtils
import com.ping.android.utils.bus.BusProvider
import com.ping.android.utils.bus.events.GroupImagePositionEvent
import kotlinx.android.synthetic.main.activity_group_image_gallery.*
import javax.inject.Inject

class GroupImageGalleryActivity : CoreActivity() {
    lateinit var messages: MutableList<Message>

    lateinit var adapter: ImagePagerAdapter
    private var currentPosition: Int = 0

    @Inject
    lateinit var busProvider: BusProvider
    private val component: GroupImageComponent by lazy {
        loggedInComponent.provideGroupImageComponent()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        component.inject(this)
        postponeEnterTransition()
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

        prepareSharedElementTransition()
    }

    private fun prepareSharedElementTransition() {
        val callback = object : SharedElementCallback() {
            override fun onMapSharedElements(names: MutableList<String>?, sharedElements: MutableMap<String, View>?) {
                super.onMapSharedElements(names, sharedElements)
                // Locate the image view at the primary fragment (the ImageFragment
                // that is currently visible). To locate the fragment, call
                // instantiateItem with the selection position.
                // At this stage, the method will simply return the fragment at the
                // position and will not create a new one.
                val currentFragment = viewpager.adapter?.instantiateItem(viewpager, currentPosition) as? Fragment
                if (currentFragment?.view != null && names != null && sharedElements != null) {
                    sharedElements[names[0]] = currentFragment.view!!.findViewById(R.id.image_detail)
                }
            }
        }
        setEnterSharedElementCallback(callback)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        busProvider.post(GroupImagePositionEvent(currentPosition))
    }

    companion object {
        const val IMAGES_EXTRA = "IMAGES_EXTRA"
        const val POSITION_EXTRA = "POSITION_EXTRA"
    }
}
