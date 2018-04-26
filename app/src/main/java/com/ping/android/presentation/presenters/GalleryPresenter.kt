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

    fun getMessageList(): List<Message>

    var currentPosition: Int

    interface View : BaseView {
        fun showGridGallery()
        //fun displayMedia(messages: List<Message>)
    }
}

class GalleryPresenterImpl @Inject constructor() : GalleryPresenter {
    @Inject
    lateinit var view: GalleryPresenter.View

    @Inject
    lateinit var loadConversationMediaUseCase: LoadConversationMediaUseCase
    lateinit var conversation: Conversation
    override var currentPosition = 0
    var messages: List<Message> = ArrayList()
    var lastTimestamp = Double.MAX_VALUE

    override fun initConversation(conversation: Conversation) {
        this.conversation = conversation
    }

    override fun loadMedia() {
        view.showLoading()
        val observer = object : DefaultObserver<LoadConversationMediaUseCase.Output>() {
            override fun onNext(t: LoadConversationMediaUseCase.Output) {
                super.onNext(t)
                view.hideLoading()
                t.messages.sortByDescending { it.timestamp }
                messages = t.messages
                view.showGridGallery()
            }

            override fun onError(exception: Throwable) {
                view.hideLoading()
            }
        }
        loadConversationMediaUseCase.execute(observer,
                LoadConversationMediaUseCase.Params(conversation, lastTimestamp))
    }

    override fun getMessageList(): List<Message> {
        return messages
    }
}