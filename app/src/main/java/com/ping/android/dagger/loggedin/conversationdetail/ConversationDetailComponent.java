package com.ping.android.dagger.loggedin.conversationdetail;

import com.ping.android.dagger.loggedin.conversationdetail.background.BackgroundComponent;
import com.ping.android.dagger.loggedin.conversationdetail.background.BackgroundModule;
import com.ping.android.dagger.loggedin.conversationdetail.group.ConversationDetailGroupComponent;
import com.ping.android.dagger.loggedin.conversationdetail.group.ConversationDetailGroupModule;
import com.ping.android.dagger.loggedin.conversationdetail.pvp.ConversationDetailPVPModule;
import com.ping.android.dagger.loggedin.conversationdetail.pvp.ConversationDetailPVPComponent;
import com.ping.android.dagger.scopes.PerActivity;
import com.ping.android.presentation.view.activity.ConversationDetailActivity;

import dagger.Subcomponent;

/**
 * Created by tuanluong on 1/31/18.
 */
@PerActivity
@Subcomponent(modules = ConversationDetailModule.class)
public interface ConversationDetailComponent {
    ConversationDetailPVPComponent provideConversationDetailPVPComponent(ConversationDetailPVPModule module);

    ConversationDetailGroupComponent provideConversationDetailGroupComponent(ConversationDetailGroupModule module);

    BackgroundComponent provideBackgroundComponent(BackgroundModule module);

    void inject(ConversationDetailActivity conversationDetailActivity);
}
