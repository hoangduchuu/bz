package com.ping.android.dagger.loggedin.transphabet.manualmapping;

import com.ping.android.dagger.scopes.PerActivity;
import com.ping.android.dagger.scopes.PerFragment;
import com.ping.android.presentation.presenters.ManualMappingPresenter;
import com.ping.android.presentation.presenters.impl.ManualMappingPresenterImpl;

import dagger.Module;
import dagger.Provides;

/**
 * Created by tuanluong on 1/23/18.
 */
@Module
public class ManualMappingModule {
    ManualMappingPresenter.View view;

    public ManualMappingModule(ManualMappingPresenter.View view) {
        this.view = view;
    }

    @Provides
    @PerFragment
    public ManualMappingPresenter.View provideView() {
        return view;
    }

    @Provides
    @PerFragment
    public ManualMappingPresenter provideNewChatPresenter(ManualMappingPresenterImpl presenter) {
        return presenter;
    }
}
