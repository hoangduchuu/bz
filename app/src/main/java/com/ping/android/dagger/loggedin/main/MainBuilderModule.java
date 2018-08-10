package com.ping.android.dagger.loggedin.main;

import com.ping.android.dagger.loggedin.main.call.CallListModule;
import com.ping.android.dagger.loggedin.main.contact.ContactModule;
import com.ping.android.dagger.loggedin.main.conversation.ConversationViewModule;
import com.ping.android.dagger.loggedin.main.group.GroupModule;
import com.ping.android.dagger.loggedin.main.profile.ProfileModule;
import com.ping.android.dagger.scopes.PerFragment;
import com.ping.android.presentation.view.fragment.CallFragment;
import com.ping.android.presentation.view.fragment.ContactFragment;
import com.ping.android.presentation.view.fragment.ConversationFragment;
import com.ping.android.presentation.view.fragment.GroupFragment;
import com.ping.android.presentation.view.fragment.ProfileFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

@Module
public abstract class MainBuilderModule {
    @PerFragment
    @ContributesAndroidInjector(modules = {ConversationViewModule.class})
    public abstract ConversationFragment bindConversationFragment();

    @PerFragment
    @ContributesAndroidInjector(modules = {CallListModule.class})
    public abstract CallFragment bindCallFragment();

    @PerFragment
    @ContributesAndroidInjector(modules = GroupModule.class)
    public abstract GroupFragment bindGroupFragment();

    @PerFragment
    @ContributesAndroidInjector(modules = ContactModule.class)
    public abstract ContactFragment bindContactFragment();

    @PerFragment
    @ContributesAndroidInjector(modules = ProfileModule.class)
    public abstract ProfileFragment bindProfileFragment();
}
