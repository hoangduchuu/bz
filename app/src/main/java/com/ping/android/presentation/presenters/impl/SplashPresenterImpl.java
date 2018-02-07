package com.ping.android.presentation.presenters.impl;

import com.bzzzchat.cleanarchitecture.DefaultObserver;
import com.ping.android.domain.usecase.InitializeUserUseCase;
import com.ping.android.presentation.presenters.SplashPresenter;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

/**
 * Created by tuanluong on 2/6/18.
 */

public class SplashPresenterImpl implements SplashPresenter {
    @Inject
    View view;
    @Inject
    InitializeUserUseCase initializeUserUseCase;
    private boolean isLoggedIn = false;

    @Inject
    public SplashPresenterImpl() {}

    @Override
    public void initializeUser() {
        initializeUserUseCase.execute(new DefaultObserver<Boolean>() {
            @Override
            public void onNext(Boolean aBoolean) {
                isLoggedIn = aBoolean;
                if (isLoggedIn) {
                    view.navigateToMainScreen();
                } else {
                    view.navigateToLoginScreen();
                }
            }

            @Override
            public void onError(@NotNull Throwable exception) {
                exception.printStackTrace();
                view.navigateToLoginScreen();
            }
        }, null);
    }

    @Override
    public void finishTimer() {
        if (isLoggedIn) {
            view.navigateToMainScreen();
        } else {
            view.navigateToLoginScreen();
        }
    }

    @Override
    public void destroy() {
        initializeUserUseCase.dispose();
    }
}
