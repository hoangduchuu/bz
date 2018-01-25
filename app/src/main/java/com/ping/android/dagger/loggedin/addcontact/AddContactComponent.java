package com.ping.android.dagger.loggedin.addcontact;

import com.ping.android.activity.AddContactActivity;
import com.ping.android.dagger.loggedin.SearchUserModule;
import com.ping.android.dagger.scopes.PerActivity;

import dagger.Subcomponent;

/**
 * Created by tuanluong on 1/25/18.
 */
@PerActivity
@Subcomponent(modules = { SearchUserModule.class })
public interface AddContactComponent {
    void inject(AddContactActivity addContactActivity);
}
