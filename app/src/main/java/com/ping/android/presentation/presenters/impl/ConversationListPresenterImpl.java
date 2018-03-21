package com.ping.android.presentation.presenters.impl;

import com.bzzzchat.cleanarchitecture.DefaultObserver;
import com.bzzzchat.rxfirebase.database.ChildEvent;
import com.ping.android.domain.usecase.ObserveConversationsUseCase;
import com.ping.android.domain.usecase.ObserveGroupsUseCase;
import com.ping.android.domain.usecase.conversation.DeleteConversationsUseCase;
import com.ping.android.domain.usecase.conversation.GetLastConversationsUseCase;
import com.ping.android.model.ChildData;
import com.ping.android.model.Conversation;
import com.ping.android.model.Group;
import com.ping.android.presentation.presenters.ConversationListPresenter;
import com.ping.android.ultility.Constant;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import javax.inject.Inject;

/**
 * Created by tuanluong on 1/28/18.
 */

public class ConversationListPresenterImpl implements ConversationListPresenter {
    @Inject
    ObserveConversationsUseCase observeConversationsUseCase;
    @Inject
    GetLastConversationsUseCase getLastConversationsUseCase;
    @Inject
    DeleteConversationsUseCase deleteConversationsUseCase;
    @Inject
    ConversationListPresenter.View view;

    int unreadNum;

    @Inject
    public ConversationListPresenterImpl() {}

    @Override
    public void getConversations() {
        getLastConversationsUseCase.execute(new DefaultObserver<List<Conversation>>() {
            @Override
            public void onNext(List<Conversation> conversations) {
                view.updateConversationList(conversations);
                observeConversations();
            }

            @Override
            public void onError(@NotNull Throwable exception) {
                exception.printStackTrace();
                observeConversations();
            }
        }, null);
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
    public void destroy() {
        observeConversationsUseCase.dispose();
    }
}
