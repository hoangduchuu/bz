package com.ping.android.presentation.view.activity

import android.os.Bundle
import androidx.core.app.SharedElementCallback
import androidx.core.util.Pair
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import com.bzzzchat.cleanarchitecture.BasePresenter
import com.bzzzchat.configuration.GlideApp
import com.ping.android.R
import com.ping.android.model.Conversation
import com.ping.android.model.ImageMessage
import com.ping.android.model.Message
import com.ping.android.presentation.presenters.GalleryPresenter
import com.ping.android.presentation.view.adapter.AdapterConstants
import com.ping.android.presentation.view.adapter.FlexibleAdapterV2
import com.ping.android.presentation.view.adapter.delegate.FirebaseMessageDelegateAdapter
import com.ping.android.presentation.view.custom.GridItemDecoration
import com.ping.android.utils.ThemeUtils
import com.ping.android.utils.bus.BusProvider
import com.ping.android.utils.bus.events.GroupImagePositionEvent
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_gallery.*
import javax.inject.Inject

class GalleryActivity : CoreActivity(), GalleryPresenter.View, FirebaseMessageDelegateAdapter.FirebaseMessageListener {
    @Inject
    lateinit var presenter: GalleryPresenter
    @Inject
    lateinit var busProvider: BusProvider
    private lateinit var conversation: Conversation
    private lateinit var adapter: FlexibleAdapterV2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent.extras.let {
            conversation = intent.extras.getParcelable("conversation")
            ThemeUtils.onActivityCreateSetTheme(this, conversation.currentColor)
        }
        setContentView(R.layout.activity_gallery)
        AndroidInjection.inject(this)

        presenter.initConversation(conversation)
        presenter.loadMedia()

        registerEvent(busProvider.events
                .subscribe { o ->
                    if (o is GroupImagePositionEvent) {
                        presenter.currentPosition = o.position
                    }
                })

        btn_back.setOnClickListener { onBackPressed() }
        val glide = GlideApp.with(this)
        adapter = FlexibleAdapterV2()
        adapter.registerItemType(AdapterConstants.IMAGE, FirebaseMessageDelegateAdapter(glide, this))
        gallery_list.layoutManager = androidx.recyclerview.widget.GridLayoutManager(this, 3)
        gallery_list.addItemDecoration(GridItemDecoration(3, R.dimen.grid_item_padding))
        val listener = object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView?, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val totalItem = gallery_list.layoutManager.itemCount
                val lastVisibleItem = (gallery_list.layoutManager as androidx.recyclerview.widget.GridLayoutManager).findLastVisibleItemPosition()
                if (totalItem <= lastVisibleItem + 5) {
                    presenter.loadMedia(true)
                }
            }
        }
        gallery_list.addOnScrollListener(listener)
        gallery_list.adapter = adapter

        setExitSharedElementCallback(object : SharedElementCallback() {
            override fun onMapSharedElements(names: List<String>?, sharedElements: MutableMap<String, View>?) {
                super.onMapSharedElements(names, sharedElements)
                val position = presenter.currentPosition
                val selectedViewHolder = gallery_list.findViewHolderForAdapterPosition(position)
                if (selectedViewHolder?.itemView == null) {
                    return
                }
                val sharedView: View = selectedViewHolder.itemView.findViewById(R.id.image)
                val name = if (names!!.isNotEmpty()) names[0] else null
                if (name != null) {
                    sharedElements!![name] = sharedView
                }
            }
        })
    }

    override fun onClick(view: View, position: Int, pair: Pair<View, String>) {
        // Open image in viewpager
        presenter.currentPosition = position
        //(exist as TransitionSet).excludeTarget(view, true)
        //val fragment = ViewPagerGalleryFragment.newInstance()
        //navigationManager.moveToFragment(fragment, map)

        presenter.handleImagePress(position, pair)
    }

    override fun onLoaded(position: Int) {
        if (presenter.currentPosition == position) {
            startPostponedEnterTransition()
        }
    }

    override fun showLoading() {
        super<CoreActivity>.showLoading()
    }

    override fun hideLoading() {
        super<CoreActivity>.hideLoading()
    }

    override fun getPresenter(): BasePresenter {
        return presenter
    }

    override fun openImageDetail(conversationId: String, messages: MutableList<Message>, position: Int, pair: Pair<View, String>) {
        GroupImageGalleryActivity.start(this, conversationId, messages, position, false, pair)
    }

    override fun updateMessages(messages: List<Message>) {
        val data = messages.map {
            ImageMessage(it)
        }
        adapter.updateItems(data)
    }

    override fun updateMessage(message: Message, index: Int) {
        adapter.updateItem(ImageMessage(message))
    }
}
