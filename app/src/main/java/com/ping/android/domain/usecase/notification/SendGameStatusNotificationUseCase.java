package com.ping.android.domain.usecase.notification;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.domain.repository.NotificationRepository;
import com.ping.android.domain.repository.UserRepository;
import com.ping.android.model.Conversation;
import com.ping.android.model.User;
import com.ping.android.ultility.CommonMethod;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 3/22/18.
 */

public class SendGameStatusNotificationUseCase extends UseCase<Boolean, SendGameStatusNotificationUseCase.Params> {
    @Inject
    NotificationRepository notificationRepository;
    @Inject
    UserRepository userRepository;

    @Inject
    public SendGameStatusNotificationUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<Boolean> buildUseCaseObservable(Params params) {
        return userRepository.getCurrentUser()
                .flatMap(sender -> {
                    if (!needSendNotification(params.conversation, params.opponentUser, sender)) {
                        return Observable.just(false);
                    }
                    String senderName = sender.getDisplayName();
                    Map<String, String> nickNames = params.conversation.nickNames;
                    if (nickNames.containsKey(sender.key)) {
                        senderName = nickNames.get(sender.key);
                    }
                    String body = senderName + (params.passed ? " passed a game you sent.": " failed to complete a game you sent.");
                    final String userName = senderName;
                    return userRepository.readBadgeNumbers(params.opponentUser.key)
                                        .flatMap(integer -> notificationRepository
                                                .sendGameStatusNotificationToSender(sender.key, userName, params.opponentUser.quickBloxID,
                                                        params.conversation.key, body, integer));
                });
    }

    private boolean needSendNotification(Conversation conversation, User user, User sender) {
        if (user == null) return false;
        boolean isBlock = CommonMethod.getBooleanFrom(sender.blocks, user.key);
        boolean isBlockBy = CommonMethod.getBooleanFrom(sender.blockBys, user.key);
        if (isBlock || isBlockBy) {
            return false;
        }
        //check if target opponentUser enable notification
        return !(conversation.notifications != null
                && conversation.notifications.containsKey(user.key)
                && !conversation.notifications.get(user.key));
    }

    public static class Params {
        private Conversation conversation;
        private boolean passed;
        private User opponentUser;

        public Params(Conversation conversation, User opponentUser, boolean passed) {
            this.conversation = conversation;
            this.passed = passed;
            this.opponentUser = opponentUser;
        }
    }
}
