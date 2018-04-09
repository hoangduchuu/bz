package com.ping.android.presentation.view.fragment


import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bzzzchat.extensions.inflate
import com.ping.android.activity.R
import com.ping.android.presentation.presenters.GalleryPresenter
import com.ping.android.presentation.view.adapter.GalleryAdapter
import kotlinx.android.synthetic.main.fragment_grid_gallery.*
import javax.inject.Inject

class GridGalleryFragment : Fragment() {

    @Inject lateinit var presenter: GalleryPresenter

    private val galleryList by lazy {
        adapter = GalleryAdapter()
        gallery_list.adapter = adapter
        gallery_list.layoutManager = GridLayoutManager(context, 3)
        gallery_list
    }

    private lateinit var adapter: GalleryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return container?.inflate(R.layout.fragment_grid_gallery, false)
    }

    companion object {
        @JvmStatic
        fun newInstance() = GridGalleryFragment()
    }
}
