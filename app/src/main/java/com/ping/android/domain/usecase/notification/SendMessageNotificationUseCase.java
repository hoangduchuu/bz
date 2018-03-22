package com.ping.android.domain.usecase.notification;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.domain.repository.NotificationRepository;
import com.ping.android.domain.repository.UserRepository;
import com.ping.android.model.Conversation;
import com.ping.android.model.Message;
import com.ping.android.model.User;
import com.ping.android.service.ServiceManager;
import com.ping.android.ultility.CommonMethod;
import com.ping.android.ultility.Constant;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 3/22/18.
 */

public class SendMessageNotificationUseCase extends UseCase<Boolean, SendMessageNotificationUseCase.Params> {
    @Inject
    NotificationRepository notificationRepository;
    @Inject
    UserRepository userRepository;

    @Inject
    public SendMessageNotificationUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<Boolean> buildUseCaseObservable(Params params) {
        return userRepository.getCurrentUser()
                .flatMap(sender -> {
                    String senderName = sender.getDisplayName();
                    Map<String, String> nickNames = params.conversation.nickNames;
                    if (nickNames.containsKey(sender.key)) {
                        senderName = nickNames.get(sender.key);
                    }
                    if (params.conversation.group != null) {
                        senderName = String.format("%s to %s", senderName, params.conversation.group.groupName);
                    }
                    ArrayList<User> validUsers = new ArrayList<>();
                    for (User user : params.conversation.members) {
                        if (user.quickBloxID > 0 && !user.key.equals(sender.key)) {
                            if (!needSendNotification(params.conversation, user, sender)) continue;
                            validUsers.add(user);
                        }
                    }
                    final String userName = senderName;
                    return Observable.fromArray(validUsers.toArray())
                            .flatMap(object -> {
                                User user = (User) object;
                                return userRepository.readBadgeNumbers(user.key)
                                        .flatMap(integer -> {
                                            //get incoming mask of target opponentUser
                                            boolean incomingMask = CommonMethod.getBooleanFrom(params.conversation.maskMessages, user.key);
                                            String body = "";
                                            switch (params.message.messageType) {
                                                case Constant.MSG_TYPE_TEXT:
                                                    String messageText = params.message.message;
                                                    if (incomingMask && user.mappings != null && user.mappings.size() > 0) {
                                                        messageText = ServiceManager.getInstance().encodeMessage(user.mappings, params.message.message);
                                                    }
                                                    body = String.format("%s: %s", userName, messageText);
                                                    break;
                                                case Constant.MSG_TYPE_VOICE:
                                                    body = userName + ": sent a voice message.";
                                                    break;
                                                case Constant.MSG_TYPE_IMAGE:
                                                    body = userName + ": sent a picture message.";
                                                    break;
                                                case Constant.MSG_TYPE_GAME:
                                                    body = userName + ": sent a game.";
                                                    break;
                                                default:
                                                    break;
                                            }
                                            return notificationRepository.sendMessageNotification(
                                                    sender.key, body, params.conversation.key,
                                                    params.message, user, integer);
                                        });
                            })
                            .take(validUsers.size());
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
        private Message message;

        public Params(Conversation conversation, Message message) {
            this.conversation = conversation;
            this.message = message;
        }
    }
}
