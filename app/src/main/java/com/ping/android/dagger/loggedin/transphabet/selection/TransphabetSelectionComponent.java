package com.ping.android.dagger.loggedin.transphabet.selection;

import com.ping.android.dagger.scopes.PerFragment;
import com.ping.android.fragment.transphabet.SelectiveCategoriesFragment;
import com.ping.android.fragment.transphabet.TransphabetFragment;

import dagger.Subcomponent;

/**
 * Created by tuanluong on 2/2/18.
 */
@PerFragment
@Subcomponent
public interface TransphabetSelectionComponent {
    void inject(TransphabetFragment fragment);

    void inject(SelectiveCategoriesFragment fragment);
}
