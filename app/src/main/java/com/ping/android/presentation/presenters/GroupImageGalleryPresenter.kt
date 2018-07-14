package com.ping.android.presentation.presenters

import com.bzzzchat.cleanarchitecture.BasePresenter
import com.bzzzchat.cleanarchitecture.BaseView
import com.bzzzchat.cleanarchitecture.DefaultObserver
import com.ping.android.domain.usecase.message.UpdateMaskChildMessagesUseCase
import com.ping.android.model.Message
import javax.inject.Inject

interface GroupImageGalleryPresenter : BasePresenter {
    fun togglePuzzle(message: Message, isMask: Boolean, conversationId: String)

    interface View : BaseView
}

class GroupImageGalleryPresenterImpl @Inject constructor() : GroupImageGalleryPresenter {
    @Inject
    lateinit var updateMaskChildMessagesUseCase: UpdateMaskChildMessagesUseCase

    override fun togglePuzzle(message: Message, isMask: Boolean, conversationId: String) {
        val observer = object: DefaultObserver<Boolean>() {}
        val params = UpdateMaskChildMessagesUseCase.Params()
        params.conversationId = conversationId
        params.isMask = isMask
        params.messages = arrayListOf(message)
        updateMaskChildMessagesUseCase.execute(observer, params)
    }
}