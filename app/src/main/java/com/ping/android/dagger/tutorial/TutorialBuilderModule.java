package com.ping.android.dagger.tutorial;

import com.ping.android.dagger.loggedin.main.call.CallListModule;
import com.ping.android.dagger.loggedin.main.contact.ContactModule;
import com.ping.android.dagger.loggedin.main.conversation.ConversationViewModule;
import com.ping.android.dagger.loggedin.main.group.GroupModule;
import com.ping.android.dagger.loggedin.main.profile.ProfileModule;
import com.ping.android.dagger.scopes.PerFragment;
import com.ping.android.dagger.tutorial.more.MoreViewModule;
import com.ping.android.dagger.tutorial.shake.ShakeViewModule;
import com.ping.android.dagger.tutorial.typing.TypingViewModule;
import com.ping.android.dagger.tutorial.voice.VoiceViewModule;
import com.ping.android.presentation.view.fragment.CallFragment;
import com.ping.android.presentation.view.fragment.ContactFragment;
import com.ping.android.presentation.view.fragment.ConversationFragment;
import com.ping.android.presentation.view.fragment.GroupFragment;
import com.ping.android.presentation.view.fragment.ProfileFragment;
import com.ping.android.presentation.view.tutorial.more.TutoMoreFragment;
import com.ping.android.presentation.view.tutorial.shake.TutoShakeFragment;
import com.ping.android.presentation.view.tutorial.type.TutoTyingFragment;
import com.ping.android.presentation.view.tutorial.voice.TutoVoiceFragment;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
/**
 * Created by Huu Hoang on 28/12/2018
 */
@Module
public abstract class TutorialBuilderModule {
    @PerFragment
    @ContributesAndroidInjector(modules = {MoreViewModule.class})
    public abstract TutoMoreFragment bindTutoMoreFragment();

    @PerFragment
    @ContributesAndroidInjector(modules = {ShakeViewModule.class})
    public abstract TutoShakeFragment bindTutoShakeFragment();

    @PerFragment
    @ContributesAndroidInjector(modules = TypingViewModule.class)
    public abstract TutoTyingFragment bindTutoTyingFragment();

    @PerFragment
    @ContributesAndroidInjector(modules = VoiceViewModule.class)
    public abstract TutoVoiceFragment bindTutoVoiceFragment();

}
