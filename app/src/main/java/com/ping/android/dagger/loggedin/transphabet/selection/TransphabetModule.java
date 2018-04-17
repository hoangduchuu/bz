package com.ping.android.dagger.loggedin.transphabet.selection;

import com.ping.android.dagger.scopes.PerActivity;
import com.ping.android.dagger.scopes.PerFragment;
import com.ping.android.presentation.presenters.TransphabetPresenter;
import com.ping.android.presentation.presenters.impl.TransphabetPresenterImpl;

import dagger.Module;
import dagger.Provides;

/**
 * Created by tuanluong on 2/2/18.
 */

@Module
public class TransphabetModule {
    private final TransphabetPresenter.View view;

    public TransphabetModule(TransphabetPresenter.View view) {
        this.view = view;
    }

    @Provides
    @PerFragment
    public TransphabetPresenter provideTransphabetPresenter(TransphabetPresenterImpl presenter) {
        return presenter;
    }
}
