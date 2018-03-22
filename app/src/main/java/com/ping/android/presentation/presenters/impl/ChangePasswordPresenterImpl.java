package com.ping.android.presentation.presenters.impl;

import com.bzzzchat.cleanarchitecture.DefaultObserver;
import com.ping.android.domain.usecase.ObserveCurrentUserUseCase;
import com.ping.android.model.User;
import com.ping.android.presentation.presenters.ChangePasswordPresenter;

import javax.inject.Inject;

/**
 * Created by tuanluong on 3/22/18.
 */

public class ChangePasswordPresenterImpl implements ChangePasswordPresenter {
    @Inject
    View view;
    @Inject
    ObserveCurrentUserUseCase observeCurrentUserUseCase;

    @Inject
    public ChangePasswordPresenterImpl() {}

    @Override
    public void create() {
        observeCurrentUserUseCase.execute(new DefaultObserver<User>() {
            @Override
            public void onNext(User user) {
                view.onUserUpdated(user);
            }
        }, null);
    }

    @Override
    public void destroy() {
        observeCurrentUserUseCase.dispose();
    }
}
