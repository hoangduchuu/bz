package com.ping.android.domain.usecase;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.device.Device;
import com.ping.android.device.Notification;
import com.ping.android.domain.repository.QuickbloxRepository;
import com.ping.android.domain.repository.UserRepository;
import com.ping.android.managers.UserManager;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 2/8/18.
 */

public class LogoutUseCase extends UseCase<Boolean, Void> {
    @Inject
    UserRepository userRepository;
    @Inject
    QuickbloxRepository quickbloxRepository;
    @Inject
    Device device;
    @Inject
    Notification notification;
    @Inject
    UserManager userManager;

    @Inject
    public LogoutUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<Boolean> buildUseCaseObservable(Void aVoid) {
        return userManager.getCurrentUser()
                .flatMap(user -> {
                    userManager.logout();
                    String deviceId = device.getDeviceId();
                    notification.clearAll();
                    return userRepository.logout(user.key, deviceId)
                            .flatMap(aBoolean -> quickbloxRepository.signOut());
                });
    }
}
