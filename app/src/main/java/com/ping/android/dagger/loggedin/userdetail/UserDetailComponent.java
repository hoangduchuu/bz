package com.ping.android.dagger.loggedin.userdetail;

import com.ping.android.dagger.scopes.PerActivity;
import com.ping.android.presentation.view.activity.UserDetailActivity;

import dagger.Subcomponent;

/**
 * Created by tuanluong on 3/14/18.
 */
@PerActivity
@Subcomponent(modules = { UserDetailModule.class })
public interface UserDetailComponent {
    void inject(UserDetailActivity activity);
}
