package com.ping.android.presentation.presenters.impl;

import com.bzzzchat.cleanarchitecture.DefaultObserver;
import com.ping.android.domain.usecase.conversation.UpdateConversationNicknameUseCase;
import com.ping.android.model.Conversation;
import com.ping.android.model.Nickname;
import com.ping.android.presentation.presenters.NicknamePresenter;

import org.jetbrains.annotations.NotNull;

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
    public NicknamePresenterImpl() {
    }


    @Override
    public void init(Conversation conversation) {
        this.conversation = conversation;
    }

    @Override
    public void updateNickName(Nickname nickname) {
        view.showLoading();
        updateConversationNicknameUseCase
                .execute(new DefaultObserver<Boolean>() {
                             @Override
                             public void onNext(Boolean aBoolean) {
                                 conversation.nickNames.put(nickname.userId, nickname.nickName);
                                 view.hideLoading();
                                 view.updateNickname(nickname);
                             }

                             @Override
                             public void onError(@NotNull Throwable exception) {
                                 exception.printStackTrace();
                                 view.hideLoading();
                             }
                         },
                        new UpdateConversationNicknameUseCase.Params(conversation, nickname));
    }
}
