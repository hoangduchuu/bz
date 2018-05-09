package com.ping.android.dagger.loggedin.newgroup;

import com.ping.android.dagger.scopes.PerActivity;
import com.ping.android.presentation.presenters.AddGroupPresenter;
import com.ping.android.presentation.presenters.impl.AddGroupPresenterImpl;

import dagger.Module;
import dagger.Provides;

/**
 * Created by tuanluong on 1/25/18.
 */

@Module
public class NewGroupModule {
    public AddGroupPresenter.View view;

    public NewGroupModule(AddGroupPresenter.View view) {
        this.view = view;
    }

    @Provides
    @PerActivity
    public AddGroupPresenter.View provideView() {
        return view;
    }

    @Provides
    @PerActivity
    public AddGroupPresenter provideAddGroupPresenter(AddGroupPresenterImpl presenter) {
        return presenter;
    }
}
