package com.ping.android.dagger.loggedout.splash;

import com.ping.android.dagger.scopes.PerActivity;
import com.ping.android.presentation.presenters.SplashPresenter;
import com.ping.android.presentation.presenters.impl.SplashPresenterImpl;

import dagger.Module;
import dagger.Provides;

/**
 * Created by tuanluong on 2/6/18.
 */
@Module
public class SplashModule {
    private final SplashPresenter.View view;

    public SplashModule(SplashPresenter.View view) {
        this.view = view;
    }

    @Provides
    @PerActivity
    public SplashPresenter.View provideView() {
        return view;
    }

    @Provides
    @PerActivity
    public SplashPresenter providePresenter(SplashPresenterImpl presenter) {
        return presenter;
    }
}
