package com.ping.android.dagger.loggedin.call.video;

import com.ping.android.dagger.scopes.PerFragment;
import com.ping.android.presentation.view.fragment.VideoConversationFragment;

import dagger.Subcomponent;

/**
 * Created by tuanluong on 3/20/18.
 */
@PerFragment
@Subcomponent(modules = { VideoCallModule.class})
public interface VideoCallComponent {
    void inject(VideoConversationFragment fragment);
}
