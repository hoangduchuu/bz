package com.ping.android.dagger.loggedin.main.conversation;

import com.ping.android.dagger.scopes.PerFragment;
import com.ping.android.presentation.presenters.ConversationPresenter;
import com.ping.android.presentation.presenters.impl.ConversationPresenterImpl;

import dagger.Module;
import dagger.Provides;

/**
 * Created by tuanluong on 1/28/18.
 */

@Module
public class ConversationModule {
    ConversationPresenter.View view;

    public ConversationModule(ConversationPresenter.View view) {
        this.view = view;
    }

    @Provides
    @PerFragment
    public ConversationPresenter.View provideView() {
        return view;
    }

    @Provides
    @PerFragment
    public ConversationPresenter provideConversationPresenter(ConversationPresenterImpl presenter) {
        return presenter;
    }
}
