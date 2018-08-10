package com.ping.android.dagger.loggedin.newchat;

import com.ping.android.dagger.scopes.PerActivity;
import com.ping.android.presentation.presenters.NewChatPresenter;
import com.ping.android.presentation.presenters.SearchUserPresenter;
import com.ping.android.presentation.presenters.impl.NewChatPresenterImpl;
import com.ping.android.presentation.view.activity.NewChatActivity;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;

/**
 * Created by tuanluong on 1/23/18.
 */
@Module
public abstract class NewChatModule {

    @Binds
    public abstract NewChatPresenter.NewChatView provideView(NewChatActivity activity);

    @Binds
    public abstract SearchUserPresenter.View provideSearchView(NewChatActivity activity);

    @Provides
    @PerActivity
    public static NewChatPresenter provideNewChatPresenter(NewChatPresenterImpl presenter) {
        return presenter;
    }
}
