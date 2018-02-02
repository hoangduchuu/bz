package com.ping.android.dagger.loggedin.conversationdetail.group;

import com.ping.android.dagger.scopes.PerFragment;
import com.ping.android.presentation.view.fragment.ConversationGroupDetailFragment;
import com.ping.android.presentation.view.fragment.ConversationPVPDetailFragment;

import dagger.Subcomponent;

/**
 * Created by tuanluong on 1/31/18.
 */
@PerFragment
@Subcomponent(modules = { ConversationDetailGroupModule.class })
public interface ConversationDetailGroupComponent {
    void inject(ConversationGroupDetailFragment fragment);
}
