package com.ping.android.dagger.loggedin.main.conversation;

import com.ping.android.dagger.scopes.PerFragment;
import com.ping.android.fragment.ConversationFragment;

import dagger.Subcomponent;

/**
 * Created by tuanluong on 1/28/18.
 */
@PerFragment
@Subcomponent(modules = { ConversationModule.class })
public interface ConversationComponent {
    void inject(ConversationFragment fragment);
}
