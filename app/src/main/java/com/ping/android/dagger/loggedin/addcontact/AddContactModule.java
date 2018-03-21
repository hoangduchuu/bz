package com.ping.android.dagger.loggedin.addcontact;

import com.ping.android.dagger.scopes.PerActivity;
import com.ping.android.presentation.presenters.AddContactPresenter;
import com.ping.android.presentation.presenters.impl.AddContactPresenterImpl;

import dagger.Module;
import dagger.Provides;

/**
 * Created by tuanluong on 3/21/18.
 */
@Module
public class AddContactModule {
    private final AddContactPresenter.View view;

    public AddContactModule(AddContactPresenter.View view) {
        this.view = view;
    }

    @Provides
    @PerActivity
    public AddContactPresenter.View provideView() {
        return view;
    }

    @Provides
    @PerActivity
    public AddContactPresenter providePresenter(AddContactPresenterImpl presenter) {
        return presenter;
    }
}
