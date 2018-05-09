package com.ping.android.domain.usecase;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.domain.repository.UserRepository;
import com.ping.android.model.User;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 3/19/18.
 */

public class GetUserByQuickBloxIdUseCase extends UseCase<User, Integer> {
    @Inject
    UserRepository userRepository;

    @Inject
    public GetUserByQuickBloxIdUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<User> buildUseCaseObservable(Integer qbId) {
        return userRepository.getUserByQuickBloxId(qbId);
    }
}
