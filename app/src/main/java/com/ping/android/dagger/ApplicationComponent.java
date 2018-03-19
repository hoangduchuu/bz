package com.ping.android.dagger;

import com.ping.android.dagger.loggedin.LoggedInComponent;
import com.ping.android.dagger.loggedout.LoggedOutComponent;
import com.ping.android.service.CallService;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by tuanluong on 1/23/18.
 */
@Singleton
@Component(modules = {ApplicationModule.class, RepositoryModule.class})
public interface ApplicationComponent {
    LoggedInComponent provideLoggedInComponent();

    LoggedOutComponent provideLoggedOutComponent();

    void inject(CallService service);
}
