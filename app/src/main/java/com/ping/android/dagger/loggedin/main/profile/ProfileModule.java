package com.ping.android.dagger.loggedin.main.profile;

import com.ping.android.dagger.scopes.PerFragment;
import com.ping.android.presentation.presenters.ProfilePresenter;
import com.ping.android.presentation.presenters.impl.ProfilePresenterImpl;

import dagger.Module;
import dagger.Provides;

/**
 * Created by tuanluong on 2/8/18.
 */
@Module
public class ProfileModule {
    private final ProfilePresenter.View view;

    public ProfileModule(ProfilePresenter.View view) {
        this.view = view;
    }

    @Provides
    @PerFragment
    public ProfilePresenter.View provideView() {
        return view;
    }

    @Provides
    @PerFragment
    public ProfilePresenter providePresenter(ProfilePresenterImpl presenter) {
        return presenter;
    }
}
