package com.ping.android.dagger.loggedin.conversationdetail;

import com.ping.android.dagger.scopes.PerActivity;
import com.ping.android.utils.Navigator;

import dagger.Module;
import dagger.Provides;

@Module
public class ConversationDetailModule {
    public ConversationDetailModule() {}

    @Provides
    @PerActivity
    public Navigator provideNavigator() {
        return new Navigator();
    }
}
