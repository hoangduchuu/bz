package com.ping.android.dagger.tutorial.more;

import com.ping.android.dagger.scopes.PerFragment;
import com.ping.android.presentation.view.tutorial.more.TutoMoreContract;
import com.ping.android.presentation.view.tutorial.more.TutoMoreFragment;
import com.ping.android.presentation.view.tutorial.more.TutoMorePresenter;
import com.ping.android.presentation.view.tutorial.shake.TutoShakeContract;
import com.ping.android.presentation.view.tutorial.shake.TutoShakeFragment;
import com.ping.android.presentation.view.tutorial.shake.TutoShakePresenter;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
/**
 * Created by Huu Hoang on 28/12/2018
 */
@Module
public abstract class MoreViewModule {
    @Binds
    abstract TutoMoreContract.View provideView(TutoMoreFragment fragment);

    @Provides
    @PerFragment
    static TutoMoreContract.Presenter provideMoreTutorialPresenter(TutoMorePresenter presenter) {
        return presenter;
    }
}
