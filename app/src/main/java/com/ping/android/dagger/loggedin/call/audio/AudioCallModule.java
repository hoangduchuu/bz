package com.ping.android.dagger.loggedin.call.audio;

import com.ping.android.dagger.scopes.PerFragment;
import com.ping.android.presentation.presenters.AudioCallPresenter;
import com.ping.android.presentation.presenters.impl.AudioCallPresenterImpl;

import dagger.Module;
import dagger.Provides;

/**
 * Created by tuanluong on 3/20/18.
 */
@Module
public class AudioCallModule {
    private final AudioCallPresenter.View view;

    public AudioCallModule(AudioCallPresenter.View view) {
        this.view = view;
    }

    @Provides
    @PerFragment
    public AudioCallPresenter.View provideView() {
        return view;
    }

    @Provides
    @PerFragment
    public AudioCallPresenter providePresenter(AudioCallPresenterImpl presenter) {
        return presenter;
    }
}
