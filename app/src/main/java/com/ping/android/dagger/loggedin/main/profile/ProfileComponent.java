package com.ping.android.dagger.loggedin.main.profile;

import com.ping.android.dagger.scopes.PerFragment;
import com.ping.android.presentation.view.fragment.ProfileFragment;

import dagger.Subcomponent;

/**
 * Created by tuanluong on 2/8/18.
 */
@PerFragment
@Subcomponent(modules = { ProfileModule.class })
public interface ProfileComponent {
    void inject(ProfileFragment fragment);
}
