package com.ping.android.presentation.presenters.impl;

import com.bzzzchat.cleanarchitecture.DefaultObserver;
import com.ping.android.domain.usecase.LogoutUseCase;
import com.ping.android.presentation.presenters.ProfilePresenter;

import javax.inject.Inject;

/**
 * Created by tuanluong on 2/8/18.
 */

public class ProfilePresenterImpl implements ProfilePresenter {
    @Inject
    LogoutUseCase logoutUseCase;

    @Inject
    public ProfilePresenterImpl() {}

    @Override
    public void logout() {
        logoutUseCase.execute(new DefaultObserver<>(), null);
    }

    @Override
    public void destroy() {
        logoutUseCase.dispose();
    }
}
