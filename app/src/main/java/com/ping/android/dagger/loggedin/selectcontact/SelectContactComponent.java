package com.ping.android.dagger.loggedin.selectcontact;

import com.ping.android.dagger.scopes.PerActivity;
import com.ping.android.presentation.view.activity.SelectContactActivity;

import dagger.Subcomponent;

/**
 * Created by tuanluong on 1/25/18.
 */
@PerActivity
@Subcomponent(modules = { SelectContactModule.class })
public interface SelectContactComponent {
    void inject(SelectContactActivity activity);
}
