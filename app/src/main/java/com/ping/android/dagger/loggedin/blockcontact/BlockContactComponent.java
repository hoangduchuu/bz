package com.ping.android.dagger.loggedin.blockcontact;

import com.ping.android.dagger.scopes.PerActivity;
import com.ping.android.presentation.view.activity.BlockActivity;

import dagger.Subcomponent;

/**
 * Created by tuanluong on 1/25/18.
 */
@PerActivity
@Subcomponent(modules = { BlockContactModule.class })
public interface BlockContactComponent {
    void inject(BlockActivity activity);
}
