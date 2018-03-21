package com.ping.android.presentation.presenters.impl;

import com.bzzzchat.cleanarchitecture.DefaultObserver;
import com.ping.android.domain.usecase.LogoutUseCase;
import com.ping.android.domain.usecase.ObserveCurrentUserUseCase;
import com.ping.android.model.User;
import com.ping.android.presentation.presenters.ProfilePresenter;

import javax.inject.Inject;

/**
 * Created by tuanluong on 2/8/18.
 */

public class ProfilePresenterImpl implements ProfilePresenter {
    @Inject
    ProfilePresenter.View view;
    @Inject
    LogoutUseCase logoutUseCase;
    @Inject
    ObserveCurrentUserUseCase observeCurrentUserUseCase;

    @Inject
    public ProfilePresenterImpl() {}

    @Override
    public void create() {
        observeCurrentUserUseCase.execute(new DefaultObserver<User>() {
            @Override
            public void onNext(User user) {
                view.updateUser(user);
            }
        }, null);
    }

    @Override
    public void logout() {
        logoutUseCase.execute(new DefaultObserver<>(), null);
    }

    @Override
    public void destroy() {
        logoutUseCase.dispose();
        observeCurrentUserUseCase.dispose();
    }
}
