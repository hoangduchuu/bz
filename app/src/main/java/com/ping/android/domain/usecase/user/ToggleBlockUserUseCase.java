package com.ping.android.domain.usecase.user;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.domain.repository.CommonRepository;
import com.ping.android.domain.repository.UserRepository;
import com.ping.android.managers.UserManager;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 3/14/18.
 */

public class ToggleBlockUserUseCase extends UseCase<Boolean, ToggleBlockUserUseCase.Params> {
    @Inject
    UserRepository userRepository;
    @Inject
    CommonRepository commonRepository;
    @Inject
    UserManager userManager;

    @Inject
    public ToggleBlockUserUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<Boolean> buildUseCaseObservable(Params params) {
        return userManager.getCurrentUser()
                .flatMap(user -> {
                    Map<String, Object> updateValue = new HashMap<>();
                    updateValue.put(String.format("users/%s/blocks/%s", user.key, params.userId), params.value ? true : null);
                    updateValue.put(String.format("users/%s/blockBys/%s", params.userId, user.key), params.value ? true : null);
                    return commonRepository.updateBatchData(updateValue);
                });
    }

    public static class Params {
        public final String userId;
        public final boolean value;

        public Params(String userId, boolean value) {
            this.userId = userId;
            this.value = value;
        }
    }
}
