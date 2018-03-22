package com.ping.android.domain.usecase.notification;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.domain.repository.NotificationRepository;
import com.ping.android.domain.repository.UserRepository;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 3/22/18.
 */

public class SendMissedCallNotificationUseCase extends UseCase<Boolean, SendMissedCallNotificationUseCase.Params> {
    @Inject
    NotificationRepository notificationRepository;
    @Inject
    UserRepository userRepository;

    @Inject
    public SendMissedCallNotificationUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<Boolean> buildUseCaseObservable(Params params) {
        return userRepository.readBadgeNumbers(params.userId)
                .flatMap(result -> notificationRepository
                        .sendMissedCallNotificationToUser(params.quickBloxId, params.callType, result));
    }

    public static class Params {
        public String userId;
        public int quickBloxId;
        public String callType;

        public Params(String userId, int quickBloxId, String callType) {
            this.userId = userId;
            this.quickBloxId = quickBloxId;
            this.callType = callType;
        }
    }
}
