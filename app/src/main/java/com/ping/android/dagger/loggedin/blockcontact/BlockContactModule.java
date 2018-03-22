package com.ping.android.dagger.loggedin.blockcontact;

import com.ping.android.dagger.scopes.PerActivity;
import com.ping.android.presentation.presenters.BlockContactPresenter;
import com.ping.android.presentation.presenters.impl.BlockContactPresenterImpl;

import dagger.Module;
import dagger.Provides;

/**
 * Created by tuanluong on 3/21/18.
 */
@Module
public class BlockContactModule {
    private final BlockContactPresenter.View view;

    public BlockContactModule(BlockContactPresenter.View view) {
        this.view = view;
    }

    @Provides
    @PerActivity
    public BlockContactPresenter.View provideView() {
        return view;
    }

    @Provides
    @PerActivity
    public BlockContactPresenter providePresenter(BlockContactPresenterImpl presenter) {
        return presenter;
    }
}
