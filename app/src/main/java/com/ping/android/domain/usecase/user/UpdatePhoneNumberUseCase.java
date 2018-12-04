package com.ping.android.domain.usecase.user;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.bzzzchat.cleanarchitecture.UseCaseWithTimeOut;
import com.ping.android.domain.repository.UserRepository;
import com.ping.android.managers.UserManager;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import io.reactivex.Observable;

public class UpdatePhoneNumberUseCase extends UseCaseWithTimeOut<Boolean, String> {
    @Inject
    UserRepository userRepository;
    @Inject
    UserManager userManager;

    @Inject
    public UpdatePhoneNumberUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<Boolean> buildUseCaseObservable(String s) {
        return userManager.getCurrentUser()
                .flatMap(user -> userRepository.updatePhoneNumber(user.key, s));
    }
}
