package com.ping.android.dagger.loggedin.main;

import com.ping.android.dagger.scopes.PerActivity;
import com.ping.android.presentation.presenters.MainPresenter;
import com.ping.android.presentation.presenters.impl.MainPresenterImpl;
import com.ping.android.presentation.view.activity.MainActivity;

import dagger.Module;
import dagger.Provides;

/**
 * Created by tuanluong on 1/28/18.
 */
@Module
public class MainModule {
    private final MainPresenter.View view;

    public MainModule(MainPresenter.View view) {
        this.view = view;
    }

    @Provides
    @PerActivity
    public MainPresenter.View provideView() {
        return view;
    }

    @Provides
    @PerActivity
    public MainPresenter provideMainPresenter(MainPresenterImpl presenter) {
        return presenter;
    }
}
