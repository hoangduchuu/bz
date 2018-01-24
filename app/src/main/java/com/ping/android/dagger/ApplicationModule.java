package com.ping.android.dagger;

import com.tl.cleanarchitecture.JobExecutor;
import com.tl.cleanarchitecture.PostExecutionThread;
import com.tl.cleanarchitecture.ThreadExecutor;
import com.tl.cleanarchitecture.UIThread;

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
