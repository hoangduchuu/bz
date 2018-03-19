package com.ping.android.dagger.loggedin.main.call;

import com.ping.android.dagger.scopes.PerFragment;
import com.ping.android.presentation.presenters.CallListPresenter;
import com.ping.android.presentation.presenters.impl.CallListPresenterImpl;

import dagger.Module;
import dagger.Provides;

/**
 * Created by tuanluong on 1/30/18.
 */
@Module
public class CallModule {
    private final CallListPresenter.View view;

    public CallModule(CallListPresenter.View view) {
        this.view = view;
    }

    @Provides
    @PerFragment
    CallListPresenter.View provideView() {
        return view;
    }

    @Provides
    @PerFragment
    CallListPresenter provideCallPresenter(CallListPresenterImpl presenter) {
        return presenter;
    }
}
