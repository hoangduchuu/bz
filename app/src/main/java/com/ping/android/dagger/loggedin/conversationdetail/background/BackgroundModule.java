package com.ping.android.dagger.loggedin.conversationdetail.background;

import com.ping.android.dagger.scopes.PerFragment;
import com.ping.android.presentation.presenters.BackgroundPresenter;
import com.ping.android.presentation.presenters.BackgroundPresenterImpl;

import javax.inject.Inject;

import dagger.Module;
import dagger.Provides;

@Module
public class BackgroundModule {
    private BackgroundPresenter.View view;

    public BackgroundModule(BackgroundPresenter.View view) {
        this.view = view;
    }

    @Provides
    @PerFragment
    public BackgroundPresenter.View provideView() {
        return view;
    }

    @Provides
    @PerFragment
    public BackgroundPresenter providePresenter(BackgroundPresenterImpl presenter) {
        return presenter;
    }
}
