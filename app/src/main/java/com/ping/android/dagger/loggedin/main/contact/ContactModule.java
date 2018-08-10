package com.ping.android.dagger.loggedin.main.contact;

import com.ping.android.dagger.scopes.PerFragment;
import com.ping.android.presentation.presenters.ContactPresenter;
import com.ping.android.presentation.presenters.impl.ContactPresenterImpl;
import com.ping.android.presentation.view.fragment.CallFragment;
import com.ping.android.presentation.view.fragment.ContactFragment;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;

/**
 * Created by tuanluong on 3/14/18.
 */

@Module
public abstract class ContactModule {
    @Binds
    public abstract ContactPresenter.View provideView(ContactFragment fragment);

    @Provides
    @PerFragment
    public static ContactPresenter provideContactPresenter(ContactPresenterImpl presenter) {
        return presenter;
    }
}
