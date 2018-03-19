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

public class LoginChatServiceUseCase extends UseCase<Boolean, LoginChatServiceUseCase.Params> {
    @Inject
    QuickbloxRepository quickbloxRepository;

    @Inject
    public LoginChatServiceUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<Boolean> buildUseCaseObservable(Params params) {
        return quickbloxRepository.loginChat(params.qbId, params.pingId);
    }

    public static class Params {
        public int qbId;
        public String pingId;

        public Params(int qbId, String pingId) {
            this.qbId = qbId;
            this.pingId = pingId;
        }
    }
}
