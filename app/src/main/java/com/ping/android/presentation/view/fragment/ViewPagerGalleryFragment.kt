package com.ping.android.presentation.view.fragment


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ping.android.R

/**
 * A simple [Fragment] subclass.
 *
 */
class ViewPagerGalleryFragment : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_view_pager_gallery, container, false)
    }

    companion object {
        @JvmStatic
        fun newInstance() = ViewPagerGalleryFragment().apply {

        }
    }
}
