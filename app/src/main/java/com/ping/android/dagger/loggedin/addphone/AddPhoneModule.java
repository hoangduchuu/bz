package com.ping.android.dagger.loggedin.addphone;

import com.ping.android.dagger.scopes.PerActivity;
import com.ping.android.presentation.presenters.AddPhonePresenter;
import com.ping.android.presentation.presenters.impl.AddPhonePresenterImpl;

import dagger.Module;
import dagger.Provides;

@Module
public class AddPhoneModule {
    private final AddPhonePresenter.View view;

    public AddPhoneModule(AddPhonePresenter.View view) {
        this.view = view;
    }

    @Provides
    @PerActivity
    public AddPhonePresenter.View provideView() {
        return view;
    }

    @Provides
    @PerActivity
    public AddPhonePresenter provideAddPhonePresenter(AddPhonePresenterImpl presenter) {
        return presenter;
    }
}
