package com.ping.android.presentation.presenters.impl;

import android.text.TextUtils;

import com.bzzzchat.cleanarchitecture.DefaultObserver;
import com.ping.android.domain.usecase.GetCurrentUserUseCase;
import com.ping.android.domain.usecase.conversation.CreatePVPConversationUseCase;
import com.ping.android.domain.usecase.group.CreateGroupUseCase;
import com.ping.android.model.User;
import com.ping.android.presentation.presenters.NewChatPresenter;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by tuanluong on 1/22/18.
 */

public class NewChatPresenterImpl implements NewChatPresenter {
    @Inject
    public NewChatView view;
    @Inject
    GetCurrentUserUseCase getCurrentUserUseCase;
    @Inject
    CreateGroupUseCase createGroupUseCase;
    @Inject
    CreatePVPConversationUseCase createPVPConversationUseCase;
    private User currentUser;

    @Inject
    public NewChatPresenterImpl() {
    }

    @Override
    public void create() {
        getCurrentUserUseCase.execute(new DefaultObserver<User>() {
            @Override
            public void onNext(User user) {
                currentUser = user;
            }
        }, null);
    }

    @Override
    public void createGroup(List<User> toUsers, String message) {
        toUsers.add(currentUser);
        List<String> displayNames = new ArrayList<>();
        for (User user : toUsers) {
            displayNames.add(user.getDisplayName());
        }
        CreateGroupUseCase.Params params = new CreateGroupUseCase.Params();
        params.users = toUsers;
        params.groupName = TextUtils.join(", ", displayNames);
        params.groupProfileImage = "";
        params.message = message;
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
        view = null;
        getCurrentUserUseCase.dispose();
        createGroupUseCase.dispose();
        createPVPConversationUseCase.dispose();
    }
}
