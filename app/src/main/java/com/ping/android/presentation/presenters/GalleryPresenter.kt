package com.ping.android.presentation.presenters

import com.bzzzchat.cleanarchitecture.BasePresenter
import com.bzzzchat.cleanarchitecture.BaseView
import com.bzzzchat.cleanarchitecture.DefaultObserver
import com.ping.android.domain.usecase.conversation.LoadConversationMediaUseCase
import com.ping.android.model.Conversation
import com.ping.android.model.Message
import javax.inject.Inject

interface GalleryPresenter : BasePresenter {
    fun initConversation(conversation: Conversation)
    fun loadMedia()

    interface View : BaseView {
        fun displayMedia(messages: List<Message>)
    }
}

class GalleryPresenterImpl @Inject constructor() : GalleryPresenter {
    @Inject
    lateinit var view: GalleryPresenter.View

    @Inject
    lateinit var loadConversationMediaUseCase: LoadConversationMediaUseCase
    lateinit var conversation: Conversation
    var lastTimestamp = Double.MAX_VALUE

    override fun initConversation(conversation: Conversation) {
        this.conversation = conversation
    }

    override fun loadMedia() {
        val observer = object : DefaultObserver<LoadConversationMediaUseCase.Output>() {
            override fun onNext(t: LoadConversationMediaUseCase.Output) {
                super.onNext(t)
                t.messages.sortBy { it.timestamp }
                view.displayMedia(t.messages)
            }
        }
        loadConversationMediaUseCase.execute(observer,
                LoadConversationMediaUseCase.Params(conversation, lastTimestamp))
    }


}