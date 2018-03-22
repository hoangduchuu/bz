package com.ping.android.dagger.loggedin.transphabet.manualmapping;

import com.ping.android.dagger.scopes.PerFragment;
import com.ping.android.presentation.view.fragment.MappingFragment;

import dagger.Subcomponent;

/**
 * Created by tuanluong on 1/23/18.
 */
@PerFragment
@Subcomponent(modules = { ManualMappingModule.class })
public interface ManualMappingComponent {
    void inject(MappingFragment fragment);
}
