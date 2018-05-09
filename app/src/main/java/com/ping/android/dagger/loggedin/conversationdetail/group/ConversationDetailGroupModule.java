package com.ping.android.dagger.loggedin.conversationdetail.group;

import com.ping.android.dagger.scopes.PerFragment;
import com.ping.android.presentation.presenters.ConversationGroupDetailPresenter;
import com.ping.android.presentation.presenters.ConversationPVPDetailPresenter;
import com.ping.android.presentation.presenters.impl.ConversationGroupDetailPresenterImpl;
import com.ping.android.presentation.presenters.impl.ConversationPVPDetailPresenterImpl;

import dagger.Module;
import dagger.Provides;

/**
 * Created by tuanluong on 1/31/18.
 */
@Module
public class ConversationDetailGroupModule {
    private final ConversationGroupDetailPresenter.View view;

    public ConversationDetailGroupModule(ConversationGroupDetailPresenter.View view) {
        this.view = view;
    }

    @PerFragment
    @Provides
    ConversationGroupDetailPresenter providePresenter(ConversationGroupDetailPresenterImpl presenter) {
        return presenter;
    }

    @PerFragment
    @Provides
    ConversationGroupDetailPresenter.View provideView() {
        return view;
    }
}
