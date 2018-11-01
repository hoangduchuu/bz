package com.ping.android.domain.usecase;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.domain.repository.UserRepository;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 2/27/18.
 */

public class ObserveUserStatusUseCase extends UseCase<Boolean, String> {
    @Inject
    UserRepository userRepository;

    @Inject
    public ObserveUserStatusUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<Boolean> buildUseCaseObservable(String userId) {
        return userRepository.observeUserStatus(userId)
                .map(dataSnapshot -> {
                    boolean isOnline = false;
                    if (dataSnapshot.exists()) {
                        Map<String, Double> devices = (Map<String, Double>) dataSnapshot.getValue();
                        if (devices != null) {
                            for (String key : devices.keySet()) {
                                double timestamp = devices.get(key);
                                if ((System.currentTimeMillis() - (timestamp * 1000)) < 3600000 * 24) {
                                    isOnline = true;
                                }
                            }
                        }
                    }
                    return isOnline;
                });
    }
}
