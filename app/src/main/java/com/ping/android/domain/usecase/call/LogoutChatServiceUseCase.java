package com.ping.android.domain.usecase.call;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.domain.repository.QuickbloxRepository;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 3/16/18.
 */

public class LogoutChatServiceUseCase extends UseCase<Boolean, Void> {
    @Inject
    QuickbloxRepository quickbloxRepository;

    @Inject
    public LogoutChatServiceUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<Boolean> buildUseCaseObservable(Void params) {
        return quickbloxRepository.logout();
    }
}
