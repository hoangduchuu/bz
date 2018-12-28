package com.ping.android.dagger.tutorial.typing;

import com.ping.android.dagger.scopes.PerFragment;
import com.ping.android.presentation.view.tutorial.shake.TutoShakeContract;
import com.ping.android.presentation.view.tutorial.shake.TutoShakeFragment;
import com.ping.android.presentation.view.tutorial.shake.TutoShakePresenter;
import com.ping.android.presentation.view.tutorial.type.TutoTyingFragment;
import com.ping.android.presentation.view.tutorial.type.TutoTypingContract;
import com.ping.android.presentation.view.tutorial.type.TutoTypingPresenter;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
/**
 * Created by Huu Hoang on 28/12/2018
 */
@Module
public abstract class TypingViewModule {
    @Binds
    abstract TutoTypingContract.View provideView(TutoTyingFragment fragment);

    @Provides
    @PerFragment
    static TutoTypingContract.Presenter provideTypingPresenter(TutoTypingPresenter presenter) {
        return presenter;
    }
}
