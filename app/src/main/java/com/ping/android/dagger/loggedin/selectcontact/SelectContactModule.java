package com.ping.android.dagger.loggedin.selectcontact;

import com.ping.android.dagger.scopes.PerActivity;
import com.ping.android.presentation.presenters.SelectContactPresenter;
import com.ping.android.presentation.presenters.impl.SelectContactPresenterImpl;
import com.ping.android.presentation.view.activity.SelectContactActivity;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;

/**
 * Created by tuanluong on 3/21/18.
 */
@Module
public abstract class SelectContactModule {
    @Binds
    public abstract SelectContactPresenter.View provideView(SelectContactActivity activity);

    @Provides
    @PerActivity
    public static SelectContactPresenter providePresenter(SelectContactPresenterImpl presenter) {
        return presenter;
    }
}
