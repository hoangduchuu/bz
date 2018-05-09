package com.ping.android.dagger.loggedin.userdetail;

import com.ping.android.dagger.scopes.PerActivity;
import com.ping.android.presentation.presenters.UserDetailPresenter;
import com.ping.android.presentation.presenters.impl.UserDetailPresenterImpl;

import dagger.Module;
import dagger.Provides;

/**
 * Created by tuanluong on 3/14/18.
 */
@Module
public class UserDetailModule {
    private final UserDetailPresenter.View view;

    public UserDetailModule(UserDetailPresenter.View view) {
        this.view = view;
    }

    @Provides
    @PerActivity
    public UserDetailPresenter.View provideView() {
        return view;
    }

    @Provides
    @PerActivity
    public UserDetailPresenter provideUserDetailPresenter(UserDetailPresenterImpl presenter) {
        return presenter;
    }
}
