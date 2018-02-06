package com.ping.android.dagger;

import android.app.Application;

import com.bzzzchat.cleanarchitecture.JobExecutor;
import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UIThread;
import com.ping.android.device.Device;
import com.ping.android.device.impl.DeviceImpl;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by tuanluong on 1/23/18.
 */

@Module
public class ApplicationModule {
    private Application application;

    public ApplicationModule(Application application) {
        this.application = application;
    }

    @Provides
    @Singleton
    public Application provideApplication() {
        return application;
    }

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

    @Provides
    @Singleton
    public Device provideDevice(DeviceImpl device) {
        return device;
    }
}
