package com.ping.android.presentation.presenters

import com.bzzzchat.cleanarchitecture.BasePresenter
import com.bzzzchat.cleanarchitecture.BaseView
import com.ping.android.model.Conversation

interface GalleryPresenter : BasePresenter {
    fun initConversation(conversation: Conversation)

    interface View : BaseView {

    }
}