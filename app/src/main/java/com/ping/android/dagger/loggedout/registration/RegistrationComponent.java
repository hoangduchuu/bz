package com.ping.android.dagger.loggedout.registration;

import com.ping.android.dagger.scopes.PerActivity;
import com.ping.android.presentation.view.activity.RegistrationActivity;

import dagger.Subcomponent;

/**
 * Created by tuanluong on 2/6/18.
 */
@PerActivity
@Subcomponent(modules = {RegistrationModule.class})
public interface RegistrationComponent {
    void inject(RegistrationActivity activity);
}
