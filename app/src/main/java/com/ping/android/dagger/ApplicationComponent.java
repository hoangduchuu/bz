package com.ping.android.dagger;

import com.ping.android.dagger.loggedin.LoggedInComponent;
import com.ping.android.dagger.loggedout.LoggedOutComponent;
import com.ping.android.service.CallService;
import com.ping.android.service.FbMessagingService;
import com.ping.android.service.NotificationBroadcastReceiver;
import com.ping.android.service.PushNotificationBroadcastReceiver;

import org.jetbrains.annotations.NotNull;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by tuanluong on 1/23/18.
 */
@Singleton
@Component(modules = {ApplicationModule.class, RepositoryModule.class})
public interface ApplicationComponent {
    LoggedInComponent provideLoggedInComponent();

    LoggedOutComponent provideLoggedOutComponent();

    void inject(CallService service);

    void inject(NotificationBroadcastReceiver receiver);

    void inject(PushNotificationBroadcastReceiver receiver);

    void inject(@NotNull FbMessagingService fbMessagingService);
}
