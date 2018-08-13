package com.ping.android.presentation.presenters

import androidx.core.util.Pair
import android.view.View
import com.bzzzchat.cleanarchitecture.BasePresenter
import com.bzzzchat.cleanarchitecture.BaseView
import com.bzzzchat.cleanarchitecture.DefaultObserver
import com.ping.android.domain.usecase.conversation.LoadConversationMediaUseCase
import com.ping.android.domain.usecase.conversation.ObserveMediaChangeUseCase
import com.ping.android.model.Conversation
import com.ping.android.model.Message
import com.ping.android.model.enums.MessageType
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

interface GalleryPresenter : BasePresenter {
    fun initConversation(conversation: Conversation)

    fun loadMedia(isLoadMore: Boolean = false)

    fun getMessageList(): List<Message>

    fun handleImagePress(position: Int, pair: Pair<android.view.View, String>)

    var currentPosition: Int

    interface View : BaseView {
        fun openImageDetail(conversationId: String, messages: MutableList<Message>, position: Int, pair: Pair<android.view.View, String>)
        fun updateMessages(messages: List<Message>)
        fun updateMessage(message: Message, index: Int)
    }
}

data class MediaItemsEvent(var messages: List<Message>)

class GalleryPresenterImpl @Inject constructor() : GalleryPresenter {
    @JvmField
    @Inject
    var view: GalleryPresenter.View? = null
    @Inject
    lateinit var loadConversationMediaUseCase: LoadConversationMediaUseCase
    @Inject
    lateinit var observeMediaChangeUseCase: ObserveMediaChangeUseCase

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
                            view?.updateMessage(message, index)
                        }
                    }
                } else {
                    val index = messages.indexOf(t)
                    if (index >= 0) {
                        messages[index] = t
                        view?.updateMessage(t, index)
                    }
                }
            }

            override fun onError(exception: Throwable) {
                exception.printStackTrace()
            }
        }
        observeMediaChangeUseCase.execute(observer, conversation)
    }

    override fun loadMedia(isLoadMore: Boolean) {
        if (isLoading.get() || !canLoadMore) {
            return
        }
        if (!isLoadMore) {
            view?.showLoading()
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
                    view?.updateMessages(result)
                }
                isLoading.set(false)
                view?.hideLoading()
                //view?.showGridGallery()
            }

            override fun onError(exception: Throwable) {
                view?.hideLoading()
                isLoading.set(false)
            }
        }
        loadConversationMediaUseCase.execute(observer,
                LoadConversationMediaUseCase.Params(conversation, lastTimestamp))
    }

    override fun getMessageList(): List<Message> {
        return messages
    }

    override fun handleImagePress(position: Int, pair: Pair<View, String>) {
        view?.openImageDetail(conversation.key, messages, position, pair)
    }

    override fun destroy() {
        view = null
        observeMediaChangeUseCase.dispose()
    }
}