package com.ping.android.dagger.loggedin.main.group;

import com.ping.android.dagger.scopes.PerFragment;
import com.ping.android.presentation.presenters.GroupPresenter;
import com.ping.android.presentation.presenters.impl.GroupPresenterImpl;
import com.ping.android.presentation.view.fragment.GroupFragment;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;

/**
 * Created by tuanluong on 1/29/18.
 */
@Module
public abstract class GroupModule {
    @Binds
    public abstract GroupPresenter.View provideView(GroupFragment fragment);

    @Provides
    @PerFragment
    public static GroupPresenter providePresenter(GroupPresenterImpl presenter) {
        return presenter;
    }
}
