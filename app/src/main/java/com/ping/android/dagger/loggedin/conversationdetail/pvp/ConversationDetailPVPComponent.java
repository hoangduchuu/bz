package com.ping.android.dagger.loggedin.conversationdetail.pvp;

import com.ping.android.dagger.scopes.PerFragment;
import com.ping.android.presentation.view.fragment.ConversationPVPDetailFragment;

import dagger.Subcomponent;

/**
 * Created by tuanluong on 1/31/18.
 */
@PerFragment
@Subcomponent(modules = { ConversationDetailPVPModule.class })
public interface ConversationDetailPVPComponent {
    void inject(ConversationPVPDetailFragment fragment);
}
