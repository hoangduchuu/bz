package com.ping.android.domain.usecase;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.domain.repository.CommonRepository;
import com.ping.android.domain.repository.UserRepository;
import com.ping.android.managers.UserManager;
import com.ping.android.model.Call;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 3/22/18.
 */

public class DeleteCallsUseCase extends UseCase<Boolean, List<Call>> {
    @Inject
    UserRepository userRepository;
    @Inject
    CommonRepository commonRepository;
    @Inject
    UserManager userManager;

    @Inject
    public DeleteCallsUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<Boolean> buildUseCaseObservable(List<Call> calls) {
        return userManager.getCurrentUser()
                .flatMap(user -> {
                    Map<String, Object> updateValue = new HashMap<>();
                    for (Call call : calls) {
                        updateValue.put(String.format("calls/%s/%s", user.key, call.key), null);
                    }
                    return commonRepository.updateBatchData(updateValue);
                });
    }
}
