package com.ping.android.dagger.loggedin;

import com.ping.android.dagger.loggedin.addcontact.AddContactComponent;
import com.ping.android.dagger.loggedin.main.MainComponent;
import com.ping.android.dagger.loggedin.main.MainModule;
import com.ping.android.dagger.loggedin.newchat.NewChatComponent;
import com.ping.android.dagger.loggedin.newchat.NewChatModule;
import com.ping.android.dagger.loggedin.newgroup.NewGroupComponent;
import com.ping.android.dagger.loggedin.newgroup.NewGroupModule;
import com.ping.android.dagger.scopes.LoggedIn;

import dagger.Subcomponent;

/**
 * Created by tuanluong on 1/23/18.
 */
@LoggedIn
@Subcomponent(modules = { RepositoryModule.class })
public interface LoggedInComponent {
    MainComponent provideMainComponent(MainModule mainModule);

    NewChatComponent provideNewChatComponent(NewChatModule module, SearchUserModule searchUserModule);

    NewGroupComponent provideNewGroupComponent(NewGroupModule newGroupModule, SearchUserModule searchUserModule);

    AddContactComponent provideAddContactComponent(SearchUserModule searchUserModule);
}
