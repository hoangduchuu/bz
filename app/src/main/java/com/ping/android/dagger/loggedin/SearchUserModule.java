package com.ping.android.dagger.loggedin;

import com.ping.android.dagger.scopes.PerActivity;
import com.ping.android.presentation.presenters.SearchUserPresenter;
import com.ping.android.presentation.presenters.impl.SearchUserPresenterImpl;

import dagger.Module;
import dagger.Provides;

/**
 * Created by tuanluong on 1/25/18.
 */
@Module
public class SearchUserModule {

    @Provides
    @PerActivity
    public SearchUserPresenter provideNewChatPresenter(SearchUserPresenterImpl presenter) {
        return presenter;
    }
}
