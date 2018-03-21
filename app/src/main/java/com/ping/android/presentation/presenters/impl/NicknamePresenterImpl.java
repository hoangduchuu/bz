package com.ping.android.presentation.presenters.impl;

import com.bzzzchat.cleanarchitecture.DefaultObserver;
import com.ping.android.domain.usecase.conversation.UpdateConversationNicknameUseCase;
import com.ping.android.model.Conversation;
import com.ping.android.model.Nickname;
import com.ping.android.presentation.presenters.NicknamePresenter;

import javax.inject.Inject;

/**
 * Created by tuanluong on 3/21/18.
 */

public class NicknamePresenterImpl implements NicknamePresenter {
    @Inject
    View view;
    @Inject
    UpdateConversationNicknameUseCase updateConversationNicknameUseCase;

    private Conversation conversation;

    @Inject
    public NicknamePresenterImpl() {}


    @Override
    public void init(Conversation conversation) {
        this.conversation = conversation;
    }

    @Override
    public void updateNickName(Nickname nickname) {
        updateConversationNicknameUseCase.execute(new DefaultObserver<>(), new UpdateConversationNicknameUseCase.Params(conversation, nickname));
    }
}
