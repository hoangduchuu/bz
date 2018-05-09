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
    SearchUserPresenter.View view;

    public SearchUserModule(SearchUserPresenter.View view) {
        this.view = view;
    }

    @Provides
    @PerActivity
    public SearchUserPresenter.View provideView() {
        return view;
    }

    @Provides
    @PerActivity
    public SearchUserPresenter provideNewChatPresenter(SearchUserPresenterImpl presenter) {
        return presenter;
    }
}
