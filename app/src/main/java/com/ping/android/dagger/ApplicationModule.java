package com.ping.android.dagger;

import android.app.Application;

import com.bzzzchat.cleanarchitecture.JobExecutor;
import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UIThread;
import com.ping.android.device.Device;
import com.ping.android.device.ImageStorage;
import com.ping.android.device.Notification;
import com.ping.android.device.impl.DeviceImpl;
import com.ping.android.device.impl.ImageStorageImpl;
import com.ping.android.device.impl.NotificationImpl;
import com.ping.android.domain.repository.NotificationMessageRepository;
import com.ping.android.managers.UserManager;
import com.ping.android.model.NotificationMessage;
import com.ping.android.service.CallServiceHandler;
import com.ping.android.service.CallServiceHandlerImpl;

import javax.inject.Inject;
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

    @Provides
    @Singleton
    public Notification provideNotification(NotificationMessageRepository notificationMessageRepository) {
        return new NotificationImpl(application, notificationMessageRepository);
    }

    @Provides
    @Singleton
    public CallServiceHandler provideCallServiceHandler(CallServiceHandlerImpl handler) {
        return handler;
    }

    @Provides
    @Singleton
    public ImageStorage provideImageStorgate() {
        return new ImageStorageImpl(application);
    }
}
