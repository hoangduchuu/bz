package com.ping.android.dagger.loggedout.login;

import com.ping.android.presentation.presenters.LoginPresenter;
import com.ping.android.presentation.view.activity.LoginActivity;

import dagger.Binds;
import dagger.Module;

@Module
public abstract class LoginViewModule {
    @Binds
    abstract LoginPresenter.View provideView(LoginActivity activity);
}
