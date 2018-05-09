package com.ping.android.dagger.loggedin.main.contact;

import com.ping.android.dagger.scopes.PerFragment;
import com.ping.android.presentation.view.fragment.ContactFragment;

import dagger.Subcomponent;

/**
 * Created by tuanluong on 3/14/18.
 */
@PerFragment
@Subcomponent(modules = {ContactModule.class})
public interface ContactComponent {
    void inject(ContactFragment fragment);
}
