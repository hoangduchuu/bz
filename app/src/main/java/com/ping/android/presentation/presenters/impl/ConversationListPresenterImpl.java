package com.ping.android.presentation.presenters.impl;

import com.bzzzchat.cleanarchitecture.DefaultObserver;
import com.ping.android.domain.usecase.conversation.ObserveConversationsUseCase;
import com.ping.android.domain.usecase.conversation.DeleteConversationsUseCase;
import com.ping.android.domain.usecase.conversation.LoadMoreConversationUseCase;
import com.ping.android.domain.usecase.user.ObserveMappingsUseCase;
import com.ping.android.model.ChildData;
import com.ping.android.model.Conversation;
import com.ping.android.presentation.presenters.ConversationListPresenter;
import com.ping.android.utils.configs.Constant;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

/**
 * Created by tuanluong on 1/28/18.
 */

public class ConversationListPresenterImpl implements ConversationListPresenter {
    @Inject
    ObserveConversationsUseCase observeConversationsUseCase;
    @Inject
    DeleteConversationsUseCase deleteConversationsUseCase;
    @Inject
    LoadMoreConversationUseCase loadMoreConversationUseCase;
    @Inject
    ObserveMappingsUseCase observeMappingsUseCase;
    @Inject
    ConversationListPresenter.View view;

    private boolean canLoadMore = true;
    private double lastTimestamp = Double.MAX_VALUE;
    private AtomicBoolean isLoading;

    @Inject
    public ConversationListPresenterImpl() {
        isLoading = new AtomicBoolean(false);
    }

    @Override
    public void getConversations() {
        observeMappingsUseCase.execute(new DefaultObserver<Map<String, String>>() {
            @Override
            public void onNext(Map<String, String> mappings) {
                view.updateMappings(mappings);
            }
        }, null);
        loadMoreConversationUseCase.execute(new DefaultObserver<LoadMoreConversationUseCase.Output>() {
            @Override
            public void onNext(LoadMoreConversationUseCase.Output output) {
                Collections.sort(output.conversations, (o1, o2) -> Double.compare(o2.timesstamps, o1.timesstamps));
                view.updateConversationList(output.conversations);
                canLoadMore = output.canLoadMore;
                lastTimestamp = output.lastTimestamp;
                observeConversations();
            }

            @Override
            public void onError(@NotNull Throwable exception) {
                exception.printStackTrace();
                observeConversations();
            }
        }, lastTimestamp);
    }

    private void observeConversations() {
        observeConversationsUseCase.execute(new DefaultObserver<ChildData<Conversation>>() {
            @Override
            public void onNext(ChildData<Conversation> childData) {
                switch (childData.type) {
                    case CHILD_ADDED:
                        view.addConversation(childData.data);
                        break;
                    case CHILD_CHANGED:
                        if (childData.data.conversationType == Constant.CONVERSATION_TYPE_INDIVIDUAL) {
                            view.notifyConversationChange(childData.data);
                        }
                        view.updateConversation(childData.data);
                        break;
                    case CHILD_REMOVED:
                        view.deleteConversation(childData.data);
                        break;
                }
            }
        }, null);
    }

    @Override
    public void deleteConversations(List<Conversation> conversations) {
        view.showLoading();
        deleteConversationsUseCase.execute(new DefaultObserver<Boolean>() {
            @Override
            public void onNext(Boolean aBoolean) {
                view.hideLoading();
            }

            @Override
            public void onError(@NotNull Throwable exception) {
                view.hideLoading();
            }
        }, conversations);
    }

    @Override
    public void loadMore() {
        if (isLoading.get() || !canLoadMore) return;
        isLoading.set(true);
        lastTimestamp = lastTimestamp - 0.001;
        loadMoreConversationUseCase.execute(new DefaultObserver<LoadMoreConversationUseCase.Output>() {
            @Override
            public void onNext(LoadMoreConversationUseCase.Output output) {
                canLoadMore = output.canLoadMore;
                lastTimestamp = output.lastTimestamp;
                view.appendConversations(output.conversations);
                isLoading.set(false);
            }
        }, lastTimestamp);
    }

    @Override
    public void destroy() {
        observeConversationsUseCase.dispose();
        loadMoreConversationUseCase.dispose();
    }
}
