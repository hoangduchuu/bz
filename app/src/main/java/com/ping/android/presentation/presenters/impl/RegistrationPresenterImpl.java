package com.ping.android.presentation.presenters.impl;

import com.bzzzchat.cleanarchitecture.DefaultObserver;
import com.ping.android.domain.usecase.InitializeUserUseCase;
import com.ping.android.presentation.presenters.RegistrationPresenter;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

/**
 * Created by tuanluong on 2/6/18.
 */

public class RegistrationPresenterImpl implements RegistrationPresenter {
    @Inject
    View view;
    @Inject
    InitializeUserUseCase initializeUserUseCase;

    @Inject
    public RegistrationPresenterImpl() {

    }

    @Override
    public void initializeUser() {
        initializeUserUseCase.execute(new DefaultObserver<Boolean>() {
            @Override
            public void onNext(Boolean aBoolean) {
                view.hideLoading();
                view.navigateToMainScreen();
            }

            @Override
            public void onError(@NotNull Throwable exception) {
                view.hideLoading();
            }
        }, null);
    }

    @Override
    public void destroy() {
        initializeUserUseCase.dispose();
    }
}
