package com.ping.android.presentation.presenters.impl;

import android.text.TextUtils;

import com.bzzzchat.cleanarchitecture.DefaultObserver;
import com.ping.android.domain.usecase.ObserveCurrentUserUseCase;
import com.ping.android.domain.usecase.ObserveFriendsStatusUseCase;
import com.ping.android.model.User;
import com.ping.android.presentation.presenters.MainPresenter;

import javax.inject.Inject;

/**
 * Created by tuanluong on 2/10/18.
 */

public class MainPresenterImpl implements MainPresenter {
    @Inject
    View view;
    @Inject
    ObserveCurrentUserUseCase observeCurrentUserUseCase;
    @Inject
    ObserveFriendsStatusUseCase observeFriendsStatusUseCase;
    private User currentUser;
    private boolean isInit = false;

    @Inject
    public MainPresenterImpl() {

    }

    @Override
    public void create() {
        observeCurrentUserUseCase.execute(new DefaultObserver<User>() {
            @Override
            public void onNext(User user) {
                handleUserUpdate(user);
            }
        }, null);
        observeFriendsStatusUseCase.execute(new DefaultObserver<>(), null);
    }

    private void handleUserUpdate(User user) {
        this.currentUser = user;
        if (!isInit) {
            if (TextUtils.isEmpty(currentUser.phone)) {
                view.openPhoneRequireView();
            }
            if (!currentUser.showMappingConfirm) {
                view.showMappingConfirm();
            }
            isInit = true;
        }
    }

    @Override
    public void destroy() {
        observeCurrentUserUseCase.dispose();
        observeFriendsStatusUseCase.dispose();
    }
}
