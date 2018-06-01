package com.ping.android.domain.usecase.user;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.domain.repository.UserRepository;
import com.ping.android.managers.UserManager;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.Observable;

public class ObserveMappingsUseCase extends UseCase<Map<String, String>, Void> {
    @Inject
    UserRepository userRepository;
    @Inject
    UserManager userManager;

    @Inject
    public ObserveMappingsUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<Map<String, String>> buildUseCaseObservable(Void aVoid) {
        return userManager.getCurrentUser()
                .flatMap(user -> userRepository.observeMappings(user.key)
                .map(dataSnapshot -> {
                    if (dataSnapshot.exists()) {
                        return (Map<String, String>) dataSnapshot.getValue();
                    }
                    return new HashMap<String, String>();
                }));
    }
}
