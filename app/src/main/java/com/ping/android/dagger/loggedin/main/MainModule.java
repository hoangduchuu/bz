package com.ping.android.dagger.loggedin.main;

import com.ping.android.dagger.scopes.PerActivity;
import com.ping.android.presentation.presenters.MainPresenter;
import com.ping.android.presentation.presenters.impl.MainPresenterImpl;

import dagger.Module;
import dagger.Provides;

/**
 * Created by tuanluong on 1/28/18.
 */
@Module
public class MainModule {
    @Provides
    @PerActivity
    public MainPresenter provideMainPresenter(MainPresenterImpl presenter) {
        return presenter;
    }

}
