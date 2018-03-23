package com.ping.android.dagger.loggedin.changepassword;

import com.ping.android.dagger.scopes.PerActivity;
import com.ping.android.presentation.presenters.ChangePasswordPresenter;
import com.ping.android.presentation.presenters.impl.ChangePasswordPresenterImpl;

import dagger.Module;
import dagger.Provides;

/**
 * Created by tuanluong on 3/21/18.
 */
@Module
public class ChangePasswordModule {
    private final ChangePasswordPresenter.View view;

    public ChangePasswordModule(ChangePasswordPresenter.View view) {
        this.view = view;
    }

    @Provides
    @PerActivity
    public ChangePasswordPresenter.View provideView() {
        return view;
    }

    @Provides
    @PerActivity
    public ChangePasswordPresenter providePresenter(ChangePasswordPresenterImpl presenter) {
        return presenter;
    }
}
