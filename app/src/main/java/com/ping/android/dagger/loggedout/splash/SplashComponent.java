package com.ping.android.dagger.loggedout.splash;

import com.ping.android.dagger.scopes.PerActivity;
import com.ping.android.presentation.view.activity.SplashActivity;

import dagger.Subcomponent;

/**
 * Created by tuanluong on 2/6/18.
 */
@PerActivity
@Subcomponent(modules = { SplashModule.class })
public interface SplashComponent {
    void inject(SplashActivity activity);
}
