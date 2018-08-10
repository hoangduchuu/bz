package com.ping.android.dagger.loggedin.conversationdetail.background;

import com.ping.android.dagger.scopes.PerFragment;
import com.ping.android.presentation.presenters.BackgroundPresenter;
import com.ping.android.presentation.presenters.BackgroundPresenterImpl;
import com.ping.android.presentation.view.fragment.BackgroundFragment;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;

@Module
public abstract class BackgroundModule {

    @Binds
    public abstract BackgroundPresenter.View provideView(BackgroundFragment fragment);

    @Provides
    @PerFragment
    public static BackgroundPresenter providePresenter(BackgroundPresenterImpl presenter) {
        return presenter;
    }
}
