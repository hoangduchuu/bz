package com.ping.android.dagger.loggedin.main.contact;

import com.ping.android.dagger.scopes.PerFragment;
import com.ping.android.presentation.presenters.ContactPresenter;
import com.ping.android.presentation.presenters.impl.ContactPresenterImpl;

import dagger.Module;
import dagger.Provides;

/**
 * Created by tuanluong on 3/14/18.
 */

@Module
public class ContactModule {
    private final ContactPresenter.View view;

    public ContactModule(ContactPresenter.View view) {
        this.view = view;
    }

    @Provides
    @PerFragment
    public ContactPresenter.View provideView() {
        return view;
    }

    @Provides
    @PerFragment
    public ContactPresenter provideContactPresenter(ContactPresenterImpl presenter) {
        return presenter;
    }
}
