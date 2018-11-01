package com.ping.android.dagger.loggedin.blockcontact;

import com.ping.android.dagger.scopes.PerActivity;
import com.ping.android.presentation.presenters.BlockContactPresenter;
import com.ping.android.presentation.presenters.impl.BlockContactPresenterImpl;
import com.ping.android.presentation.view.activity.BlockActivity;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;

/**
 * Created by tuanluong on 3/21/18.
 */
@Module
public abstract class BlockContactModule {
    @Binds
    public abstract BlockContactPresenter.View provideView(BlockActivity activity);

    @Provides
    @PerActivity
    public static BlockContactPresenter providePresenter(BlockContactPresenterImpl presenter) {
        return presenter;
    }
}
