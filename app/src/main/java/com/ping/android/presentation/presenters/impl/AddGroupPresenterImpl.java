package com.ping.android.presentation.presenters.impl;

import com.bzzzchat.cleanarchitecture.DefaultObserver;
import com.ping.android.domain.usecase.GetCurrentUserUseCase;
import com.ping.android.domain.usecase.group.CreateGroupUseCase;
import com.ping.android.domain.usecase.group.UploadGroupProfileImageUseCase;
import com.ping.android.model.User;
import com.ping.android.presentation.presenters.AddGroupPresenter;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import javax.inject.Inject;

/**
 * Created by tuanluong on 2/8/18.
 */

public class AddGroupPresenterImpl implements AddGroupPresenter {
    @Inject
    UploadGroupProfileImageUseCase uploadGroupProfileImageUseCase;
    @Inject
    CreateGroupUseCase createGroupUseCase;
    @Inject
    GetCurrentUserUseCase getCurrentUserUseCase;
    @Inject
    AddGroupPresenter.View view;
    private User currentUser;

    @Inject
    public AddGroupPresenterImpl() {
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
    public void createGroup(List<User> toUsers, String groupNames, String groupProfileImage, String s) {
        CreateGroupUseCase.Params params = new CreateGroupUseCase.Params();
        params.users = toUsers;
        params.groupName = groupNames;
        params.groupProfileImage = groupProfileImage;
        params.message = s;
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
    public void handlePickerPress() {
        view.initProfileImagePath(currentUser.key);
        view.openPicker();
    }

    @Override
    public void destroy() {
        view = null;
        uploadGroupProfileImageUseCase.dispose();
        createGroupUseCase.dispose();
        getCurrentUserUseCase.dispose();
    }
}
