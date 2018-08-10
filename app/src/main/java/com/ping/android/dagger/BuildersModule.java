package com.ping.android.dagger;

import com.ping.android.dagger.loggedin.SearchUserModule;
import com.ping.android.dagger.loggedin.addcontact.AddContactModule;
import com.ping.android.dagger.loggedin.blockcontact.BlockContactModule;
import com.ping.android.dagger.loggedin.changepassword.ChangePasswordModule;
import com.ping.android.dagger.loggedin.chat.ChatModule;
import com.ping.android.dagger.loggedin.conversationdetail.ConversationDetailModule;
import com.ping.android.dagger.loggedin.gallery.GalleryModule;
import com.ping.android.dagger.loggedin.main.MainBuilderModule;
import com.ping.android.dagger.loggedin.main.MainModule;
import com.ping.android.dagger.loggedin.main.MainViewModule;
import com.ping.android.dagger.loggedin.newchat.NewChatModule;
import com.ping.android.dagger.loggedin.newgroup.NewGroupModule;
import com.ping.android.dagger.loggedin.nickname.NicknameModule;
import com.ping.android.dagger.loggedin.selectcontact.SelectContactModule;
import com.ping.android.dagger.loggedin.transphabet.TransphabetModule;
import com.ping.android.dagger.loggedout.login.LoginModule;
import com.ping.android.dagger.loggedout.login.LoginViewModule;
import com.ping.android.dagger.loggedout.registration.RegistrationModule;
import com.ping.android.dagger.loggedout.registration.RegistrationViewModule;
import com.ping.android.dagger.loggedout.splash.SplashModule;
import com.ping.android.dagger.scopes.PerActivity;
import com.ping.android.presentation.view.activity.AddContactActivity;
import com.ping.android.presentation.view.activity.AddGroupActivity;
import com.ping.android.presentation.view.activity.BlockActivity;
import com.ping.android.presentation.view.activity.ChangePasswordActivity;
import com.ping.android.presentation.view.activity.ChatActivity;
import com.ping.android.presentation.view.activity.ConversationDetailActivity;
import com.ping.android.presentation.view.activity.GalleryActivity;
import com.ping.android.presentation.view.activity.LoginActivity;
import com.ping.android.presentation.view.activity.MainActivity;
import com.ping.android.presentation.view.activity.NewChatActivity;
import com.ping.android.presentation.view.activity.NicknameActivity;
import com.ping.android.presentation.view.activity.RegistrationActivity;
import com.ping.android.presentation.view.activity.SelectContactActivity;
import com.ping.android.presentation.view.activity.SplashActivity;
import com.ping.android.presentation.view.activity.TransphabetActivity;

import dagger.Module;
import dagger.android.AndroidInjectionModule;
import dagger.android.ContributesAndroidInjector;

@Module(includes = AndroidInjectionModule.class)
public abstract class BuildersModule {

    @PerActivity
    @ContributesAndroidInjector(modules = {SplashModule.class})
    abstract SplashActivity bindSplashActivity();

    @PerActivity
    @ContributesAndroidInjector(modules = {RegistrationViewModule.class, RegistrationModule.class})
    abstract RegistrationActivity bindRegistrationActivity();

    @PerActivity
    @ContributesAndroidInjector(modules = {LoginViewModule.class, LoginModule.class})
    abstract LoginActivity bindLoginActivity();

    @PerActivity
    @ContributesAndroidInjector(modules = {MainViewModule.class, MainModule.class, MainBuilderModule.class})
    abstract MainActivity bindMainActivity();

    @PerActivity
    @ContributesAndroidInjector(modules = ChatModule.class)
    abstract ChatActivity bindChatActivity();

    @PerActivity
    @ContributesAndroidInjector(modules = ConversationDetailModule.class)
    abstract ConversationDetailActivity bindConversationDetailActivity();

    @PerActivity
    @ContributesAndroidInjector(modules = {NewChatModule.class, SearchUserModule.class})
    abstract NewChatActivity bindNewChatActivity();

    @PerActivity
    @ContributesAndroidInjector(modules = {NewGroupModule.class, SearchUserModule.class})
    abstract AddGroupActivity bindAddGroupActivity();

    @PerActivity
    @ContributesAndroidInjector(modules = {AddContactModule.class, SearchUserModule.class})
    abstract AddContactActivity bindAddContactActivity();

    @PerActivity
    @ContributesAndroidInjector(modules = GalleryModule.class)
    abstract GalleryActivity bindGalleryActivity();

    @PerActivity
    @ContributesAndroidInjector(modules = TransphabetModule.class)
    abstract TransphabetActivity bindTransphabetActivity();

    @PerActivity
    @ContributesAndroidInjector(modules = BlockContactModule.class)
    abstract BlockActivity bindBlockActivity();

    @PerActivity
    @ContributesAndroidInjector(modules = SelectContactModule.class)
    abstract SelectContactActivity bindSelectContactActivity();

    @PerActivity
    @ContributesAndroidInjector(modules = NicknameModule.class)
    abstract NicknameActivity bindNicknameActivity();

    @PerActivity
    @ContributesAndroidInjector(modules = ChangePasswordModule.class)
    abstract ChangePasswordActivity bindChangePasswordActivity();
}
