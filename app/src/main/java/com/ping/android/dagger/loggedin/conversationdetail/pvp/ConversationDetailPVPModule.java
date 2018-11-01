package com.ping.android.dagger.loggedin.conversationdetail.pvp;

import com.ping.android.dagger.scopes.PerFragment;
import com.ping.android.presentation.presenters.ConversationPVPDetailPresenter;
import com.ping.android.presentation.presenters.impl.ConversationPVPDetailPresenterImpl;
import com.ping.android.presentation.view.fragment.ConversationPVPDetailFragment;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;

/**
 * Created by tuanluong on 1/31/18.
 */
@Module
public abstract class ConversationDetailPVPModule {

    @PerFragment
    @Provides
    static ConversationPVPDetailPresenter providePresenter(ConversationPVPDetailPresenterImpl presenter) {
        return presenter;
    }

    @Binds
    abstract ConversationPVPDetailPresenter.View provideView(ConversationPVPDetailFragment fragment);
}
