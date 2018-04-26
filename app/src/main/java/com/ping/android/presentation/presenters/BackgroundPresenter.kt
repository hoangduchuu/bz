package com.ping.android.presentation.presenters

import com.bzzzchat.cleanarchitecture.BasePresenter
import com.bzzzchat.cleanarchitecture.BaseView
import com.bzzzchat.cleanarchitecture.DefaultObserver
import com.ping.android.domain.usecase.conversation.GetDefaultBackgroundsUseCase
import com.ping.android.domain.usecase.conversation.UpdateConversationBackgroundUseCase
import com.ping.android.domain.usecase.conversation.UploadConversationBackgroundUseCase
import com.ping.android.model.Conversation
import javax.inject.Inject

interface BackgroundPresenter: BasePresenter {
    fun changeBackground(url: String)
    fun initConversation(conversation: Conversation)
    fun uploadConversationBackground(absolutePath: String)

    interface View: BaseView {
        fun navigateBack()
        fun updateBackgrounds(t: List<String>)
    }
}

class BackgroundPresenterImpl @Inject constructor() : BackgroundPresenter {
    @Inject
    lateinit var view: BackgroundPresenter.View
    @Inject
    lateinit var getDefaultBackgroundsUseCase: GetDefaultBackgroundsUseCase
    @Inject
    lateinit var updateConversationBackgroundUseCase: UpdateConversationBackgroundUseCase
    @Inject
    lateinit var uploadConversationBackgroundUseCase: UploadConversationBackgroundUseCase

    private lateinit var conversation: Conversation

    override fun initConversation(conversation: Conversation) {
        this.conversation = conversation
    }

    override fun create() {
        val observer = object : DefaultObserver<List<String>>() {
            override fun onNext(t: List<String>) {
                view.updateBackgrounds(t)
            }
        }
        getDefaultBackgroundsUseCase.execute(observer, null)
    }

    override fun changeBackground(url: String) {
        val observer = object : DefaultObserver<Boolean>() {
            override fun onComplete() {

            }

            override fun onNext(t: Boolean) {
                view.hideLoading()
                view.navigateBack()
            }

            override fun onError(e: Throwable) {
                view.hideLoading()
            }
        }
        view.showLoading()
        updateConversationBackgroundUseCase.execute(observer = observer,
                params = UpdateConversationBackgroundUseCase.Params(this.conversation, url))
    }

    override fun uploadConversationBackground(absolutePath: String) {
        view.showLoading()
        var observer = object : DefaultObserver<Boolean>() {
            override fun onNext(t: Boolean) {
                view.navigateBack()
                view.hideLoading()
            }

            override fun onError(exception: Throwable) {
                view.hideLoading()
            }
        }
        uploadConversationBackgroundUseCase.execute(observer,
                UploadConversationBackgroundUseCase.Params(absolutePath, conversation))
    }
}
