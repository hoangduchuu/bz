package com.ping.android.presentation.view.fragment


import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bzzzchat.extensions.inflate
import com.ping.android.activity.R
import com.ping.android.dagger.loggedin.conversationdetail.background.BackgroundComponent
import com.ping.android.dagger.loggedin.conversationdetail.ConversationDetailComponent
import com.ping.android.dagger.loggedin.conversationdetail.background.BackgroundModule
import com.ping.android.model.CameraItem
import com.ping.android.model.Conversation
import com.ping.android.model.FirebaseImageItem
import com.ping.android.model.GalleryItem
import com.ping.android.presentation.presenters.BackgroundPresenter
import com.ping.android.presentation.view.adapter.AdapterConstants
import com.ping.android.presentation.view.adapter.FlexibleAdapterV2
import com.ping.android.presentation.view.adapter.ViewType
import com.ping.android.presentation.view.adapter.delegate.CameraItemDelegateAdapter
import com.ping.android.presentation.view.adapter.delegate.FirebaseBackgroundDelegateAdapter
import com.ping.android.presentation.view.adapter.delegate.GalleryItemDelegateAdapter
import com.ping.android.utils.DataProvider
import com.ping.android.utils.ImagePickerHelper
import io.reactivex.functions.Consumer
import kotlinx.android.synthetic.main.fragment_background.*
import java.io.File
import javax.inject.Inject

class BackgroundFragment : BaseFragment(), BackgroundPresenter.View {
    @Inject
    lateinit var presenter: BackgroundPresenter

    val component: BackgroundComponent by lazy {
        getComponent(ConversationDetailComponent::class.java)
                .provideBackgroundComponent(BackgroundModule(this))
    }

    private val galleryList by lazy {
        gallery_list.layoutManager = GridLayoutManager(context, 3)
        gallery_list
    }

    private lateinit var adapter: FlexibleAdapterV2
    private lateinit var imagePickerHelper: ImagePickerHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        component.inject(this)
        arguments.let {
            val conversation: Conversation? = it?.getParcelable("conversation")
            if (conversation != null) {
                presenter.initConversation(conversation)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? = container?.inflate(R.layout.fragment_background, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        val data = DataProvider.getDefaultBackgrounds()
        var models: MutableList<ViewType> = ArrayList()
        models.add(CameraItem())
        models.add(GalleryItem())
        models.addAll(data.map {
            FirebaseImageItem(it)
        })
        adapter.addItems(models)
    }

    private fun initView() {
        btn_back.setOnClickListener { activity?.onBackPressed() }
        adapter = FlexibleAdapterV2()
        adapter.registerItemType(AdapterConstants.IMAGE, FirebaseBackgroundDelegateAdapter(clickListener = {
            this.handleBackgroundSelected(it)
        }))
        adapter.registerItemType(AdapterConstants.CAMERA, CameraItemDelegateAdapter(clickListener = {
            this.handleCameraClicked()
        }))
        adapter.registerItemType(AdapterConstants.GALLERY, GalleryItemDelegateAdapter(clickListener = {
            this.handleGalleryClicked()
        }))
        galleryList.adapter = adapter

        imagePickerHelper = ImagePickerHelper
                .from(this)
                .setCrop(true)

        registerEvent(imagePickerHelper.fileObservable.subscribe({
            presenter.uploadConversationBackground(it.absolutePath)
        }))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (imagePickerHelper != null) {
            imagePickerHelper.onActivityResult(requestCode, resultCode, data)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (imagePickerHelper != null) {
            imagePickerHelper.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun handleGalleryClicked() {
        imagePickerHelper.openPicker()
    }

    private fun handleCameraClicked() {
        imagePickerHelper.openCamera()
    }

    override fun showLoading() {
        super<BaseFragment>.showLoading()
    }

    override fun hideLoading() {
        super<BaseFragment>.hideLoading()
    }

    private fun handleBackgroundSelected(it: FirebaseImageItem) {
        presenter.changeBackground(it.imageUrl)
    }

    // region BackgroundPresenter.View
    override fun navigateBack() {
        activity?.onBackPressed()
    }

    // endregion

    companion object {
        @JvmStatic
        fun newInstance(conversation: Conversation) = BackgroundFragment().apply {
            arguments = Bundle()
            arguments?.putParcelable("conversation", conversation)
        }
    }
}
