package com.ping.android.presentation.presenters.impl;

import com.bzzzchat.cleanarchitecture.DefaultObserver;
import com.ping.android.domain.usecase.group.CreateGroupUseCase;
import com.ping.android.domain.usecase.group.UploadGroupProfileImageUseCase;
import com.ping.android.presentation.presenters.AddGroupPresenter;

import org.jetbrains.annotations.NotNull;

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
    AddGroupPresenter.View view;

    @Inject
    public AddGroupPresenterImpl() {
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
}
