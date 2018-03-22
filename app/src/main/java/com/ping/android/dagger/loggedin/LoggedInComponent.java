package com.ping.android.dagger.loggedin;

import com.ping.android.dagger.loggedin.addcontact.AddContactComponent;
import com.ping.android.dagger.loggedin.addcontact.AddContactModule;
import com.ping.android.dagger.loggedin.blockcontact.BlockContactComponent;
import com.ping.android.dagger.loggedin.blockcontact.BlockContactModule;
import com.ping.android.dagger.loggedin.call.CallComponent;
import com.ping.android.dagger.loggedin.call.CallModule;
import com.ping.android.dagger.loggedin.chat.ChatComponent;
import com.ping.android.dagger.loggedin.chat.ChatModule;
import com.ping.android.dagger.loggedin.conversationdetail.ConversationDetailComponent;
import com.ping.android.dagger.loggedin.main.MainComponent;
import com.ping.android.dagger.loggedin.main.MainModule;
import com.ping.android.dagger.loggedin.transphabet.manualmapping.ManualMappingComponent;
import com.ping.android.dagger.loggedin.transphabet.manualmapping.ManualMappingModule;
import com.ping.android.dagger.loggedin.newchat.NewChatComponent;
import com.ping.android.dagger.loggedin.newchat.NewChatModule;
import com.ping.android.dagger.loggedin.newgroup.NewGroupComponent;
import com.ping.android.dagger.loggedin.newgroup.NewGroupModule;
import com.ping.android.dagger.loggedin.selectcontact.SelectContactComponent;
import com.ping.android.dagger.loggedin.selectcontact.SelectContactModule;
import com.ping.android.dagger.loggedin.transphabet.TransphabetComponent;
import com.ping.android.dagger.loggedin.userdetail.UserDetailComponent;
import com.ping.android.dagger.loggedin.userdetail.UserDetailModule;
import com.ping.android.dagger.scopes.LoggedIn;

import dagger.Subcomponent;

/**
 * Created by tuanluong on 1/23/18.
 */
@LoggedIn
@Subcomponent
public interface LoggedInComponent {
    MainComponent provideMainComponent(MainModule mainModule);

    NewChatComponent provideNewChatComponent(NewChatModule module, SearchUserModule searchUserModule);

    NewGroupComponent provideNewGroupComponent(NewGroupModule newGroupModule, SearchUserModule searchUserModule);

    AddContactComponent provideAddContactComponent(AddContactModule contactModule, SearchUserModule searchUserModule);

    ConversationDetailComponent provideConversationDetailComponent();

    TransphabetComponent provideTransphabetComponent();

    ChatComponent provideChatComponent(ChatModule chatModule);

    UserDetailComponent provideUserDetailComponent(UserDetailModule module);

    CallComponent provideCallComponent(CallModule module);

    BlockContactComponent provideBlockContactComponent(BlockContactModule module);

    SelectContactComponent provideSelectContactComponent(SelectContactModule module);

    ManualMappingComponent provideManualMappingComponent(ManualMappingModule module);
}
