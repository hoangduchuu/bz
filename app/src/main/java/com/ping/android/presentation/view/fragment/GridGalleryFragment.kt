package com.ping.android.presentation.view.fragment


import android.os.Bundle
import android.support.transition.TransitionInflater
import android.support.transition.TransitionSet
import android.support.v4.app.SharedElementCallback
import android.support.v4.widget.NestedScrollView
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import com.bzzzchat.extensions.inflate
import com.ping.android.R
import com.ping.android.dagger.loggedin.conversationdetail.gallery.GalleryComponent
import com.ping.android.dagger.loggedin.conversationdetail.gallery.GridGalleryComponent
import com.ping.android.dagger.loggedin.conversationdetail.gallery.GridGalleryModule
import com.ping.android.model.ImageMessage
import com.ping.android.presentation.presenters.GalleryPresenter
import com.ping.android.presentation.presenters.MediaItemsEvent
import com.ping.android.presentation.view.adapter.AdapterConstants
import com.ping.android.presentation.view.adapter.FlexibleAdapterV2
import com.ping.android.presentation.view.adapter.delegate.FirebaseMessageDelegateAdapter
import com.ping.android.utils.Log
import com.ping.android.utils.Navigator
import com.ping.android.utils.bus.BusProvider
import kotlinx.android.synthetic.main.fragment_grid_gallery.*
import javax.inject.Inject

class GridGalleryFragment : BaseFragment(), FirebaseMessageDelegateAdapter.FirebaseMessageListener {
    @Inject
    lateinit var navigationManager: Navigator
    @Inject
    lateinit var galleryPresenter: GalleryPresenter
    @Inject
    lateinit var busProvider: BusProvider

    val component: GridGalleryComponent by lazy {
        getComponent(GalleryComponent::class.java)
                .provideGridGalleryComponent(GridGalleryModule())
    }

    private lateinit var adapter: FlexibleAdapterV2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        component.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = container?.inflate(R.layout.fragment_grid_gallery, false)
        prepareTransitions()
        postponeEnterTransition()

        registerEvent(busProvider.events.subscribe({
            if (it is MediaItemsEvent) {
                val data = it.messages.map {
                    ImageMessage(it)
                }
                adapter.addItems(data)
            }
        }))
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btn_back.setOnClickListener { activity?.onBackPressed() }
        adapter = FlexibleAdapterV2()
        adapter.registerItemType(AdapterConstants.IMAGE, FirebaseMessageDelegateAdapter(this))
        val messages = galleryPresenter.getMessageList()
        val data = messages.map {
            ImageMessage(it)
        }
        gallery_list.layoutManager = GridLayoutManager(context, 3)
        val listener = object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val totalItem = gallery_list.layoutManager.itemCount
                val lastVisibleItem = (gallery_list.layoutManager as GridLayoutManager).findLastVisibleItemPosition()
                if (totalItem <= lastVisibleItem + 5) {
                    galleryPresenter.loadMedia(true)
                }
            }
        }
        gallery_list.addOnScrollListener(listener)
        adapter.addItems(data)
        gallery_list.adapter = adapter
    }

    override fun onClick(view: View, position: Int, map: Map<String, View>) {
        // Open image in viewpager
        galleryPresenter.currentPosition = position
        (exitTransition as TransitionSet).excludeTarget(view, true)

        navigationManager.moveToFragment(ViewPagerGalleryFragment.newInstance(), map)
    }

    override fun onLoaded(position: Int) {
        if (galleryPresenter.currentPosition == position) {
            startPostponedEnterTransition()
        }
    }

    private fun prepareTransitions() {
        val transition = TransitionInflater.from(context)
                .inflateTransition(R.transition.grid_exit_transition)
        transition.duration = 375
        exitTransition = transition

        val callback = object : SharedElementCallback() {
            override fun onMapSharedElements(names: MutableList<String>?, sharedElements: MutableMap<String, View>?) {
                // Locate the viewholder for clicked position
                val selectedViewHolder = gallery_list.findViewHolderForAdapterPosition(galleryPresenter.currentPosition)
                if (selectedViewHolder?.itemView == null) {
                    return
                }
                sharedElements!![names!![0]] = selectedViewHolder.itemView.findViewById(R.id.image)
            }
        }
        setExitSharedElementCallback(callback)
    }

    override fun showLoading() {
        super<BaseFragment>.showLoading()
    }

    override fun hideLoading() {
        super<BaseFragment>.hideLoading()
    }

//    override fun displayMedia(messages: List<Message>) {
//val data = messages.map {
//    ImageMessage(it)
//}
//    adapter.addItems(data)
//    }

    companion object {
        @JvmStatic
        fun newInstance() = GridGalleryFragment().apply {
        }
    }
}
