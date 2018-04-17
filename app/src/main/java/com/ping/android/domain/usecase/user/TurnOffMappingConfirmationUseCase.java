package com.ping.android.domain.usecase.user;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.domain.repository.UserRepository;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import io.reactivex.Observable;

public class TurnOffMappingConfirmationUseCase extends UseCase<Boolean, Void> {
    @Inject
    UserRepository userRepository;

    @Inject
    public TurnOffMappingConfirmationUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<Boolean> buildUseCaseObservable(Void aVoid) {
        return userRepository.getCurrentUser()
                .flatMap(user -> userRepository.turnOffMappingConfirmation(user.key));
    }
}
