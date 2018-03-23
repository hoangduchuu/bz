package com.ping.android.dagger.loggedin.call.audio;

import com.ping.android.dagger.scopes.PerFragment;
import com.ping.android.presentation.view.fragment.AudioConversationFragment;

import dagger.Subcomponent;

/**
 * Created by tuanluong on 3/20/18.
 */
@PerFragment
@Subcomponent(modules = { AudioCallModule.class})
public interface AudioCallComponent {
    void inject(AudioConversationFragment fragment);
}
