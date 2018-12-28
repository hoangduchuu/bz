package com.ping.android.dagger.tutorial.shake;

import com.ping.android.dagger.scopes.PerFragment;
import com.ping.android.presentation.presenters.ConversationListPresenter;
import com.ping.android.presentation.presenters.impl.ConversationListPresenterImpl;
import com.ping.android.presentation.view.fragment.ConversationFragment;
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
public abstract class ShakeViewModule {
    @Binds
    abstract TutoShakeContract.View provideView(TutoShakeFragment fragment);

    @Provides
    @PerFragment
    static TutoShakeContract.Presenter provideShakePresenter(TutoShakePresenter presenter) {
        return presenter;
    }
}
