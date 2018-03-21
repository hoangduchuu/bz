package com.ping.android.dagger.loggedin.call.incoming;

import com.ping.android.dagger.scopes.PerFragment;
import com.ping.android.presentation.presenters.IncomingCallPresenter;
import com.ping.android.presentation.presenters.impl.IncomingCallPresenterImpl;

import dagger.Module;
import dagger.Provides;

/**
 * Created by tuanluong on 3/20/18.
 */
@Module
public class IncomingCallModule {
    private final IncomingCallPresenter.View view;

    public IncomingCallModule(IncomingCallPresenter.View view) {
        this.view = view;
    }

    @Provides
    @PerFragment
    public IncomingCallPresenter.View provideView() {
        return view;
    }

    @Provides
    @PerFragment
    public IncomingCallPresenter providePresenter(IncomingCallPresenterImpl presenter) {
        return presenter;
    }
}
