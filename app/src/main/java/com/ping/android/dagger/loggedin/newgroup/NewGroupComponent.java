package com.ping.android.dagger.loggedin.newgroup;

import com.ping.android.dagger.loggedin.SearchUserModule;
import com.ping.android.dagger.scopes.PerActivity;
import com.ping.android.presentation.activity.AddGroupActivity;

import dagger.Subcomponent;

/**
 * Created by tuanluong on 1/25/18.
 */
@PerActivity
@Subcomponent(modules = { NewGroupModule.class, SearchUserModule.class })
public interface NewGroupComponent {
    void inject(AddGroupActivity activity);
}
