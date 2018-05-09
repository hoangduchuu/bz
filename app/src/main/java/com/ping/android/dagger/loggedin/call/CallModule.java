package com.ping.android.dagger.loggedin.call;

import com.ping.android.dagger.scopes.PerActivity;
import com.ping.android.presentation.presenters.CallPresenter;
import com.ping.android.presentation.presenters.impl.CallPresenterImpl;

import dagger.Module;
import dagger.Provides;

/**
 * Created by tuanluong on 3/16/18.
 */
@Module
public class CallModule {
    private final CallPresenter.View view;

    public CallModule(CallPresenter.View view) {
        this.view = view;
    }

    @Provides
    @PerActivity
    CallPresenter.View provideView() {
        return view;
    }

    @Provides
    @PerActivity
    CallPresenter provideCallPresenter(CallPresenterImpl presenter) {
        return presenter;
    }
}
