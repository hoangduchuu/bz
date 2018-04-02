package com.ping.android.presentation.presenters.impl;

import android.text.TextUtils;

import com.bzzzchat.cleanarchitecture.DefaultObserver;
import com.ping.android.domain.repository.QuickbloxRepository;
import com.ping.android.domain.usecase.LoginQuickBloxUseCase;
import com.ping.android.domain.usecase.ObserveCurrentUserUseCase;
import com.ping.android.domain.usecase.ObserveFriendsStatusUseCase;
import com.ping.android.domain.usecase.RemoveUserBadgeUseCase;
import com.ping.android.managers.UserManager;
import com.ping.android.model.User;
import com.ping.android.presentation.presenters.MainPresenter;

import org.jetbrains.annotations.NotNull;

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
    @Inject
    RemoveUserBadgeUseCase removeUserBadgeUseCase;
    @Inject
    LoginQuickBloxUseCase loginQuickBloxUseCase;
    private User currentUser;
    private boolean isInit = false;

    @Inject
    public MainPresenterImpl() {

    }

    @Override
    public void create() {
        loginQuickBlox();
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

    @Override
    public void removeMissedCallsBadge() {
        removeUserBadgeUseCase.execute(new DefaultObserver<>(), "missed_call");
    }

    @Override
    public void onNetworkAvailable() {
        if (this.currentUser != null) {
            loginQuickBlox();
        }
    }

    private void loginQuickBlox() {
        loginQuickBloxUseCase.execute(new DefaultObserver<Boolean>() {
            @Override
            public void onNext(Boolean aBoolean) {
                view.startCallService();
            }

            @Override
            public void onError(@NotNull Throwable exception) {
                exception.printStackTrace();
            }
        }, null);
    }
}
