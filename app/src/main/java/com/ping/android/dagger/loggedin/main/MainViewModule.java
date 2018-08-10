package com.ping.android.dagger.loggedin.main;

import com.ping.android.presentation.presenters.MainPresenter;
import com.ping.android.presentation.view.activity.MainActivity;

import dagger.Binds;
import dagger.Module;

@Module
public abstract class MainViewModule {
    @Binds
    abstract MainPresenter.View provideMainView(MainActivity activity);
}
