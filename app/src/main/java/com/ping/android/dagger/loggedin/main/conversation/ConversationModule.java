package com.ping.android.dagger.loggedin.main.conversation;

import com.ping.android.dagger.scopes.PerFragment;
import com.ping.android.presentation.presenters.ConversationListPresenter;
import com.ping.android.presentation.presenters.impl.ConversationListPresenterImpl;

import dagger.Module;
import dagger.Provides;

/**
 * Created by tuanluong on 1/28/18.
 */

@Module
public class ConversationModule {
    ConversationListPresenter.View view;

    public ConversationModule(ConversationListPresenter.View view) {
        this.view = view;
    }

    @Provides
    @PerFragment
    public ConversationListPresenter.View provideView() {
        return view;
    }

    @Provides
    @PerFragment
    public ConversationListPresenter provideConversationPresenter(ConversationListPresenterImpl presenter) {
        return presenter;
    }
}
