package com.ping.android.dagger.loggedin.call;

import com.ping.android.dagger.loggedin.call.audio.AudioCallComponent;
import com.ping.android.dagger.loggedin.call.audio.AudioCallModule;
import com.ping.android.dagger.loggedin.call.incoming.IncomingCallComponent;
import com.ping.android.dagger.loggedin.call.incoming.IncomingCallModule;
import com.ping.android.dagger.loggedin.call.video.VideoCallComponent;
import com.ping.android.dagger.loggedin.call.video.VideoCallModule;
import com.ping.android.dagger.scopes.PerActivity;
import com.ping.android.presentation.view.activity.CallActivity;

import dagger.Subcomponent;

/**
 * Created by tuanluong on 3/16/18.
 */
@PerActivity
@Subcomponent(modules = { CallModule.class })
public interface CallComponent {
    void inject(CallActivity activity);

    IncomingCallComponent provideIncomingCallComponent(IncomingCallModule module);

    AudioCallComponent provideAudioCallComponent(AudioCallModule module);

    VideoCallComponent provideVideoCallComponent(VideoCallModule module);
}
