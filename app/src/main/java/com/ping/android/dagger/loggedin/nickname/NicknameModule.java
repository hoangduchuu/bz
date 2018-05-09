package com.ping.android.dagger.loggedin.nickname;

import com.ping.android.dagger.scopes.PerActivity;
import com.ping.android.presentation.presenters.AddGroupPresenter;
import com.ping.android.presentation.presenters.NicknamePresenter;
import com.ping.android.presentation.presenters.impl.AddGroupPresenterImpl;
import com.ping.android.presentation.presenters.impl.NicknamePresenterImpl;

import dagger.Module;
import dagger.Provides;

/**
 * Created by tuanluong on 1/25/18.
 */

@Module
public class NicknameModule {
    public NicknamePresenter.View view;

    public NicknameModule(NicknamePresenter.View view) {
        this.view = view;
    }

    @Provides
    @PerActivity
    public NicknamePresenter.View provideView() {
        return view;
    }

    @Provides
    @PerActivity
    public NicknamePresenter provideAddGroupPresenter(NicknamePresenterImpl presenter) {
        return presenter;
    }
}
