package com.ping.android.presentation.view.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.transition.TransitionManager
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.app.Fragment
import android.support.v4.app.SharedElementCallback
import android.support.v4.content.ContextCompat
import android.support.v4.util.Pair
import android.support.v4.view.ViewPager
import android.view.View
import android.view.WindowManager
import com.bzzzchat.extensions.toggleVisibility
import com.bzzzchat.videorecorder.view.showToast
import com.ping.android.R
import com.ping.android.dagger.loggedin.groupimage.GroupImageComponent
import com.ping.android.dagger.loggedin.groupimage.GroupImageModule
import com.ping.android.model.Message
import com.ping.android.model.enums.Color
import com.ping.android.presentation.presenters.GroupImageGalleryPresenter
import com.ping.android.presentation.view.adapter.ImagePagerAdapter
import com.ping.android.utils.PermissionsChecker
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
    private val background: ColorDrawable = ColorDrawable(android.graphics.Color.BLACK)

    lateinit var adapter: ImagePagerAdapter
    @Inject
    lateinit var busProvider: BusProvider
    @Inject
    lateinit var presenter: GroupImageGalleryPresenter
    private val component: GroupImageComponent by lazy {
        loggedInComponent.provideGroupImageComponent(GroupImageModule(this))
    }
    private lateinit var permissionChecker: PermissionsChecker

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

        permissionChecker = PermissionsChecker.from(this)

        setContentView(R.layout.activity_group_image_gallery)
        container.background = background
        btn_back.setOnClickListener { onBackPressed() }
        togglePuzzle.setOnClickListener {
            presenter.togglePuzzle(currentPosition)
        }
        btnDownload.setOnClickListener {
            permissionChecker.check(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .subscribe { success ->
                        if (success) {
                            presenter.downloadImage(messages[currentPosition])
                        }
                    }
        }

        val listener = object : ViewPager.OnPageChangeListener {
            override fun onPageSelected(position: Int) {
                currentPosition = position
                val message = messages[position]
                onMessageSelected(message)
                updateTitle(position)
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
        updateTitle(currentPosition)

        registerEvent(busProvider.events.subscribe {
            if (it is ImageTapEvent) {
                if (top_bar.visibility == View.GONE) {
                    showSystemUI()
                } else {
                    hideSystemUI()
                }
                TransitionManager.beginDelayedTransition(container)
                top_bar.toggleVisibility()
                togglePuzzle.toggleVisibility()
            }
            if (it is ImagePullEvent) {
                if (it.isStart) {
                    // Hide buttons
                    top_bar.animate().alpha(0.0f).start()
                    togglePuzzle.animate().alpha(0.0f).start()
                    hideSystemUI()
                } else {
                    updateBackgroundOpacity(it.progress)
                    // Show buttons
                    if (it.progress == 0.0f) {
                        TransitionManager.beginDelayedTransition(container)
                        top_bar.animate().alpha(1.0f).start()
                        togglePuzzle.animate().alpha(1.0f).start()
                        if (top_bar.visibility == View.VISIBLE) {
                            showSystemUI()
                        }
                    }
                }
            }
        })

        prepareSharedElementTransition()
        presenter.init(conversationId, messages, isGroupImage)
    }


    private fun updateBackgroundOpacity(progress: Float) {
        val finalProgress = Math.min(1f, progress * 3f)
        background.alpha = (0xff * (1f - finalProgress)).toInt()
    }

    override fun onResume() {
        super.onResume()
        hideSystemUI()
    }

    private fun updateTitle(position: Int) {
        val title = "${position + 1} of ${messages.size}"
        txtTitle.text = title
    }

    override fun showImageMessages(messages: List<Message>) {
        adapter.updateMessages(messages)
    }

    override fun updateMessage(message: Message, position: Int) {
        adapter.updateMessage(message, position)
    }

    override fun showMessageDownloadSuccessfully() {
        showToast("Your image saved to gallery")
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
                if (currentFragment?.view != null && names != null && sharedElements != null && sharedElements.isNotEmpty()) {
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
        fun start(activity: Activity, conversationId: String, messages: List<Message>, position: Int, isGroupImage: Boolean, sharedElements: Pair<View, String>? = null) {
            val intent = Intent(activity, GroupImageGalleryActivity::class.java)
            intent.putExtra(ChatActivity.EXTRA_CONVERSATION_COLOR, Color.COLOR_1)
            intent.putParcelableArrayListExtra(GroupImageGalleryActivity.IMAGES_EXTRA, ArrayList(messages))
            intent.putExtra(GroupImageGalleryActivity.POSITION_EXTRA, position)
            intent.putExtra(GroupImageGalleryActivity.CONVERSATION_ID, conversationId)
            intent.putExtra(GroupImageGalleryActivity.IS_GROUP_IMAGE_EXTRA, isGroupImage)
            val options  = if (sharedElements != null) {
                ActivityOptionsCompat.makeSceneTransitionAnimation(activity, sharedElements)
            } else {
                ActivityOptionsCompat.makeSceneTransitionAnimation(activity)
            }
            activity.startActivity(intent, options.toBundle())
        }
    }
}
