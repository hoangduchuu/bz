package com.ping.android.domain.usecase.notification;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.domain.repository.NotificationRepository;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 3/22/18.
 */

public class SendStartCallNotificationUseCase extends UseCase<Boolean, SendStartCallNotificationUseCase.Params> {
    @Inject
    NotificationRepository notificationRepository;

    @Inject
    public SendStartCallNotificationUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<Boolean> buildUseCaseObservable(Params params) {
        return notificationRepository.sendCallingNotificationToUser(params.quickBloxId, params.callType);
    }

    public static class Params {
        public int quickBloxId;
        public String callType;

        public Params(int quickBloxId, String callType) {
            this.quickBloxId = quickBloxId;
            this.callType = callType;
        }
    }
}
