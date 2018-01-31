package com.ping.android.dagger.loggedin.main.group;

import com.ping.android.dagger.scopes.PerFragment;
import com.ping.android.presentation.view.fragment.GroupFragment;

import dagger.Subcomponent;

/**
 * Created by tuanluong on 1/29/18.
 */

@PerFragment
@Subcomponent(modules = { GroupModule.class })
public interface GroupComponent {
    void inject(GroupFragment fragment);
}
