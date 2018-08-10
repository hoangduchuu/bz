package com.ping.android.dagger.loggedin.conversationdetail.group;

import com.ping.android.dagger.scopes.PerFragment;
import com.ping.android.presentation.presenters.ConversationGroupDetailPresenter;
import com.ping.android.presentation.presenters.ConversationPVPDetailPresenter;
import com.ping.android.presentation.presenters.impl.ConversationGroupDetailPresenterImpl;
import com.ping.android.presentation.presenters.impl.ConversationPVPDetailPresenterImpl;
import com.ping.android.presentation.view.fragment.ConversationGroupDetailFragment;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;

/**
 * Created by tuanluong on 1/31/18.
 */
@Module
public abstract class ConversationDetailGroupModule {
    @PerFragment
    @Provides
    static ConversationGroupDetailPresenter providePresenter(ConversationGroupDetailPresenterImpl presenter) {
        return presenter;
    }

    @Binds
    abstract ConversationGroupDetailPresenter.View provideView(ConversationGroupDetailFragment fragment);
}
