package com.ping.android.dagger.loggedin.newchat;

import com.ping.android.dagger.loggedin.SearchUserModule;
import com.ping.android.dagger.scopes.PerActivity;
import com.ping.android.presentation.activity.NewChatActivity;

import dagger.Subcomponent;

/**
 * Created by tuanluong on 1/23/18.
 */
@PerActivity
@Subcomponent(modules = { NewChatModule.class, SearchUserModule.class})
public interface NewChatComponent {
    void inject(NewChatActivity newChatActivity);
}
