package com.ping.android.dagger.loggedout.splash;

import com.ping.android.dagger.scopes.PerActivity;
import com.ping.android.presentation.presenters.SplashPresenter;
import com.ping.android.presentation.presenters.impl.SplashPresenterImpl;
import com.ping.android.presentation.view.activity.SplashActivity;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;

/**
 * Created by tuanluong on 2/6/18.
 */
@Module
public abstract class SplashModule {

    @Binds
    public abstract SplashPresenter.View view(SplashActivity activity);

    @Provides
    @PerActivity
    static SplashPresenter providePresenter(SplashPresenterImpl presenter) {
        return presenter;
    }
}
