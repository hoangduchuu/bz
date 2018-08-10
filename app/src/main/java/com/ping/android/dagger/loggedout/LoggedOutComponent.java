package com.ping.android.dagger.loggedout;

import com.ping.android.dagger.loggedout.login.LoginComponent;
import com.ping.android.dagger.loggedout.login.LoginModule;
import com.ping.android.dagger.loggedout.registration.RegistrationComponent;
import com.ping.android.dagger.loggedout.registration.RegistrationModule;
import com.ping.android.dagger.scopes.LoggedOut;

import dagger.Subcomponent;

/**
 * Created by tuanluong on 1/23/18.
 */
@LoggedOut
@Subcomponent
public interface LoggedOutComponent {

    LoginComponent provideLoginComponent(LoginModule loginModule);
    RegistrationComponent provideRegistrationComponent(RegistrationModule module);
}
