package com.ping.android.dagger.loggedin.main;

import com.ping.android.activity.MainActivity;
import com.ping.android.dagger.loggedin.RepositoryModule;
import com.ping.android.dagger.loggedin.main.conversation.ConversationComponent;
import com.ping.android.dagger.loggedin.main.conversation.ConversationModule;
import com.ping.android.dagger.loggedin.main.group.GroupComponent;
import com.ping.android.dagger.loggedin.main.group.GroupModule;
import com.ping.android.dagger.scopes.PerActivity;

import dagger.Subcomponent;

/**
 * Created by tuanluong on 1/28/18.
 */
@PerActivity
@Subcomponent(modules = { MainModule.class })
public interface MainComponent {
    void inject(MainActivity activity);

    ConversationComponent provideConversationComponent(ConversationModule module);

    GroupComponent provideGroupComponent(GroupModule groupModule);
}
