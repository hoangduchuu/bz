package com.ping.android.dagger.loggedin.changepassword;

import com.ping.android.dagger.scopes.PerActivity;
import com.ping.android.presentation.view.activity.ChangePasswordActivity;

import dagger.Subcomponent;

/**
 * Created by tuanluong on 1/25/18.
 */
@PerActivity
@Subcomponent(modules = { ChangePasswordModule.class })
public interface ChangePasswordComponent {
    void inject(ChangePasswordActivity activity);
}
