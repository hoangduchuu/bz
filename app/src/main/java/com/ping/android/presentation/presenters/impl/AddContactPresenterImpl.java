package com.ping.android.presentation.presenters.impl;

import com.bzzzchat.cleanarchitecture.DefaultObserver;
import com.ping.android.domain.usecase.AddContactUseCase;
import com.ping.android.domain.usecase.conversation.CreatePVPConversationUseCase;
import com.ping.android.model.User;
import com.ping.android.presentation.presenters.AddContactPresenter;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

/**
 * Created by tuanluong on 3/21/18.
 */

public class AddContactPresenterImpl implements AddContactPresenter {
    @Inject
    View view;
    @Inject
    CreatePVPConversationUseCase createPVPConversationUseCase;
    @Inject
    AddContactUseCase addContactUseCase;

    @Inject
    public AddContactPresenterImpl() {

    }

    @Override
    public void createPVPConversation(User otherUser) {
        CreatePVPConversationUseCase.Params params = new CreatePVPConversationUseCase.Params();
        params.message = "";
        params.toUser = otherUser;
        view.showLoading();
        createPVPConversationUseCase.execute(new DefaultObserver<String>() {
            @Override
            public void onNext(String s) {
                view.hideLoading();
                view.moveToChatScreen(s);
            }

            @Override
            public void onError(@NotNull Throwable exception) {
                view.hideLoading();
            }
        }, params);
    }

    @Override
    public void addContact(String userId) {
        addContactUseCase.execute(new DefaultObserver<Boolean>(), userId);
    }

    @Override
    public void destroy() {
        createPVPConversationUseCase.dispose();
        addContactUseCase.dispose();
    }
}
