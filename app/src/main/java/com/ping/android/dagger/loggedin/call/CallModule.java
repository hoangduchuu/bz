package com.ping.android.dagger.loggedin.call;

import com.ping.android.dagger.loggedin.call.audio.AudioCallModule;
import com.ping.android.dagger.loggedin.call.incoming.IncomingCallModule;
import com.ping.android.dagger.loggedin.call.video.VideoCallModule;
import com.ping.android.dagger.scopes.PerActivity;
import com.ping.android.dagger.scopes.PerFragment;
import com.ping.android.presentation.presenters.CallPresenter;
import com.ping.android.presentation.presenters.impl.CallPresenterImpl;
import com.ping.android.presentation.view.activity.CallActivity;
import com.ping.android.presentation.view.fragment.AudioConversationFragment;
import com.ping.android.presentation.view.fragment.IncomeCallFragment;
import com.ping.android.presentation.view.fragment.VideoConversationFragment;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import dagger.android.ContributesAndroidInjector;

/**
 * Created by tuanluong on 3/16/18.
 */
@Module
public abstract class CallModule {
    @Binds
    abstract CallPresenter.View provideView(CallActivity callActivity);

    @Provides
    @PerActivity
    static CallPresenter provideCallPresenter(CallPresenterImpl presenter) {
        return presenter;
    }

    @PerFragment
    @ContributesAndroidInjector(modules = IncomingCallModule.class)
    abstract IncomeCallFragment bindIncomeCallFragment();

    @PerFragment
    @ContributesAndroidInjector(modules = AudioCallModule.class)
    abstract AudioConversationFragment bindAudioConversationFragment();

    @PerFragment
    @ContributesAndroidInjector(modules = VideoCallModule.class)
    abstract VideoConversationFragment bindVideoConversationFragment();
}
