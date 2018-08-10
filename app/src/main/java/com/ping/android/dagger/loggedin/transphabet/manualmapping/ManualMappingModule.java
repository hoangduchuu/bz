package com.ping.android.dagger.loggedin.transphabet.manualmapping;

import com.ping.android.dagger.scopes.PerFragment;
import com.ping.android.presentation.presenters.ManualMappingPresenter;
import com.ping.android.presentation.presenters.impl.ManualMappingPresenterImpl;
import com.ping.android.presentation.view.fragment.MappingFragment;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;

/**
 * Created by tuanluong on 1/23/18.
 */
@Module
public abstract class ManualMappingModule {
    @Binds
    public abstract ManualMappingPresenter.View provideView(MappingFragment fragment);

    @Provides
    @PerFragment
    public static ManualMappingPresenter provideNewChatPresenter(ManualMappingPresenterImpl presenter) {
        return presenter;
    }
}
