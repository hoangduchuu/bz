package com.ping.android.presentation.presenters

import com.bzzzchat.cleanarchitecture.BasePresenter
import com.bzzzchat.cleanarchitecture.BaseView
import com.bzzzchat.cleanarchitecture.DefaultObserver
import com.ping.android.domain.usecase.conversation.LoadConversationMediaUseCase
import com.ping.android.domain.usecase.conversation.ObserveMediaChangeUseCase
import com.ping.android.domain.usecase.message.UpdateMaskChildMessagesUseCase
import com.ping.android.domain.usecase.message.UpdateMaskMessagesUseCase
import com.ping.android.model.Conversation
import com.ping.android.model.Message
import com.ping.android.model.enums.MessageType
import com.ping.android.utils.bus.BusProvider
import com.ping.android.utils.bus.events.MessageUpdateEvent
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

interface GalleryPresenter : BasePresenter {
    fun initConversation(conversation: Conversation)

    fun loadMedia(isLoadMore: Boolean = false)

    fun getMessageList(): List<Message>

    fun updateMask(message: Message, isMask: Boolean)

    var currentPosition: Int

    interface View : BaseView {
        //fun displayMedia(messages: List<Message>)
    }
}

data class MediaItemsEvent(var messages: List<Message>)

class GalleryPresenterImpl @Inject constructor() : GalleryPresenter {
    @Inject
    lateinit var view: GalleryPresenter.View
    @Inject
    lateinit var loadConversationMediaUseCase: LoadConversationMediaUseCase
    @Inject
    lateinit var updateMaskMessagesUseCase: UpdateMaskMessagesUseCase
    @Inject
    lateinit var updateMaskChildMessagesUseCase: UpdateMaskChildMessagesUseCase
    @Inject
    lateinit var observeMediaChangeUseCase: ObserveMediaChangeUseCase
    @Inject
    lateinit var busProvider: BusProvider

    lateinit var conversation: Conversation
    override var currentPosition = 0
    var messages: MutableList<Message> = ArrayList()
    var lastTimestamp = Double.MAX_VALUE
    var isLoading = AtomicBoolean(false)
    var canLoadMore = true

    override fun initConversation(conversation: Conversation) {
        this.conversation = conversation
        val observer = object : DefaultObserver<Message>() {
            override fun onNext(t: Message) {
                if (t.type == MessageType.IMAGE_GROUP) {
                    for (message in t.childMessages) {
                        val index = messages.indexOf(message)
                        if (index >= 0) {
                            messages[index] = message
                            busProvider.post(MessageUpdateEvent(message, index))
                        }
                    }
                } else {
                    val index = messages.indexOf(t)
                    if (index >= 0) {
                        messages[index] = t
                        busProvider.post(MessageUpdateEvent(t, index))
                    }
                }
            }
        }
        observeMediaChangeUseCase.execute(observer, conversation)
    }

    override fun loadMedia(isLoadMore: Boolean) {
        if (isLoading.get() || !canLoadMore) {
            return
        }
        if (!isLoadMore) {
            view.showLoading()
        }
        isLoading.set(true)
        val observer = object : DefaultObserver<LoadConversationMediaUseCase.Output>() {
            override fun onNext(t: LoadConversationMediaUseCase.Output) {
                canLoadMore = t.canLoadMore
                if (t.messages.size > 0) {
                    val result: MutableList<Message> = ArrayList()
                    for (message in t.messages) {
                        if (message.type == MessageType.IMAGE_GROUP) {
                            result.addAll(message.childMessages)
                        } else {
                            result.add(message)
                        }
                    }
                    result.sortByDescending { it.timestamp }
                    lastTimestamp = result.last().timestamp - 0.001
                    messages.addAll(result)
                    busProvider.post(MediaItemsEvent(result))
                }
                isLoading.set(false)
                view.hideLoading()
                //view.showGridGallery()
            }

            override fun onError(exception: Throwable) {
                view.hideLoading()
                isLoading.set(false)
            }
        }
        loadConversationMediaUseCase.execute(observer,
                LoadConversationMediaUseCase.Params(conversation, lastTimestamp))
    }

    override fun getMessageList(): List<Message> {
        return messages
    }

    override fun updateMask(message: Message, isMask: Boolean) {
        if (message.parentKey == null || message.parentKey.isEmpty()) {
            val params = UpdateMaskMessagesUseCase.Params()
            params.conversationId = conversation.key
            params.isLastMessage = false
            params.isMask = isMask
            params.setMessageId(message.key)
            updateMaskMessagesUseCase.execute(DefaultObserver<Boolean>(), params)
        } else {
            val params = UpdateMaskChildMessagesUseCase.Params()
            params.conversationId = conversation.key
            params.isMask = isMask
            params.messages = arrayListOf(message)
            updateMaskChildMessagesUseCase.execute(DefaultObserver<Boolean>(), params)
        }
    }

    override fun destroy() {
        observeMediaChangeUseCase.dispose()
    }
}