package com.ping.android.dagger.loggedin;

import com.ping.android.dagger.loggedin.addphone.AddPhoneComponent;
import com.ping.android.dagger.loggedin.addphone.AddPhoneModule;
import com.ping.android.dagger.loggedin.call.CallComponent;
import com.ping.android.dagger.loggedin.call.CallModule;
import com.ping.android.dagger.loggedin.game.GameComponent;
import com.ping.android.dagger.loggedin.game.GameModule;
import com.ping.android.dagger.loggedin.groupimage.GroupImageComponent;
import com.ping.android.dagger.loggedin.groupimage.GroupImageModule;
import com.ping.android.dagger.scopes.LoggedIn;

import dagger.Subcomponent;

/**
 * Created by tuanluong on 1/23/18.
 */
@LoggedIn
@Subcomponent
public interface LoggedInComponent {

    CallComponent provideCallComponent(CallModule module);

    GameComponent provideGameComponent(GameModule gameModule);

    AddPhoneComponent provideAddPhoneComponent(AddPhoneModule module);

    GroupImageComponent provideGroupImageComponent(GroupImageModule module);
}
