package com.ping.android.dagger.loggedin.call.audio;

import com.ping.android.dagger.scopes.PerFragment;
import com.ping.android.presentation.presenters.AudioCallPresenter;
import com.ping.android.presentation.presenters.impl.AudioCallPresenterImpl;
import com.ping.android.presentation.view.fragment.AudioConversationFragment;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;

/**
 * Created by tuanluong on 3/20/18.
 */
@Module
public abstract class AudioCallModule {
    @Binds
    public abstract AudioCallPresenter.View provideView(AudioConversationFragment fragment);

    @Provides
    @PerFragment
    public static AudioCallPresenter providePresenter(AudioCallPresenterImpl presenter) {
        return presenter;
    }
}
