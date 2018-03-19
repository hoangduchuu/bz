package com.ping.android.dagger.loggedin.call;

import com.ping.android.activity.CallActivity;
import com.ping.android.dagger.scopes.PerActivity;

import dagger.Subcomponent;

/**
 * Created by tuanluong on 3/16/18.
 */
@PerActivity
@Subcomponent(modules = { CallModule.class })
public interface CallComponent {
    void inject(CallActivity activity);
}
