package com.ping.android.domain.usecase.auth;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.bzzzchat.cleanarchitecture.UseCaseWithTimeOut;
import com.ping.android.domain.repository.SearchRepository;
import com.ping.android.domain.repository.UserRepository;
import com.ping.android.domain.usecase.InitializeUserUseCase;
import com.ping.android.model.User;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import io.reactivex.Observable;

public class AuthenticateUseCase extends UseCase<User, AuthenticateUseCase.Params> {
    @Inject
    SearchRepository searchRepository;
    @Inject
    UserRepository userRepository;
    @Inject
    InitializeUserUseCase initializeUserUseCase;

    @Inject
    public AuthenticateUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<User> buildUseCaseObservable(Params params) {
        return userRepository.checkValidUser(params.userName)
                .flatMap(user -> userRepository.loginByEmail(user.email, params.password)
                        .flatMap(user1 -> initializeUserUseCase.buildUseCaseObservable(null)
                                .map(aBoolean -> user)));
    }

    public static class Params {
        private String userName;
        private String password;

        public Params(String userName, String password) {
            this.userName = userName;
            this.password = password;
        }
    }
}
