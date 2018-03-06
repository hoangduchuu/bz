package com.ping.android.domain.usecase;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.device.Device;
import com.ping.android.domain.repository.QuickbloxRepository;
import com.ping.android.domain.repository.UserRepository;
import com.ping.android.managers.UserManager;
import com.ping.android.model.User;
import com.ping.android.ultility.Constant;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

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
    UserManager userManager;

    @Inject
    public InitializeUserUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
        userManager = UserManager.getInstance();
    }

    @NotNull
    @Override
    public Observable<Boolean> buildUseCaseObservable(Void aVoid) {
        return userRepository.initializeUser()
                .flatMap(user -> {
                    String deviceId = device.getDeviceId();
                    user.devices.put(deviceId, ((double) System.currentTimeMillis() / 1000));
                    updateDevicesId(user);
                    return userRepository.getUserList(user.friends)
                            .flatMap(users -> {
                                user.friendList = new ArrayList<>(users);
                                userManager.setUser(user);
                                if (user.quickBloxID > 0) {
                                    return quickbloxRepository.signIn(user.quickBloxID, user.pingID);
                                } else {
                                    return quickbloxRepository.signUp(user.pingID)
                                            .flatMap(qbUser -> {
                                                user.quickBloxID = qbUser.getId();
                                                return userRepository.updateQuickbloxId(qbUser.getId())
                                                        .map(aBoolean1 -> qbUser);
                                            });
                                }
                            });
                })
                .doOnNext(qbUser -> userManager.setQbUser(qbUser))
                .map(qbUser -> true);
    }

    private void updateDevicesId(User user) {
        userRepository.updateDeviceId(user.devices)
                .subscribe();
    }
}
