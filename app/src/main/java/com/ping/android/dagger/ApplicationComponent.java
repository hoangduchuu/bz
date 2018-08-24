package com.ping.android.dagger;

import com.ping.android.App;
import com.ping.android.managers.UserManager;
import com.ping.android.service.CallService;
import com.ping.android.service.FbMessagingService;
import com.ping.android.service.NotificationBroadcastReceiver;
import com.ping.android.utils.NetworkConnectionChecker;
import com.ping.android.utils.bus.BusProvider;

import org.jetbrains.annotations.NotNull;

import javax.inject.Singleton;

import dagger.Component;
import dagger.android.support.AndroidSupportInjectionModule;

/**
 * Created by tuanluong on 1/23/18.
 */
@Singleton
@Component(modules = {
        AndroidSupportInjectionModule.class,
        ApplicationModule.class,
        RepositoryModule.class,
        BuildersModule.class
})
public interface ApplicationComponent {
//    @Component.Builder
//    interface Builder {
//        @BindsInstance
//        Builder application(App app);
//
//        Builder applicationModule(ApplicationModule applicationModule);
//
//        ApplicationComponent build();
//    }

    void inject(App app);

    void inject(CallService service);

    void inject(NotificationBroadcastReceiver receiver);

    void inject(@NotNull FbMessagingService fbMessagingService);

    UserManager provideUserManager();

    BusProvider provideBusProvider();

    NetworkConnectionChecker provideNetworkConnectionChecker();
}
