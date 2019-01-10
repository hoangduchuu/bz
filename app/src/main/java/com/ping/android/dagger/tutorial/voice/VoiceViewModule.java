package com.ping.android.dagger.tutorial.voice;

import com.ping.android.dagger.scopes.PerFragment;
import com.ping.android.presentation.view.tutorial.type.TutoTyingFragment;
import com.ping.android.presentation.view.tutorial.type.TutoTypingContract;
import com.ping.android.presentation.view.tutorial.type.TutoTypingPresenter;
import com.ping.android.presentation.view.tutorial.voice.TutoVoiceContract;
import com.ping.android.presentation.view.tutorial.voice.TutoVoiceFragment;
import com.ping.android.presentation.view.tutorial.voice.TutoVoicePresenter;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
/**
 * Created by Huu Hoang on 28/12/2018
 */
@Module
public abstract class VoiceViewModule {
    @Binds
    abstract TutoVoiceContract.View provideView(TutoVoiceFragment fragment);

    @Provides
    @PerFragment
    static TutoVoiceContract.Presenter provideVoicePresenter(TutoVoicePresenter presenter) {
        return presenter;
    }
}
