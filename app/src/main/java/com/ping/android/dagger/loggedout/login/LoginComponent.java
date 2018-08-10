package com.ping.android.dagger.loggedout.login;

import com.ping.android.dagger.scopes.PerActivity;

import dagger.Subcomponent;

/**
 * Created by tuanluong on 2/6/18.
 */
@PerActivity
@Subcomponent(modules = {LoginModule.class})
public interface LoginComponent {
    //void inject(LoginActivity activity);
}
