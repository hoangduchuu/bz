package com.ping.android.domain.usecase;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.domain.repository.UserRepository;
import com.ping.android.managers.UserManager;
import com.ping.android.model.User;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 2/7/18.
 */

public class ObserveCurrentUserUseCase extends UseCase<User, Void> {
    @Inject
    UserRepository userRepository;

    @Inject
    public ObserveCurrentUserUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<User> buildUseCaseObservable(Void aVoid) {
        return userRepository.observeCurrentUser()
                .doOnNext(user -> UserManager.getInstance().setUser(user));
    }
}
