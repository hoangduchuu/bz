package com.ping.android.dagger.loggedin.call.incoming;

import com.ping.android.dagger.scopes.PerFragment;
import com.ping.android.presentation.presenters.IncomingCallPresenter;
import com.ping.android.presentation.presenters.impl.IncomingCallPresenterImpl;
import com.ping.android.presentation.view.fragment.IncomeCallFragment;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;

/**
 * Created by tuanluong on 3/20/18.
 */
@Module
public abstract class IncomingCallModule {
    @Binds
    abstract IncomingCallPresenter.View provideView(IncomeCallFragment fragment);

    @Provides
    @PerFragment
    public static IncomingCallPresenter providePresenter(IncomingCallPresenterImpl presenter) {
        return presenter;
    }
}
