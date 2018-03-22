package com.ping.android.domain.usecase.notification;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.domain.repository.ConversationRepository;
import com.ping.android.domain.repository.NotificationRepository;
import com.ping.android.domain.repository.UserRepository;

import org.jetbrains.annotations.NotNull;

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
    ConversationRepository conversationRepository;

    @Inject
    public SendMissedCallNotificationUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<Boolean> buildUseCaseObservable(Params params) {
        return userRepository.getCurrentUser()
                .flatMap(user -> {
                    String conversationId = user.key.compareTo(params.opponentUserId) > 0
                            ? user.key + params.opponentUserId
                            : params.opponentUserId + user.key;
                    return conversationRepository.getConversationNickName(params.opponentUserId, conversationId, user.key)
                            .flatMap(senderNickname -> userRepository.readBadgeNumbers(params.opponentUserId)
                                    .flatMap(integer -> {
                                        String messageData = String.format("You missed a %s call from %s.",
                                                params.callType, senderNickname.isEmpty() ? user.getDisplayName() : senderNickname);
                                        return notificationRepository
                                                .sendMissedCallNotificationToUser(messageData, params.quickBloxId, params.callType, integer);
                                    }));
                });
    }

    public static class Params {
        private String opponentUserId;
        private int quickBloxId;
        private String callType;

        public Params(String opponentUserId, int quickBloxId, String callType) {
            this.opponentUserId = opponentUserId;
            this.quickBloxId = quickBloxId;
            this.callType = callType;
        }
    }
}
