package com.ping.android.dagger.loggedin.conversationdetail;

import com.ping.android.dagger.loggedin.conversationdetail.background.BackgroundModule;
import com.ping.android.dagger.loggedin.conversationdetail.group.ConversationDetailGroupModule;
import com.ping.android.dagger.loggedin.conversationdetail.pvp.ConversationDetailPVPModule;
import com.ping.android.dagger.scopes.PerActivity;
import com.ping.android.dagger.scopes.PerFragment;
import com.ping.android.presentation.view.fragment.BackgroundFragment;
import com.ping.android.presentation.view.fragment.ConversationGroupDetailFragment;
import com.ping.android.presentation.view.fragment.ConversationPVPDetailFragment;
import com.ping.android.utils.Navigator;

import dagger.Module;
import dagger.Provides;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class ConversationDetailModule {
    public ConversationDetailModule() {}

    @Provides
    @PerActivity
    public static Navigator provideNavigator() {
        return new Navigator();
    }

    @PerFragment
    @ContributesAndroidInjector(modules = ConversationDetailGroupModule.class)
    abstract ConversationGroupDetailFragment bindConversationGroupDetailFragment();

    @PerFragment
    @ContributesAndroidInjector(modules = ConversationDetailPVPModule.class)
    abstract ConversationPVPDetailFragment bindConversationPVPDetailFragment();

    @PerFragment
    @ContributesAndroidInjector(modules = BackgroundModule.class)
    abstract BackgroundFragment bindBackgroundFragment();
}
