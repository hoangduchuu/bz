package com.ping.android.dagger.loggedin.selectcontact;

import com.ping.android.dagger.scopes.PerActivity;
import com.ping.android.presentation.presenters.SelectContactPresenter;
import com.ping.android.presentation.presenters.impl.SelectContactPresenterImpl;

import dagger.Module;
import dagger.Provides;

/**
 * Created by tuanluong on 3/21/18.
 */
@Module
public class SelectContactModule {
    private final SelectContactPresenter.View view;

    public SelectContactModule(SelectContactPresenter.View view) {
        this.view = view;
    }

    @Provides
    @PerActivity
    public SelectContactPresenter.View provideView() {
        return view;
    }

    @Provides
    @PerActivity
    public SelectContactPresenter providePresenter(SelectContactPresenterImpl presenter) {
        return presenter;
    }
}
