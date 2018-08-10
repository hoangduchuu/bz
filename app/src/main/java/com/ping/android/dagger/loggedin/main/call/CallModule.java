package com.ping.android.dagger.loggedin.main.call;

import com.ping.android.dagger.scopes.PerFragment;
import com.ping.android.presentation.presenters.CallListPresenter;
import com.ping.android.presentation.presenters.impl.CallListPresenterImpl;
import com.ping.android.presentation.view.fragment.CallFragment;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;

/**
 * Created by tuanluong on 1/30/18.
 */
@Module
public abstract class CallModule {
    @Binds
    abstract CallListPresenter.View provideView(CallFragment fragment);

    @Provides
    @PerFragment
    static CallListPresenter provideCallPresenter(CallListPresenterImpl presenter) {
        return presenter;
    }
}
