package com.ping.android.dagger;

import com.ping.android.dagger.loggedin.LoggedInComponent;
import com.ping.android.dagger.loggedin.RepositoryModule;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by tuanluong on 1/23/18.
 */
@Singleton
@Component(modules = {ApplicationModule.class})
public interface ApplicationComponent {
    LoggedInComponent provideLoggedInComponent(RepositoryModule module);
}
