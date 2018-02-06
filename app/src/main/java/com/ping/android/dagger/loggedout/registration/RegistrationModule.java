package com.ping.android.dagger.loggedout.registration;

import com.ping.android.dagger.scopes.PerActivity;
import com.ping.android.presentation.presenters.LoginPresenter;
import com.ping.android.presentation.presenters.RegistrationPresenter;
import com.ping.android.presentation.presenters.impl.LoginPresenterImpl;
import com.ping.android.presentation.presenters.impl.RegistrationPresenterImpl;

import dagger.Module;
import dagger.Provides;

/**
 * Created by tuanluong on 2/6/18.
 */
@Module
public class RegistrationModule {
    private final RegistrationPresenter.View view;

    public RegistrationModule(RegistrationPresenter.View view) {
        this.view = view;
    }

    @Provides
    @PerActivity
    public RegistrationPresenter.View provideView() {
        return view;
    }

    @Provides
    @PerActivity
    public RegistrationPresenter provideLoginPresenter(RegistrationPresenterImpl presenter) {
        return presenter;
    }
}
