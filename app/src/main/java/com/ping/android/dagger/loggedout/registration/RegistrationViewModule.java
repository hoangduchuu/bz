package com.ping.android.dagger.loggedout.registration;

import com.ping.android.presentation.presenters.RegistrationPresenter;
import com.ping.android.presentation.view.activity.RegistrationActivity;

import dagger.Binds;
import dagger.Module;

@Module
public abstract class RegistrationViewModule {
    @Binds
    abstract RegistrationPresenter.View provideView(RegistrationActivity activity);
}
