package com.ping.android.dagger.loggedin.main.conversation;

import com.ping.android.dagger.scopes.PerFragment;
import com.ping.android.presentation.presenters.ConversationListPresenter;
import com.ping.android.presentation.presenters.impl.ConversationListPresenterImpl;
import com.ping.android.presentation.view.fragment.ConversationFragment;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;

@Module
public abstract class ConversationViewModule {
    @Binds
    abstract ConversationListPresenter.View provideView(ConversationFragment fragment);

    @Provides
    @PerFragment
    static ConversationListPresenter provideConversationPresenter(ConversationListPresenterImpl presenter) {
        return presenter;
    }
}
