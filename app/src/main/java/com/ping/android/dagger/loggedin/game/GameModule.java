package com.ping.android.dagger.loggedin.game;

import com.ping.android.dagger.scopes.PerActivity;
import com.ping.android.presentation.presenters.AddContactPresenter;
import com.ping.android.presentation.presenters.GamePresenter;
import com.ping.android.presentation.presenters.impl.AddContactPresenterImpl;
import com.ping.android.presentation.presenters.impl.GamePresenterImpl;

import dagger.Module;
import dagger.Provides;

/**
 * Created by tuanluong on 3/21/18.
 */
@Module
public class GameModule {
    private final GamePresenter.View view;

    public GameModule(GamePresenter.View view) {
        this.view = view;
    }

    @Provides
    @PerActivity
    public GamePresenter.View provideView() {
        return view;
    }

    @Provides
    @PerActivity
    public GamePresenter providePresenter(GamePresenterImpl presenter) {
        return presenter;
    }
}
