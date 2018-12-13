package com.ping.android.domain.usecase;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.device.Device;
import com.ping.android.domain.repository.QuickbloxRepository;
import com.ping.android.domain.repository.UserRepository;
import com.ping.android.managers.UserManager;
import com.ping.android.model.User;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 2/6/18.
 */

public class InitializeUserUseCase extends UseCase<Boolean, Void> {
    @Inject
    UserRepository userRepository;
    @Inject
    QuickbloxRepository quickbloxRepository;
    @Inject
    Device device;
    @Inject
    UserManager userManager;

    @Inject
    public InitializeUserUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<Boolean> buildUseCaseObservable(Void aVoid) {
        return userRepository.initializeUser()
                .map(user -> {
                    String deviceId = device.getDeviceId();
                    user.devices.put(deviceId, ((double) System.currentTimeMillis() / 1000));
                    updateDevicesId(user);
                    userManager.setUser(user);
                    return user;
                })
                .doOnNext(userManager::setUser)
                .map(qbUser -> true);
    }

    private void updateDevicesId(User user) {
        userRepository.updateDeviceIds(user.devices)
                .subscribe();
    }
}
