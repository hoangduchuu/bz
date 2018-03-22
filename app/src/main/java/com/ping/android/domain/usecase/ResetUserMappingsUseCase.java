package com.ping.android.domain.usecase;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.domain.repository.UserRepository;
import com.ping.android.form.Mapping;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 3/21/18.
 */

public class ResetUserMappingsUseCase extends UseCase<Boolean, Map<String, String>> {
    @Inject
    UserRepository userRepository;

    @Inject
    public ResetUserMappingsUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<Boolean> buildUseCaseObservable(Map<String, String> mappings) {
        return userRepository.getCurrentUser()
                .flatMap(user -> userRepository.updateUserMappings(user.key, mappings));
    }
}
