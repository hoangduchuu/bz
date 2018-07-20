package com.ping.android.presentation.view.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.app.Fragment
import android.support.v4.app.SharedElementCallback
import android.support.v4.content.ContextCompat
import android.support.v4.util.Pair
import android.support.v4.view.ViewPager
import android.view.View
import android.view.WindowManager
import com.bzzzchat.extensions.toggleVisibility
import com.bzzzchat.videorecorder.view.withDelay
import com.ping.android.R
import com.ping.android.dagger.loggedin.groupimage.GroupImageComponent
import com.ping.android.dagger.loggedin.groupimage.GroupImageModule
import com.ping.android.model.Message
import com.ping.android.model.enums.Color
import com.ping.android.presentation.presenters.GroupImageGalleryPresenter
import com.ping.android.presentation.view.adapter.ImagePagerAdapter
import com.ping.android.utils.bus.BusProvider
import com.ping.android.utils.bus.events.GroupImagePositionEvent
import com.ping.android.utils.bus.events.ImagePullEvent
import com.ping.android.utils.bus.events.ImageTapEvent
import kotlinx.android.synthetic.main.activity_group_image_gallery.*
import java.util.*
import javax.inject.Inject

/**
 * @author tuanluong
 */

class GroupImageGalleryActivity : CoreActivity(), GroupImageGalleryPresenter.View {
    lateinit var messages: MutableList<Message>
    private var conversationId: String = ""
    private var currentPosition: Int = 0
    private var isGroupImage = false

    lateinit var adapter: ImagePagerAdapter
    @Inject
    lateinit var busProvider: BusProvider
    @Inject
    lateinit var presenter: GroupImageGalleryPresenter
    private val component: GroupImageComponent by lazy {
        loggedInComponent.provideGroupImageComponent(GroupImageModule(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        component.inject(this)
        postponeEnterTransition()
        intent.extras.let {
            conversationId = it.getString(CONVERSATION_ID)
            messages = it.getParcelableArrayList(IMAGES_EXTRA)
            currentPosition = it.getInt(POSITION_EXTRA)
            isGroupImage = it.getBoolean(IS_GROUP_IMAGE_EXTRA)
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)

        setContentView(R.layout.activity_group_image_gallery)

        btn_back.setOnClickListener { onBackPressed() }
        togglePuzzle.setOnClickListener {
            presenter.togglePuzzle(currentPosition)
        }

        val listener = object : ViewPager.OnPageChangeListener {
            override fun onPageSelected(position: Int) {
                currentPosition = position
                val message = messages[position]
                onMessageSelected(message)
            }

            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }
        }
        viewpager.addOnPageChangeListener(listener)
        adapter = ImagePagerAdapter(supportFragmentManager, messages)
        viewpager.adapter = adapter
        viewpager.currentItem = currentPosition
        onMessageSelected(messages[currentPosition])

        registerEvent(busProvider.events.subscribe {
            if (it is ImageTapEvent) {
                btn_back.toggleVisibility()
                togglePuzzle.toggleVisibility()
            }
            if (it is ImagePullEvent) {
                if (it.isStart) {
                    // Hide buttons
                    btn_back.alpha = 0.0f
                    togglePuzzle.alpha = 0.0f
                    showSystemUI()
                } else {
                    // Show buttons
                    btn_back.alpha = 1.0f
                    togglePuzzle.alpha = 1.0f
                    hideSystemUI()
                }
            }
        })

        prepareSharedElementTransition()
        presenter.init(conversationId, messages, isGroupImage)

        withDelay(1000) {
//            hideSystemUI()
        }
    }

    override fun showImageMessages(messages: List<Message>) {
        adapter.updateMessages(messages)
    }

    override fun updateMessage(message: Message, position: Int) {
        adapter.updateMessage(message, position)
    }

    override fun hideLoading() {
        super<CoreActivity>.hideLoading()
    }

    override fun showLoading() {
        super<CoreActivity>.showLoading()
    }

    private fun onMessageSelected(message: Message) {
        togglePuzzle.isChecked = message.isMask
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
        showSystemUI()
        //busProvider.post(GroupImagePositionEvent(currentPosition))
        finishAfterTransition()
    }

    override fun finishAfterTransition() {
        busProvider.post(GroupImagePositionEvent(currentPosition))
        super.finishAfterTransition()
    }

    override fun supportFinishAfterTransition() {
        busProvider.post(GroupImagePositionEvent(currentPosition))
        super.supportFinishAfterTransition()
    }

    private fun hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        val decorView = window.decorView
        decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                // Set the content to appear under the system bars so that the
                // content doesn't resize when the system bars hide and show.
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                // Hide the nav bar and status bar
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    // Shows the system bars by removing all the flags
    // except for the ones that make the content appear under the system bars.
    private fun showSystemUI() {
        val decorView = window.decorView
        decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
    }

    companion object {
        const val CONVERSATION_ID = "CONVERSATION_ID"
        const val IMAGES_EXTRA = "IMAGES_EXTRA"
        const val POSITION_EXTRA = "POSITION_EXTRA"
        const val IS_GROUP_IMAGE_EXTRA = "IS_GROUP_IMAGE_EXTRA"

        @JvmStatic
        fun start(activity: Activity, conversationId: String, messages: List<Message>, position: Int, isGroupImage: Boolean, sharedElements: Pair<View, String>) {
            val intent = Intent(activity, GroupImageGalleryActivity::class.java)
            intent.putExtra(ChatActivity.EXTRA_CONVERSATION_COLOR, Color.COLOR_1)
            intent.putParcelableArrayListExtra(GroupImageGalleryActivity.IMAGES_EXTRA, ArrayList(messages))
            intent.putExtra(GroupImageGalleryActivity.POSITION_EXTRA, position)
            intent.putExtra(GroupImageGalleryActivity.CONVERSATION_ID, conversationId)
            intent.putExtra(GroupImageGalleryActivity.IS_GROUP_IMAGE_EXTRA, isGroupImage)
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    activity, sharedElements
            )
            activity.startActivity(intent, options.toBundle())
        }
    }
}
