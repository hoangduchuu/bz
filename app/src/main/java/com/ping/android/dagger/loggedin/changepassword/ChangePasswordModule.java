package com.ping.android.dagger.loggedin.changepassword;

import com.ping.android.dagger.scopes.PerActivity;
import com.ping.android.presentation.presenters.ChangePasswordPresenter;
import com.ping.android.presentation.presenters.impl.ChangePasswordPresenterImpl;
import com.ping.android.presentation.view.activity.ChangePasswordActivity;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;

/**
 * Created by tuanluong on 3/21/18.
 */
@Module
public abstract class ChangePasswordModule {
    @Binds
    public abstract ChangePasswordPresenter.View provideView(ChangePasswordActivity activity);

    @Provides
    @PerActivity
    public static ChangePasswordPresenter providePresenter(ChangePasswordPresenterImpl presenter) {
        return presenter;
    }
}
