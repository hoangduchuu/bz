package com.ping.android.dagger.loggedin;

import com.ping.android.dagger.loggedin.SearchUserModule;
import com.ping.android.dagger.loggedin.addcontact.AddContactComponent;
import com.ping.android.dagger.loggedin.chat.ChatComponent;
import com.ping.android.dagger.loggedin.chat.ChatModule;
import com.ping.android.dagger.loggedin.conversationdetail.ConversationDetailComponent;
import com.ping.android.dagger.loggedin.main.MainComponent;
import com.ping.android.dagger.loggedin.main.MainModule;
import com.ping.android.dagger.loggedin.newchat.NewChatComponent;
import com.ping.android.dagger.loggedin.newchat.NewChatModule;
import com.ping.android.dagger.loggedin.newgroup.NewGroupComponent;
import com.ping.android.dagger.loggedin.newgroup.NewGroupModule;
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

    AddContactComponent provideAddContactComponent(SearchUserModule searchUserModule);

    ConversationDetailComponent provideConversationDetailComponent();

    TransphabetComponent provideTransphabetComponent();

    ChatComponent provideChatComponent(ChatModule chatModule);

    UserDetailComponent provideUserDetailComponent(UserDetailModule module);
}
