package com.ping.android.presentation.presenters

import com.bzzzchat.cleanarchitecture.DefaultObserver
import com.ping.android.domain.usecase.conversation.UpdateConversationBackgroundUseCase
import com.ping.android.model.Conversation
import javax.inject.Inject

interface BackgroundPresenter {
    fun changeBackground(url: String)

    fun initConversation(conversation: Conversation)

    interface View {
        fun navigateBack()
    }
}

class BackgroundPresenterImpl @Inject constructor(): BackgroundPresenter {
    @Inject
    lateinit var view: BackgroundPresenter.View
    @Inject
    lateinit var updateConversationBackgroundUseCase: UpdateConversationBackgroundUseCase

    private lateinit var conversation: Conversation

    override fun initConversation(conversation: Conversation) {
        this.conversation = conversation
    }

    override fun changeBackground(url: String) {
        var observer = object : DefaultObserver<Boolean>() {
            override fun onComplete() {

            }

            override fun onNext(t: Boolean) {
                view.navigateBack()
            }

            override fun onError(e: Throwable) {

            }
        }
        updateConversationBackgroundUseCase.execute(observer = observer,
                params = UpdateConversationBackgroundUseCase.Params(this.conversation, url))
    }
}