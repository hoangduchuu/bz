package com.ping.android.dagger.loggedin.newchat;

import com.ping.android.dagger.scopes.PerActivity;
import com.ping.android.presentation.presenters.NewChatPresenter;
import com.ping.android.presentation.presenters.impl.NewChatPresenterImpl;

import dagger.Module;
import dagger.Provides;

/**
 * Created by tuanluong on 1/23/18.
 */
@Module
public class NewChatModule {
    NewChatPresenter.NewChatView view;

    public NewChatModule(NewChatPresenter.NewChatView view) {
        this.view = view;
    }

    @Provides
    @PerActivity
    public NewChatPresenter.NewChatView provideView() {
        return view;
    }

    @Provides
    @PerActivity
    public NewChatPresenter provideNewChatPresenter(NewChatPresenterImpl presenter) {
        return presenter;
    }
}
