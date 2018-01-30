package com.ping.android.dagger.loggedin.main.call;

import com.ping.android.dagger.scopes.PerFragment;
import com.ping.android.presentation.presenters.CallPresenter;
import com.ping.android.presentation.presenters.impl.CallPresenterImpl;

import dagger.Module;
import dagger.Provides;

/**
 * Created by tuanluong on 1/30/18.
 */
@Module
public class CallModule {
    private final CallPresenter.View view;

    public CallModule(CallPresenter.View view) {
        this.view = view;
    }

    @Provides
    @PerFragment
    CallPresenter.View provideView() {
        return view;
    }

    @Provides
    @PerFragment
    CallPresenter provideCallPresenter(CallPresenterImpl presenter) {
        return presenter;
    }
}
