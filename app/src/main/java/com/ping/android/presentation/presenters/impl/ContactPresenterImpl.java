package com.ping.android.presentation.presenters.impl;

import com.bzzzchat.cleanarchitecture.DefaultObserver;
import com.ping.android.domain.usecase.ObserveFriendsChildEventUseCase;
import com.ping.android.domain.usecase.ObserveFriendsStatusUseCase;
import com.ping.android.domain.usecase.conversation.CreatePVPConversationUseCase;
import com.ping.android.model.ChildData;
import com.ping.android.model.User;
import com.ping.android.presentation.presenters.ContactPresenter;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

/**
 * Created by tuanluong on 3/14/18.
 */

public class ContactPresenterImpl implements ContactPresenter {
    @Inject
    View view;
    @Inject
    ObserveFriendsChildEventUseCase observeFriendsChildEventUseCase;
    @Inject
    CreatePVPConversationUseCase createPVPConversationUseCase;

    @Inject
    public ContactPresenterImpl() {
    }

    @Override
    public void create() {
        observeFriendsChildEventUseCase.execute(new DefaultObserver<ChildData<User>>() {
            @Override
            public void onNext(ChildData<User> userChildData) {
                switch (userChildData.type) {
                    case CHILD_ADDED:
                        view.addFriend(userChildData.data);
                        break;
                    default:
                        view.removeFriend(userChildData.data.key);
                        break;
                }
            }

            @Override
            public void onError(@NotNull Throwable exception) {
                exception.printStackTrace();
            }
        }, null);
    }

    @Override
    public void destroy() {
        observeFriendsChildEventUseCase.dispose();
        createPVPConversationUseCase.dispose();
    }

    @Override
    public void handleSendMessage(User user) {
        CreatePVPConversationUseCase.Params params = new CreatePVPConversationUseCase.Params();
        params.toUser = user;
        params.message = "";
        createPVPConversationUseCase.execute(new DefaultObserver<String>() {
            @Override
            public void onNext(String s) {
                view.openConversation(s);
            }
        }, params);
    }
}
