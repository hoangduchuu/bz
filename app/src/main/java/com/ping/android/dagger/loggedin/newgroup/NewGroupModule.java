package com.ping.android.dagger.loggedin.newgroup;

import com.ping.android.dagger.scopes.PerActivity;
import com.ping.android.presentation.presenters.AddGroupPresenter;
import com.ping.android.presentation.presenters.SearchUserPresenter;
import com.ping.android.presentation.presenters.impl.AddGroupPresenterImpl;
import com.ping.android.presentation.view.activity.AddGroupActivity;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;

/**
 * Created by tuanluong on 1/25/18.
 */

@Module
public abstract class NewGroupModule {
    @Binds
    public abstract AddGroupPresenter.View provideView(AddGroupActivity activity);

    @Binds
    public abstract SearchUserPresenter.View provideSearchView(AddGroupActivity addGroupActivity);

    @Provides
    @PerActivity
    public static AddGroupPresenter provideAddGroupPresenter(AddGroupPresenterImpl presenter) {
        return presenter;
    }
}
