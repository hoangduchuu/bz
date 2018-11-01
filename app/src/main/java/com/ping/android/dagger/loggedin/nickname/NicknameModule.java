package com.ping.android.dagger.loggedin.nickname;

import com.ping.android.dagger.scopes.PerActivity;
import com.ping.android.presentation.presenters.NicknamePresenter;
import com.ping.android.presentation.presenters.impl.NicknamePresenterImpl;
import com.ping.android.presentation.view.activity.NicknameActivity;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;

/**
 * Created by tuanluong on 1/25/18.
 */

@Module
public abstract class NicknameModule {
    @Binds
    public abstract NicknamePresenter.View provideView(NicknameActivity activity);

    @Provides
    @PerActivity
    public static NicknamePresenter provideAddGroupPresenter(NicknamePresenterImpl presenter) {
        return presenter;
    }
}
