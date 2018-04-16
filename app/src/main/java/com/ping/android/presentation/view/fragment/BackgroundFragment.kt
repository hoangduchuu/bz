package com.ping.android.presentation.view.fragment


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
import com.ping.android.model.Conversation
import com.ping.android.model.FirebaseImageItem
import com.ping.android.presentation.presenters.BackgroundPresenter
import com.ping.android.presentation.view.adapter.AdapterConstants
import com.ping.android.presentation.view.adapter.FlexibleAdapterV2
import com.ping.android.presentation.view.adapter.delegate.FirebaseBackgroundDelegateAdapter
import com.ping.android.utils.DataProvider
import kotlinx.android.synthetic.main.fragment_background.*
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
        val models = data.map {
            FirebaseImageItem(it)
        }
        adapter.addItems(models)
    }

    private fun initView() {
        btn_back.setOnClickListener { activity?.onBackPressed() }
        adapter = FlexibleAdapterV2()
        adapter.registerItemType(AdapterConstants.IMAGE, FirebaseBackgroundDelegateAdapter(clickListener = {
            this.handleBackgroundSelected(it)
        }))
        galleryList.adapter = adapter
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
