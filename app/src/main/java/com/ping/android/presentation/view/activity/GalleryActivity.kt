package com.ping.android.presentation.view.activity

import android.os.Bundle
import com.bzzzchat.cleanarchitecture.scopes.HasComponent
import com.ping.android.R
import com.ping.android.dagger.loggedin.conversationdetail.gallery.GalleryComponent
import com.ping.android.dagger.loggedin.conversationdetail.gallery.GalleryModule
import com.ping.android.model.Conversation
import com.ping.android.presentation.presenters.GalleryPresenter
import com.ping.android.presentation.view.fragment.GridGalleryFragment
import com.ping.android.utils.Navigator
import com.ping.android.utils.ThemeUtils
import javax.inject.Inject

class GalleryActivity : CoreActivity(), HasComponent<GalleryComponent>, GalleryPresenter.View {
    override val component: GalleryComponent by lazy {
        loggedInComponent.provideGalleryComponent(GalleryModule(this))
    }

    @Inject
    lateinit var navigator: Navigator
    @Inject
    lateinit var presenter: GalleryPresenter

    private lateinit var conversation: Conversation

    override fun onCreate(savedInstanceState: Bundle?) {
        intent.extras.let {
            conversation = intent.extras.getParcelable("conversation")
            ThemeUtils.onActivityCreateSetTheme(this, conversation.currentColor)
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)
        component.inject(this)

        navigator.init(supportFragmentManager, R.id.container)
        presenter.initConversation(conversation)
        presenter.loadMedia()
        navigator.openAsRoot(GridGalleryFragment.newInstance())
    }

    override fun onBackPressed() {
        navigator.navigateBack(this)
    }

    override fun showLoading() {
        super<CoreActivity>.showLoading()
    }

    override fun hideLoading() {
        super<CoreActivity>.hideLoading()
    }
}
