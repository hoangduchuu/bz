package com.ping.android.presentation.view.fragment


import android.os.Bundle
import android.support.transition.TransitionInflater
import android.support.v4.app.Fragment
import android.support.v4.app.SharedElementCallback
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ping.android.R
import com.ping.android.dagger.loggedin.conversationdetail.gallery.GalleryComponent
import com.ping.android.dagger.loggedin.conversationdetail.gallery.GridGalleryComponent
import com.ping.android.dagger.loggedin.conversationdetail.gallery.GridGalleryModule
import com.ping.android.presentation.presenters.GalleryPresenter
import com.ping.android.presentation.view.adapter.ImagePagerAdapter
import kotlinx.android.synthetic.main.fragment_view_pager_gallery.*
import javax.inject.Inject

/**
 * A simple [Fragment] subclass.
 *
 */
class ViewPagerGalleryFragment : BaseFragment() {
    @Inject
    lateinit var presenter: GalleryPresenter

    val component: GridGalleryComponent by lazy {
        getComponent(GalleryComponent::class.java).provideGridGalleryComponent(GridGalleryModule())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        component.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_view_pager_gallery, container, false)
        //if (savedInstanceState == null) {
        postponeEnterTransition()
        //}
        prepareSharedElementTransition()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btn_back.setOnClickListener { activity?.onBackPressed() }
        bindData()
    }

    private fun bindData() {
        val messages = presenter.getMessageList()
        val adapter = ImagePagerAdapter(childFragmentManager, messages)
        viewpager.adapter = adapter
        viewpager.currentItem = presenter.currentPosition
        val listener = object : ViewPager.OnPageChangeListener {
            override fun onPageSelected(position: Int) {
                presenter.currentPosition = position
            }

            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }
        }
        viewpager.addOnPageChangeListener(listener)
    }

    private fun prepareSharedElementTransition() {
        val transition = TransitionInflater.from(context)
                .inflateTransition(R.transition.image_shared_element_transition)
        transition.duration = 375
        sharedElementEnterTransition = transition

        val callback = object : SharedElementCallback() {
            override fun onMapSharedElements(names: MutableList<String>?, sharedElements: MutableMap<String, View>?) {
                super.onMapSharedElements(names, sharedElements)
                // Locate the image view at the primary fragment (the ImageFragment
                // that is currently visible). To locate the fragment, call
                // instantiateItem with the selection position.
                // At this stage, the method will simply return the fragment at the
                // position and will not create a new one.
                val currentFragment = viewpager.adapter?.instantiateItem(viewpager, presenter.currentPosition) as? Fragment
                if (currentFragment?.view != null && names != null && sharedElements != null) {
                    sharedElements[names[0]] = currentFragment.view!!.findViewById(R.id.image_detail)
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
