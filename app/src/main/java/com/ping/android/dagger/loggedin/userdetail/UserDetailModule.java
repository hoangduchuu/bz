package com.ping.android.dagger.loggedin.userdetail;

import com.ping.android.dagger.scopes.PerActivity;
import com.ping.android.presentation.presenters.UserDetailPresenter;
import com.ping.android.presentation.presenters.impl.UserDetailPresenterImpl;
import com.ping.android.presentation.view.activity.UserDetailActivity;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;

/**
 * Created by tuanluong on 3/14/18.
 */
@Module
public abstract class UserDetailModule {
    @Binds
    public abstract UserDetailPresenter.View provideView(UserDetailActivity activity);

    @Provides
    @PerActivity
    public static UserDetailPresenter provideUserDetailPresenter(UserDetailPresenterImpl presenter) {
        return presenter;
    }
}
