package com.ping.android.dagger.login;

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
    @Provides
    @PerActivity
    public LoginPresenter provideLoginPresenter(LoginPresenterImpl presenter) {
        return presenter;
    }
}
