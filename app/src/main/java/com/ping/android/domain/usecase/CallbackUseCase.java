package com.ping.android.domain.usecase;

import android.util.Pair;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.domain.repository.UserRepository;
import com.ping.android.managers.UserManager;
import com.ping.android.model.User;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import io.reactivex.Observable;

public class CallbackUseCase extends UseCase<Pair<User, User>, String> {
    @Inject
    UserRepository userRepository;
    @Inject
    UserManager userManager;

    @Inject
    public CallbackUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<Pair<User, User>> buildUseCaseObservable(String s) {
        return userManager.getCurrentUser()
                .zipWith(userRepository.getUser(s), Pair::create);
    }
}
