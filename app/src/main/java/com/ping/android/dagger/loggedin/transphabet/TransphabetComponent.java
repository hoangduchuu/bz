package com.ping.android.dagger.loggedin.transphabet;

import com.ping.android.dagger.loggedin.transphabet.selection.TransphabetSelectionComponent;
import com.ping.android.dagger.scopes.PerActivity;

import dagger.Subcomponent;

/**
 * Created by tuanluong on 2/2/18.
 */
@PerActivity
@Subcomponent
public interface TransphabetComponent {
    TransphabetSelectionComponent provideTransphabetSelectionComponent();
}
