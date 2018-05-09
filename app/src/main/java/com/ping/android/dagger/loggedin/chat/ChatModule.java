package com.ping.android.dagger.loggedin.chat;

import com.ping.android.dagger.scopes.PerActivity;
import com.ping.android.presentation.presenters.ChatPresenter;
import com.ping.android.presentation.presenters.impl.ChatPresenterImpl;

import dagger.Module;
import dagger.Provides;

/**
 * Created by tuanluong on 2/26/18.
 */

@Module
public class ChatModule {
    private final ChatPresenter.View view;

    public ChatModule(ChatPresenter.View view) {
        this.view = view;
    }

    @Provides
    @PerActivity
    public ChatPresenter.View provideView() {
        return view;
    }

    @Provides
    @PerActivity
    public ChatPresenter provideChatPresenter(ChatPresenterImpl presenter) {
        return presenter;
    }
}
