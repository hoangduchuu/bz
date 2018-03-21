package com.ping.android.dagger.loggedin.call.incoming;

import com.ping.android.dagger.scopes.PerFragment;
import com.ping.android.fragment.IncomeCallFragment;

import dagger.Subcomponent;

/**
 * Created by tuanluong on 3/20/18.
 */
@PerFragment
@Subcomponent(modules = { IncomingCallModule.class})
public interface IncomingCallComponent {
    void inject(IncomeCallFragment fragment);
}
