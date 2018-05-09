package com.ping.android.dagger.loggedin.addcontact;

import com.ping.android.presentation.view.activity.AddContactActivity;
import com.ping.android.dagger.loggedin.SearchUserModule;
import com.ping.android.dagger.scopes.PerActivity;

import dagger.Subcomponent;

/**
 * Created by tuanluong on 1/25/18.
 */
@PerActivity
@Subcomponent(modules = { AddContactModule.class, SearchUserModule.class })
public interface AddContactComponent {
    void inject(AddContactActivity addContactActivity);
}
