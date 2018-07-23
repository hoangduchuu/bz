package com.ping.android.presentation.presenters

import com.bzzzchat.cleanarchitecture.BasePresenter
import com.bzzzchat.cleanarchitecture.BaseView
import com.bzzzchat.cleanarchitecture.DefaultObserver
import com.ping.android.R.id.view
import com.ping.android.domain.usecase.message.DownloadImageUseCase
import com.ping.android.domain.usecase.message.UpdateMaskChildMessagesUseCase
import com.ping.android.domain.usecase.message.UpdateMaskMessagesUseCase
import com.ping.android.model.Message
import javax.inject.Inject

interface GroupImageGalleryPresenter : BasePresenter {
    fun init(conversationId: String, messages: List<Message>, isGroupImage: Boolean)

    fun togglePuzzle(position: Int)
    fun downloadImage(message: Message)

    interface View : BaseView {
        fun showImageMessages(messages: List<Message>)

        fun updateMessage(message: Message, position: Int)

        fun showMessageDownloadSuccessfully()
    }
}

class GroupImageGalleryPresenterImpl @Inject constructor() : GroupImageGalleryPresenter {
    @Inject
    lateinit var updateMaskMessagesUseCase: UpdateMaskMessagesUseCase
    @Inject
    lateinit var updateMaskChildMessagesUseCase: UpdateMaskChildMessagesUseCase
    @Inject
    lateinit var downloadImageUseCase: DownloadImageUseCase
    @Inject
    lateinit var view: GroupImageGalleryPresenter.View

    var messages: List<Message> = ArrayList()
    var conversationId: String = ""
    private var isGroupImage = false

    override fun init(conversationId: String, messages: List<Message>, isGroupImage: Boolean) {
        this.conversationId = conversationId
        this.messages = messages
        this.isGroupImage = isGroupImage

        view.showImageMessages(messages)
    }

    override fun togglePuzzle(position: Int) {
        val isMask = !messages[position].isMask
        val message = messages[position]
        if (message.parentKey == null || message.parentKey.isEmpty()) {
            val params = UpdateMaskMessagesUseCase.Params()
            params.conversationId = conversationId
            params.isLastMessage = false
            params.isMask = isMask
            params.setMessageId(message.key)
            updateMaskMessagesUseCase.execute(DefaultObserver<Boolean>(), params)
        } else {
            val params = UpdateMaskChildMessagesUseCase.Params()
            params.conversationId = conversationId
            params.isMask = isMask
            params.messages = arrayListOf(message)
            updateMaskChildMessagesUseCase.execute(DefaultObserver<Boolean>(), params)
        }
        messages[position].isMask = isMask
        view.updateMessage(messages[position], position)
    }

    override fun downloadImage(message: Message) {
        val observer = object: DefaultObserver<Boolean>() {
            override fun onNext(t: Boolean) {
                view.showMessageDownloadSuccessfully()
            }
        }
        downloadImageUseCase.execute(observer, message)
    }
}