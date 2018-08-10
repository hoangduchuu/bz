package com.ping.android.dagger.loggedin.main.profile;

import com.ping.android.dagger.scopes.PerFragment;
import com.ping.android.presentation.presenters.ProfilePresenter;
import com.ping.android.presentation.presenters.impl.ProfilePresenterImpl;
import com.ping.android.presentation.view.fragment.ProfileFragment;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;

/**
 * Created by tuanluong on 2/8/18.
 */
@Module
public abstract class ProfileModule {
    @Binds
    public abstract ProfilePresenter.View provideView(ProfileFragment fragment);

    @Provides
    @PerFragment
    public static ProfilePresenter providePresenter(ProfilePresenterImpl presenter) {
        return presenter;
    }
}
