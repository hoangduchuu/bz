package com.ping.android.dagger.loggedin.chat;

import com.ping.android.dagger.scopes.PerActivity;
import com.ping.android.presentation.presenters.ChatPresenter;
import com.ping.android.presentation.presenters.impl.ChatPresenterImpl;
import com.ping.android.presentation.view.activity.ChatActivity;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;

/**
 * Created by tuanluong on 2/26/18.
 */

@Module
public abstract class ChatModule {
    @Binds
    public abstract ChatPresenter.View provideView(ChatActivity activity);

    @Provides
    @PerActivity
    public static ChatPresenter provideChatPresenter(ChatPresenterImpl presenter) {
        return presenter;
    }
}
