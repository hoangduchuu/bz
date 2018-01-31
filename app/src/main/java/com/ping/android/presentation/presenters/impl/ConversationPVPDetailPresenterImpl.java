package com.ping.android.presentation.presenters.impl;

import com.bzzzchat.cleanarchitecture.DefaultObserver;
import com.ping.android.domain.usecase.GetConversationUseCase;
import com.ping.android.model.Conversation;
import com.ping.android.presentation.presenters.ConversationPVPDetailPresenter;

import javax.inject.Inject;

/**
 * Created by tuanluong on 1/31/18.
 */

public class ConversationPVPDetailPresenterImpl implements ConversationPVPDetailPresenter {
    @Inject
    GetConversationUseCase getConversationUseCase;
    @Inject
    ConversationPVPDetailPresenter.View view;

    @Inject
    public ConversationPVPDetailPresenterImpl() {}

    @Override
    public void initConversation(String conversationId) {
        getConversationUseCase.execute(new DefaultObserver<Conversation>() {
            @Override
            public void onNext(Conversation data) {
                view.updateConversation(data);
            }
        }, conversationId);
    }
}
