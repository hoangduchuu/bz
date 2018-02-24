package com.ping.android.presentation.presenters.impl;

import com.bzzzchat.cleanarchitecture.DefaultObserver;
import com.ping.android.domain.usecase.conversation.CreatePVPConversationUseCase;
import com.ping.android.domain.usecase.group.CreateGroupUseCase;
import com.ping.android.model.User;
import com.ping.android.presentation.presenters.NewChatPresenter;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

/**
 * Created by tuanluong on 1/22/18.
 */

public class NewChatPresenterImpl implements NewChatPresenter {
    @Inject
    public NewChatView view;
    @Inject
    CreateGroupUseCase createGroupUseCase;
    @Inject
    CreatePVPConversationUseCase createPVPConversationUseCase;

    @Inject
    public NewChatPresenterImpl() {
    }

    @Override
    public void createGroup(CreateGroupUseCase.Params params) {
        view.showLoading();
        createGroupUseCase.execute(new DefaultObserver<String>() {
            @Override
            public void onNext(String conversationId) {
                view.hideLoading();
                view.moveToChatScreen(conversationId);
            }

            @Override
            public void onError(@NotNull Throwable exception) {
                exception.printStackTrace();
                view.hideLoading();
            }
        }, params);
    }

    @Override
    public void createPVPConversation(CreatePVPConversationUseCase.Params params) {
        view.showLoading();
        createPVPConversationUseCase.execute(new DefaultObserver<String>() {
            @Override
            public void onNext(String s) {
                view.hideLoading();
                view.moveToChatScreen(s);
            }

            @Override
            public void onError(@NotNull Throwable exception) {
                exception.printStackTrace();
                view.hideLoading();
            }
        }, params);
    }

    @Override
    public void destroy() {
        createPVPConversationUseCase.dispose();
    }
}
