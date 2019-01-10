package com.ping.android.dagger.tutorial;

import com.ping.android.dagger.scopes.PerActivity;
import com.ping.android.presentation.view.tutorial.activity.TutorialActivity;
import com.ping.android.presentation.view.tutorial.activity.TutorialContract;
import com.ping.android.presentation.view.tutorial.activity.TutorialPresenter;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;
/**
 * Created by Huu Hoang on 27/12/2018
 */
@Module
public abstract class TutorialModule {

    @Provides
    @PerActivity
    static TutorialContract.Presenter providePresenter(TutorialPresenter presenter) {
        return presenter;
    }
}
