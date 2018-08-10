package com.ping.android.dagger.loggedin.addphone;

import com.ping.android.dagger.scopes.PerActivity;
import com.ping.android.presentation.presenters.AddPhonePresenter;
import com.ping.android.presentation.presenters.impl.AddPhonePresenterImpl;
import com.ping.android.presentation.view.activity.PhoneActivity;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;

@Module
public abstract class AddPhoneModule {
    @Binds
    public abstract AddPhonePresenter.View provideView(PhoneActivity activity);

    @Provides
    @PerActivity
    public static AddPhonePresenter provideAddPhonePresenter(AddPhonePresenterImpl presenter) {
        return presenter;
    }
}
