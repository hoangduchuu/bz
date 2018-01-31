package com.ping.android.dagger.loggedin.main.call;

import com.ping.android.dagger.scopes.PerFragment;
import com.ping.android.presentation.view.fragment.CallFragment;

import dagger.Subcomponent;

/**
 * Created by tuanluong on 1/30/18.
 */
@PerFragment
@Subcomponent(modules = { CallModule.class })
public interface CallComponent {
    void inject(CallFragment fragment);
}
