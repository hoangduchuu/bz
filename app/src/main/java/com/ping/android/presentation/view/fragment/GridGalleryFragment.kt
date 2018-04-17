package com.ping.android.presentation.view.fragment


import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bzzzchat.extensions.inflate
import com.ping.android.R
import com.ping.android.model.Conversation
import com.ping.android.presentation.presenters.GalleryPresenter
import com.ping.android.presentation.view.adapter.FlexibleAdapterV2
import kotlinx.android.synthetic.main.fragment_grid_gallery.*

class GridGalleryFragment : BaseFragment() {

    lateinit var presenter: GalleryPresenter

    private val galleryList by lazy {
        adapter = FlexibleAdapterV2()
        gallery_list.adapter = adapter
        gallery_list.layoutManager = GridLayoutManager(context, 3)
        gallery_list
    }

    private lateinit var adapter: FlexibleAdapterV2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

    companion object {
        @JvmStatic
        fun newInstance(conversation: Conversation) = GridGalleryFragment().apply {
            arguments = Bundle()
            arguments?.putParcelable("conversation", conversation)
        }
    }
}
