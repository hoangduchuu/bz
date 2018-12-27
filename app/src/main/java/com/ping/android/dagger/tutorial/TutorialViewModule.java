package com.ping.android.dagger.tutorial;

import com.ping.android.presentation.view.tutorial.activity.TutorialActivity;
import com.ping.android.presentation.view.tutorial.activity.TutorialContract;

import dagger.Binds;
import dagger.Module;
/**
 * Created by Huu Hoang on 27/12/2018
 */
@Module
public abstract class TutorialViewModule {
    @Binds
    abstract TutorialContract.View provideSplashView(TutorialActivity activity);
}
