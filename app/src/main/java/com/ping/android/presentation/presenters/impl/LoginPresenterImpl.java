package com.ping.android.presentation.presenters.impl;

import com.bzzzchat.cleanarchitecture.DefaultObserver;
import com.google.firebase.FirebaseNetworkException;
import com.ping.android.domain.usecase.InitializeUserUseCase;
import com.ping.android.domain.usecase.auth.AuthenticateUseCase;
import com.ping.android.model.User;
import com.ping.android.presentation.presenters.LoginPresenter;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeoutException;

import javax.inject.Inject;

/**
 * Created by tuanluong on 2/6/18.
 */

public class LoginPresenterImpl implements LoginPresenter {
    @Inject
    View view;
    @Inject
    InitializeUserUseCase initializeUserUseCase;
    @Inject
    AuthenticateUseCase authenticateUseCase;

    @Inject
    public LoginPresenterImpl() {

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
    public void login(String name, String password) {
        view.showLoading();
        AuthenticateUseCase.Params params = new AuthenticateUseCase.Params(name, password);
        authenticateUseCase.execute(new DefaultObserver<User>() {
            @Override
            public void onNext(User user) {
                view.hideLoading();
                view.navigateToMainScreen();
            }

            @Override
            public void onError(@NotNull Throwable exception) {
                exception.printStackTrace();
                if (exception instanceof TimeoutException || exception instanceof FirebaseNetworkException){
                    view.showTimeOutNotification();
                    view.hideLoading();
                }else {
                    view.showMessageLoginFailed();
                    view.hideLoading();
                }
            }
        }, params);
    }

    @Override
    public void destroy() {
        view = null;
        initializeUserUseCase.dispose();
        authenticateUseCase.dispose();
    }
}
