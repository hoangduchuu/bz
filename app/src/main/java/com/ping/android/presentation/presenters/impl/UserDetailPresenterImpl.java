package com.ping.android.presentation.presenters.impl;

import com.bzzzchat.cleanarchitecture.DefaultObserver;
import com.ping.android.domain.usecase.AddContactUseCase;
import com.ping.android.domain.usecase.DeleteFriendUseCase;
import com.ping.android.domain.usecase.GetUserByKeyUseCase;
import com.ping.android.domain.usecase.ObserveCurrentUserUseCase;
import com.ping.android.domain.usecase.ObserveSpecificFriendStatusUseCase;
import com.ping.android.domain.usecase.user.ToggleBlockUserUseCase;
import com.ping.android.domain.usecase.conversation.CreatePVPConversationUseCase;
import com.ping.android.model.User;
import com.ping.android.presentation.presenters.UserDetailPresenter;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

/**
 * Created by tuanluong on 3/14/18.
 */

public class UserDetailPresenterImpl implements UserDetailPresenter {
    @Inject
    UserDetailPresenter.View view;
    @Inject
    GetUserByKeyUseCase getUserByKeyUseCase;
    @Inject
    ObserveCurrentUserUseCase observeCurrentUserUseCase;
    @Inject
    ObserveSpecificFriendStatusUseCase observeFriendsStatusUseCase;
    @Inject
    CreatePVPConversationUseCase createPVPConversationUseCase;
    @Inject
    ToggleBlockUserUseCase toggleBlockUserUseCase;
    @Inject
    DeleteFriendUseCase deleteFriendUseCase;
    @Inject
    AddContactUseCase addContactUseCase;
    private User currentUser;
    private User otherUser;
    private String userID;

    @Inject
    public UserDetailPresenterImpl() {}

    @Override
    public void create() {
        observeCurrentUserUseCase.execute(new DefaultObserver<User>() {
            @Override
            public void onNext(User user) {
                currentUser = user;
                view.toggleBlockUser(user.blocks.containsKey(userID));
            }

            @Override
            public void onError(@NotNull Throwable exception) {
                super.onError(exception);
            }
        }, null);
    }

    @Override
    public void destroy() {
        observeCurrentUserUseCase.dispose();
        createPVPConversationUseCase.dispose();
        toggleBlockUserUseCase.dispose();
        deleteFriendUseCase.dispose();
        observeFriendsStatusUseCase.dispose();
        addContactUseCase.dispose();
    }

    @Override
    public void init(String otherUserId) {
        this.userID = otherUserId;
        observeCurrentUserUseCase.execute(new DefaultObserver<User>() {
            @Override
            public void onNext(User user) {
                currentUser = user;
                view.toggleBlockUser(user.blocks.containsKey(userID));
            }

            @Override
            public void onError(@NotNull Throwable exception) {
                super.onError(exception);
            }
        }, null);
        getUserByKeyUseCase.execute(new DefaultObserver<User>() {
            @Override
            public void onNext(User user) {
                otherUser = user;
                view.updateUI(otherUser);
            }
        }, otherUserId);
    }

    @Override
    public void sendMessageToUser() {
        CreatePVPConversationUseCase.Params params = new CreatePVPConversationUseCase.Params();
        params.message = "";
        params.toUser = otherUser;
        createPVPConversationUseCase.execute(new DefaultObserver<String>() {
            @Override
            public void onNext(String s) {
                view.openConversation(s);
            }
        }, params);
    }

    @Override
    public void observeFriendStatus(String friendId) {
        observeFriendsStatusUseCase.execute(new DefaultObserver<Boolean>() {
            @Override
            public void onNext(Boolean isFriend) {
                view.updateFriendStatus(isFriend);
            }

            @Override
            public void onError(@NotNull Throwable exception) {
                exception.printStackTrace();
            }
        }, friendId);
    }

    @Override
    public void toggleBlockUser(boolean checked) {
        view.showLoading();
        toggleBlockUserUseCase.execute(new DefaultObserver<Boolean>() {
            @Override
            public void onNext(Boolean aBoolean) {
                view.hideLoading();
            }

            @Override
            public void onError(@NotNull Throwable exception) {
                view.hideLoading();
            }
        }, new ToggleBlockUserUseCase.Params(otherUser.key, checked));
    }

    @Override
    public void deleteContact() {
        view.showLoading();
        deleteFriendUseCase.execute(new DefaultObserver<Boolean>() {
            @Override
            public void onNext(Boolean aBoolean) {
                view.hideLoading();
                //view.updateFriendStatus(aBoolean);
            }

            @Override
            public void onError(@NotNull Throwable exception) {
                view.hideLoading();
            }
        }, otherUser.key);
    }

    @Override
    public void addContact() {
        view.showLoading();
        addContactUseCase.execute(new DefaultObserver<Boolean>() {
            @Override
            public void onNext(Boolean aBoolean) {
                view.hideLoading();
            }

            @Override
            public void onError(@NotNull Throwable exception) {
                view.hideLoading();
            }
        }, otherUser.key);
    }

    @Override
    public void handleVoiceCallPress() {
        view.openCallScreen(currentUser, otherUser, false);
    }

    @Override
    public void handleVideoCallPress() {
        view.openCallScreen(currentUser, otherUser, true);
    }
}
