package com.ping.android.domain.usecase;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.domain.repository.UserRepository;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 3/14/18.
 */

public class ObserveSpecificFriendStatusUseCase extends UseCase<Boolean, String> {
    @Inject
    UserRepository userRepository;

    @Inject
    public ObserveSpecificFriendStatusUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<Boolean> buildUseCaseObservable(String userId) {
        return userRepository.getCurrentUser()
                .flatMap(user -> userRepository
                        .observeFriendStatus(user.key, userId));
    }
}
