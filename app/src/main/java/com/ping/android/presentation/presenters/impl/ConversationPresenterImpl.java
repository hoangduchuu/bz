package com.ping.android.presentation.presenters.impl;

import com.bzzzchat.cleanarchitecture.DefaultObserver;
import com.bzzzchat.rxfirebase.events.ChildEvent;
import com.ping.android.domain.usecase.ObserveConversationUseCase;
import com.ping.android.model.ChildData;
import com.ping.android.model.Conversation;
import com.ping.android.presentation.presenters.ConversationPresenter;

import javax.inject.Inject;

/**
 * Created by tuanluong on 1/28/18.
 */

public class ConversationPresenterImpl implements ConversationPresenter {
    @Inject
    ObserveConversationUseCase observeConversationUseCase;
    @Inject
    ConversationPresenter.View view;

    @Inject
    public ConversationPresenterImpl() {}

    @Override
    public void getConversations() {
        observeConversationUseCase.execute(new DefaultObserver<ChildData<Conversation>>() {
            @Override
            public void onNext(ChildData<Conversation> childData) {
                switch (childData.type) {
                    case CHILD_ADDED:
                        view.addConversation(childData.data);
                        break;
                    case CHILD_CHANGED:
                        view.addConversation(childData.data);
                        break;
                    case CHILD_REMOVED:
                        view.deleteConversation(childData.data);
                        break;
                }
            }
        }, null);
    }

    @Override
    public void destroy() {
        observeConversationUseCase.dispose();
    }
}
