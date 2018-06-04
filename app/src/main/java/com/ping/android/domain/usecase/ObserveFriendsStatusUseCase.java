package com.ping.android.domain.usecase;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.domain.repository.UserRepository;
import com.ping.android.managers.UserManager;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 3/14/18.
 */

public class ObserveFriendsStatusUseCase extends UseCase<Boolean, Void> {
    @Inject
    UserRepository userRepository;
    @Inject
    UserManager userManager;

    @Inject
    public ObserveFriendsStatusUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<Boolean> buildUseCaseObservable(Void params) {
        return userManager.getCurrentUser()
                .flatMap(user -> userRepository
                        .observeFriendsValue(user.key)
                        .doOnNext(userManager::setFriendsData)
                        .map(map -> true));
    }
}
