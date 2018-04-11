package com.ping.android.dagger.loggedin.addphone;

import com.ping.android.dagger.scopes.PerActivity;
import com.ping.android.presentation.view.activity.PhoneActivity;

import dagger.Subcomponent;

@PerActivity
@Subcomponent(modules = { AddPhoneModule.class })
public interface AddPhoneComponent {
    void inject(PhoneActivity activity);
}
