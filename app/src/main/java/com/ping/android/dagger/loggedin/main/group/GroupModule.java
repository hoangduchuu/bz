package com.ping.android.dagger.loggedin.main.group;

import com.ping.android.dagger.scopes.PerFragment;
import com.ping.android.presentation.presenters.GroupPresenter;
import com.ping.android.presentation.presenters.impl.GroupPresenterImpl;

import dagger.Module;
import dagger.Provides;

/**
 * Created by tuanluong on 1/29/18.
 */
@Module
public class GroupModule {
    private final GroupPresenter.View view;

    public GroupModule(GroupPresenter.View view) {
        this.view = view;
    }

    @Provides
    @PerFragment
    public GroupPresenter.View provideView() {
        return view;
    }

    @Provides
    @PerFragment
    public GroupPresenter providePresenter(GroupPresenterImpl presenter) {
        return presenter;
    }
}
