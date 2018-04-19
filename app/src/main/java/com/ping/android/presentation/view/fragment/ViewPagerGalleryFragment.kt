package com.ping.android.presentation.view.fragment


import android.os.Bundle
import android.support.transition.TransitionInflater
import android.support.v4.app.Fragment
import android.support.v4.app.SharedElementCallback
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ping.android.R
import com.ping.android.presentation.view.activity.ConversationDetailActivity

import kotlinx.android.synthetic.main.fragment_view_pager_gallery.*;
/**
 * A simple [Fragment] subclass.
 *
 */
class ViewPagerGalleryFragment : BaseFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_view_pager_gallery, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prepareSharedElementTransition()
    }

    private fun prepareSharedElementTransition() {
        val transition = TransitionInflater.from(context)
                .inflateTransition(R.transition.image_shared_element_transition)
        sharedElementEnterTransition = transition

        val callback = object : SharedElementCallback() {
            override fun onMapSharedElements(names: MutableList<String>?, sharedElements: MutableMap<String, View>?) {
                super.onMapSharedElements(names, sharedElements)
                // Locate the image view at the primary fragment (the ImageFragment
                // that is currently visible). To locate the fragment, call
                // instantiateItem with the selection position.
                // At this stage, the method will simply return the fragment at the
                // position and will not create a new one.
                val currentFragment= viewpager.adapter?.instantiateItem(viewpager, ConversationDetailActivity.currentPosition) as? Fragment
                if (currentFragment?.view != null && names != null && sharedElements != null) {
                    sharedElements[names[0]] = currentFragment.view!!.findViewById(R.id.image)
                }
            }
        }
        setEnterSharedElementCallback(callback)
    }

    companion object {
        @JvmStatic
        fun newInstance() = ViewPagerGalleryFragment().apply {

        }
    }
}
