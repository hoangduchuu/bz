package com.ping.android.domain.usecase;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.domain.repository.UserRepository;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 3/21/18.
 */

public class ToggleUserNotificationSettingUseCase extends UseCase<Boolean, Boolean> {
    @Inject
    UserRepository userRepository;

    @Inject
    public ToggleUserNotificationSettingUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<Boolean> buildUseCaseObservable(Boolean aBoolean) {
        return userRepository.getCurrentUser()
                .flatMap(user -> userRepository.updateUserNotificationSetting(user.key, aBoolean));
    }
}
