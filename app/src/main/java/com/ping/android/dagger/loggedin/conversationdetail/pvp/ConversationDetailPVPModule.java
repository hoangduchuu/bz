package com.ping.android.dagger.loggedin.conversationdetail.pvp;

import com.ping.android.dagger.scopes.PerFragment;
import com.ping.android.presentation.presenters.ConversationPVPDetailPresenter;
import com.ping.android.presentation.presenters.impl.ConversationPVPDetailPresenterImpl;

import dagger.Module;
import dagger.Provides;

/**
 * Created by tuanluong on 1/31/18.
 */
@Module
public class ConversationDetailPVPModule {
    private final ConversationPVPDetailPresenter.View view;

    public ConversationDetailPVPModule(ConversationPVPDetailPresenter.View view) {
        this.view = view;
    }

    @PerFragment
    @Provides
    ConversationPVPDetailPresenter providePresenter(ConversationPVPDetailPresenterImpl presenter) {
        return presenter;
    }

    @PerFragment
    @Provides
    ConversationPVPDetailPresenter.View provideView() {
        return view;
    }
}
