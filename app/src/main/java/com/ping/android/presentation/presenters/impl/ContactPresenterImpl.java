package com.ping.android.presentation.presenters.impl;

import com.bzzzchat.cleanarchitecture.DefaultObserver;
import com.ping.android.domain.usecase.ObserveCurrentUserUseCase;
import com.ping.android.domain.usecase.ObserveFriendsChildEventUseCase;
import com.ping.android.domain.usecase.conversation.CreatePVPConversationUseCase;
import com.ping.android.data.entity.ChildData;
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
    ObserveCurrentUserUseCase observeCurrentUserUseCase;
    private User currentUser;

    @Inject
    public ContactPresenterImpl() {
    }

    @Override
    public void create() {
        observeFriendsChildEventUseCase.execute(new DefaultObserver<ChildData<User>>() {
            @Override
            public void onNext(ChildData<User> userChildData) {
                switch (userChildData.getType()) {
                    case CHILD_ADDED:
                        view.addFriend(userChildData.getData());
                        break;
                    default:
                        view.removeFriend(userChildData.getData().key);
                        break;
                }
            }

            @Override
            public void onError(@NotNull Throwable exception) {
                exception.printStackTrace();
            }
        }, null);

        observeCurrentUserUseCase.execute(new DefaultObserver<User>() {
            @Override
            public void onNext(User user) {
                currentUser = user;
            }
        }, null);
    }

    @Override
    public void destroy() {
        observeFriendsChildEventUseCase.dispose();
        createPVPConversationUseCase.dispose();
        observeCurrentUserUseCase.dispose();
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

    @Override
    public void handleVoiceCallPress(User contact) {
        view.openCallScreen(currentUser, contact, false);
    }

    @Override
    public void handleVideoCallPress(User contact) {
        view.openCallScreen(currentUser, contact, true);
    }
}
