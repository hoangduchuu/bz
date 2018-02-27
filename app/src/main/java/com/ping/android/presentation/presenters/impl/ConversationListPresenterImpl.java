package com.ping.android.presentation.presenters.impl;

import com.bzzzchat.cleanarchitecture.DefaultObserver;
import com.bzzzchat.rxfirebase.database.ChildEvent;
import com.ping.android.domain.usecase.ObserveConversationsUseCase;
import com.ping.android.domain.usecase.ObserveGroupsUseCase;
import com.ping.android.model.ChildData;
import com.ping.android.model.Conversation;
import com.ping.android.model.Group;
import com.ping.android.presentation.presenters.ConversationListPresenter;

import java.util.List;

import javax.inject.Inject;

/**
 * Created by tuanluong on 1/28/18.
 */

public class ConversationListPresenterImpl implements ConversationListPresenter {
    @Inject
    ObserveConversationsUseCase observeConversationsUseCase;
    @Inject
    ObserveGroupsUseCase observeGroupsUseCase;

    @Inject
    ConversationListPresenter.View view;

    @Inject
    public ConversationListPresenterImpl() {}

    @Override
    public void getConversations() {
        observeConversationsUseCase.execute(new DefaultObserver<ChildData<Conversation>>() {
            @Override
            public void onNext(ChildData<Conversation> childData) {
                switch (childData.type) {
                    case CHILD_ADDED:
                        view.addConversation(childData.data);
                        break;
                    case CHILD_CHANGED:
                        view.updateConversation(childData.data);
                        break;
                    case CHILD_REMOVED:
                        view.deleteConversation(childData.data);
                        break;
                }
            }
        }, null);
        observeGroupsUseCase.execute(new DefaultObserver<ChildData<Group>>() {
            @Override
            public void onNext(ChildData<Group> groupChildData) {
                if (groupChildData != null) {
                    if (groupChildData.type == ChildEvent.Type.CHILD_CHANGED) {
                        view.updateGroupConversation(groupChildData.data);
                    }
                }
            }
        }, new ObserveGroupsUseCase.Params(false));
    }

    @Override
    public void deleteConversations(List<Conversation> conversations) {

    }

    @Override
    public void destroy() {
        observeConversationsUseCase.dispose();
        observeGroupsUseCase.dispose();
    }
}
