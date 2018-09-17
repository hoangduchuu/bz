package com.ping.android.presentation.presenters.impl

import com.bzzzchat.cleanarchitecture.DefaultObserver
import com.ping.android.data.entity.ChildData
import com.ping.android.domain.usecase.conversation.DeleteConversationsUseCase
import com.ping.android.domain.usecase.conversation.LoadMoreConversationUseCase
import com.ping.android.domain.usecase.conversation.ObserveConversationsUseCase
import com.ping.android.domain.usecase.user.ObserveMappingsUseCase
import com.ping.android.model.Conversation
import com.ping.android.presentation.presenters.ConversationListPresenter
import com.ping.android.utils.bus.BusProvider
import com.ping.android.utils.bus.events.ConversationUpdateEvent
import com.ping.android.utils.configs.Constant
import io.reactivex.disposables.CompositeDisposable

import java.util.ArrayList
import java.util.Collections
import java.util.concurrent.atomic.AtomicBoolean

import javax.inject.Inject

/**
 * Created by tuanluong on 1/28/18.
 */

class ConversationListPresenterImpl @Inject
constructor() : ConversationListPresenter {
    @Inject
    lateinit var observeConversationsUseCase: ObserveConversationsUseCase
    @Inject
    lateinit var deleteConversationsUseCase: DeleteConversationsUseCase
    @Inject
    lateinit var loadMoreConversationUseCase: LoadMoreConversationUseCase
    @Inject
    lateinit var observeMappingsUseCase: ObserveMappingsUseCase
    @Inject
    @JvmField
    var view: ConversationListPresenter.View? = null

    @Inject
    lateinit var busProvider: BusProvider
    val disposables = CompositeDisposable()
    private var canLoadMore = true
    private var lastTimestamp = java.lang.Double.MAX_VALUE
    private val isLoading: AtomicBoolean = AtomicBoolean(false)
    private val conversations: MutableList<Conversation>

    init {
        conversations = ArrayList()
    }

    override fun getConversations() {
        view?.showConnecting()
        observeMappingsUseCase.execute(object : DefaultObserver<Map<String, String>>() {
            override fun onNext(mappings: Map<String, String>) {
                view?.updateMappings(mappings)
            }
        }, null)
        loadMoreConversationUseCase.execute(object : DefaultObserver<LoadMoreConversationUseCase.Output>() {
            override fun onNext(output: LoadMoreConversationUseCase.Output) {
                output.conversations.sortWith(Comparator { o1, o2 -> java.lang.Double.compare(o2.timesstamps, o1.timesstamps) })
                conversations.addAll(output.conversations)
                view?.updateConversationList(output.conversations)
                canLoadMore = output.canLoadMore
                lastTimestamp = output.lastTimestamp
                observeConversations()
                view?.hideConnecting()
            }

            override fun onError(exception: Throwable) {
                exception.printStackTrace()
                observeConversations()
                view?.hideConnecting()
            }
        }, lastTimestamp)
    }

    private fun observeConversations() {
        observeConversationsUseCase.execute(object : DefaultObserver<ChildData<Conversation>>() {
            override fun onNext(childData: ChildData<Conversation>) {
                when (childData.type) {
                    ChildData.Type.CHILD_ADDED -> view?.addConversation(childData.data)
                    ChildData.Type.CHILD_CHANGED -> {
                        if (childData.data.conversationType == Constant.CONVERSATION_TYPE_INDIVIDUAL) {
                            view?.notifyConversationChange(childData.data)
                        }
                        view?.updateConversation(childData.data)
                    }
                    ChildData.Type.CHILD_REMOVED -> view?.deleteConversation(childData.data)
                }
            }
        }, null)
        val disposable = busProvider.events.subscribe {
            if (it is ConversationUpdateEvent) {
                view?.updateConversation(it.conversation)
            }
        }
        disposables.add(disposable)
    }

    override fun deleteConversations(conversations: List<Conversation>) {
        view?.showLoading()
        deleteConversationsUseCase.execute(object : DefaultObserver<Boolean>() {
            override fun onNext(t: Boolean) {
                view?.hideLoading()
            }

            override fun onError(exception: Throwable) {
                view?.hideLoading()
            }
        }, conversations)
    }

    override fun loadMore() {
        if (isLoading.get() || !canLoadMore) return
        isLoading.set(true)
        lastTimestamp -= 0.001
        loadMoreConversationUseCase.execute(object : DefaultObserver<LoadMoreConversationUseCase.Output>() {
            override fun onNext(output: LoadMoreConversationUseCase.Output) {
                canLoadMore = output.canLoadMore
                lastTimestamp = output.lastTimestamp
                view?.appendConversations(output.conversations)
                isLoading.set(false)
            }
        }, lastTimestamp)
    }

    override fun destroy() {
        view = null
        disposables.dispose()
        observeConversationsUseCase.dispose()
        loadMoreConversationUseCase.dispose()
        deleteConversationsUseCase.dispose()
        observeMappingsUseCase.dispose()
    }
}
