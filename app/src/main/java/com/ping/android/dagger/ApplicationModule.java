package com.ping.android.dagger;

import com.bzzzchat.cleanarchitecture.JobExecutor;
import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UIThread;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by tuanluong on 1/23/18.
 */

@Module
public class ApplicationModule {
    @Provides
    @Singleton
    public PostExecutionThread provideUiThread() {
        return new UIThread();
    }

    @Provides
    @Singleton
    public ThreadExecutor provideThreadExecutor() {
        return new JobExecutor();
    }
}
