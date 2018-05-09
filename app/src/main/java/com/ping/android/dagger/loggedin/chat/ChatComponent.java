package com.ping.android.dagger.loggedin.chat;

import com.ping.android.dagger.scopes.PerActivity;
import com.ping.android.presentation.view.activity.ChatActivity;

import dagger.Subcomponent;

/**
 * Created by tuanluong on 2/26/18.
 */
@PerActivity
@Subcomponent(modules = { ChatModule.class })
public interface ChatComponent {
    void inject(ChatActivity activity);
}
