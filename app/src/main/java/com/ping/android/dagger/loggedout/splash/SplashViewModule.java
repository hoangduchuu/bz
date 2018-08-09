package com.ping.android.dagger.loggedout.splash;

import com.ping.android.presentation.presenters.SplashPresenter;
import com.ping.android.presentation.view.activity.SplashActivity;

import dagger.Binds;
import dagger.Module;

@Module
public abstract class SplashViewModule {
    @Binds
    abstract SplashPresenter.View provideSplashView(SplashActivity activity);
}
