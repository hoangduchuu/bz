package com.ping.android.presentation.view.fragment


import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bzzzchat.extensions.inflate
import com.ping.android.R
import com.ping.android.dagger.loggedin.conversationdetail.ConversationDetailComponent
import com.ping.android.dagger.loggedin.conversationdetail.gallery.GalleryComponent
import com.ping.android.dagger.loggedin.conversationdetail.gallery.GalleryModule
import com.ping.android.dagger.loggedin.main.conversation.ConversationComponent
import com.ping.android.model.Conversation
import com.ping.android.model.ImageMessage
import com.ping.android.model.Message
import com.ping.android.presentation.presenters.GalleryPresenter
import com.ping.android.presentation.view.adapter.AdapterConstants
import com.ping.android.presentation.view.adapter.FlexibleAdapterV2
import com.ping.android.presentation.view.adapter.delegate.FirebaseMessageDelegateAdapter
import kotlinx.android.synthetic.main.fragment_grid_gallery.*
import javax.inject.Inject

class GridGalleryFragment : BaseFragment(), GalleryPresenter.View {
    @Inject lateinit var presenter: GalleryPresenter

    val component: GalleryComponent by lazy {
        getComponent(ConversationDetailComponent::class.java)
                .provideGalleryComponent(GalleryModule(this))
    }

    private val galleryList by lazy {
        gallery_list.layoutManager = GridLayoutManager(context, 3)
        gallery_list
    }

    private lateinit var adapter: FlexibleAdapterV2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        component.inject(this)
        arguments.let {
            val conversation = arguments?.get("conversation") as? Conversation
            if (conversation != null) {
                presenter.initConversation(conversation)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return container?.inflate(R.layout.fragment_grid_gallery, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btn_back.setOnClickListener { activity?.onBackPressed() }
        adapter = FlexibleAdapterV2()
        adapter.registerItemType(AdapterConstants.IMAGE, FirebaseMessageDelegateAdapter(clickListener = {
            // Open image in viewpager
        }))
        galleryList.adapter = adapter
        presenter.loadMedia()
    }

    override fun showLoading() {
        super<BaseFragment>.showLoading()
    }

    override fun hideLoading() {
        super<BaseFragment>.hideLoading()
    }

    override fun displayMedia(messages: List<Message>) {
        val data = messages.map {
            ImageMessage(it)
        }
        adapter.addItems(data)
    }

    companion object {
        @JvmStatic
        fun newInstance(conversation: Conversation) = GridGalleryFragment().apply {
            arguments = Bundle()
            arguments?.putParcelable("conversation", conversation)
        }
    }
}
