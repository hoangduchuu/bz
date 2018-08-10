package com.ping.android.dagger.loggedin.addcontact;

import com.ping.android.dagger.scopes.PerActivity;
import com.ping.android.presentation.presenters.AddContactPresenter;
import com.ping.android.presentation.presenters.SearchUserPresenter;
import com.ping.android.presentation.presenters.impl.AddContactPresenterImpl;
import com.ping.android.presentation.view.activity.AddContactActivity;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;

/**
 * Created by tuanluong on 3/21/18.
 */
@Module
public abstract class AddContactModule {
    @Binds
    abstract AddContactPresenter.View provideView(AddContactActivity addContactActivity);

    @Binds
    abstract SearchUserPresenter.View proviView(AddContactActivity addContactActivity);

    @Provides
    @PerActivity
    public static AddContactPresenter providePresenter(AddContactPresenterImpl presenter) {
        return presenter;
    }
}
