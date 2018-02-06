package com.ping.android.dagger.loggedout.login;

import com.ping.android.dagger.scopes.PerActivity;
import com.ping.android.presentation.presenters.LoginPresenter;
import com.ping.android.presentation.presenters.impl.LoginPresenterImpl;

import dagger.Module;
import dagger.Provides;

/**
 * Created by tuanluong on 2/6/18.
 */
@Module
public class LoginModule {
    private final LoginPresenter.View view;

    public LoginModule(LoginPresenter.View view) {
        this.view = view;
    }

    @Provides
    @PerActivity
    public LoginPresenter.View provideView() {
        return view;
    }

    @Provides
    @PerActivity
    public LoginPresenter provideLoginPresenter(LoginPresenterImpl presenter) {
        return presenter;
    }
}
