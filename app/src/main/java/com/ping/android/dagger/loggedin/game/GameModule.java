package com.ping.android.dagger.loggedin.game;

import com.ping.android.dagger.scopes.PerActivity;
import com.ping.android.presentation.presenters.GamePresenter;
import com.ping.android.presentation.presenters.impl.GamePresenterImpl;

import dagger.Module;
import dagger.Provides;

/**
 * Created by tuanluong on 3/21/18.
 */
@Module
public class GameModule {
    @Provides
    @PerActivity
    public GamePresenter providePresenter(GamePresenterImpl presenter) {
        return presenter;
    }
}
