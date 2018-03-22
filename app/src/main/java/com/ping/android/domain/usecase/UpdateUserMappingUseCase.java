package com.ping.android.domain.usecase;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.domain.repository.UserRepository;
import com.ping.android.model.Mapping;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 3/21/18.
 */

public class UpdateUserMappingUseCase extends UseCase<Boolean, Mapping> {
    @Inject
    UserRepository userRepository;

    @Inject
    public UpdateUserMappingUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<Boolean> buildUseCaseObservable(Mapping mapping) {
        return userRepository.getCurrentUser()
                .flatMap(user -> userRepository.updateUserMapping(user.key, mapping.mapKey, mapping.mapValue));
    }
}
