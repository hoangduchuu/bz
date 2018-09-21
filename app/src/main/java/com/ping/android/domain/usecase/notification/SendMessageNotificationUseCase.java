package com.ping.android.domain.usecase.notification;

import android.text.TextUtils;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.domain.repository.NotificationRepository;
import com.ping.android.domain.repository.UserRepository;
import com.ping.android.managers.UserManager;
import com.ping.android.model.Conversation;
import com.ping.android.model.User;
import com.ping.android.model.enums.MessageType;
import com.ping.android.utils.CommonMethod;

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
    UserManager userManager;

    @Inject
    public SendMessageNotificationUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<Boolean> buildUseCaseObservable(Params params) {
        return userManager.getCurrentUser()
                .flatMap(sender -> {
                    String senderName = sender.getDisplayName();
                    Map<String, String> nickNames = params.conversation.nickNames;
                    if (nickNames.containsKey(sender.key)) {
                        String nickname = nickNames.get(sender.key);
                        if (!TextUtils.isEmpty(nickname)) {
                            senderName = nickname;
                        }
                    }
                    if (params.conversation.group != null) {
                        String groupName = params.conversation.group.groupName;
                        senderName = String.format("%s to %s", senderName, groupName);
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
                                            switch (params.messageType) {
                                                case TEXT:
                                                    String messageText = params.message;
                                                    if (incomingMask && user.mappings != null && user.mappings.size() > 0) {
                                                        messageText = CommonMethod.encodeMessage(params.message, user.mappings);
                                                    }
                                                    body = String.format("%s: %s", userName, messageText);
                                                    break;
                                                case VOICE:
                                                    body = userName + ": sent a voice message.";
                                                    break;
                                                case IMAGE:
                                                    body = userName + ": sent a picture message.";
                                                    break;
                                                case GAME:
                                                    body = userName + ": sent a game.";
                                                    break;
                                                case VIDEO:
                                                    body = userName + ": sent a video message.";
                                                    break;
                                                case IMAGE_GROUP:
                                                    body = userName + ": sent (" + params.message + ") pictures.";
                                                    break;
                                                case GAME_GROUP:
                                                    body = userName + ": sent (" + params.message + ") games." ;
                                                    break;
                                                case STICKER:
                                                    body = userName + ": sent a sticker.";
                                                    break;
                                                default:
                                                    break;
                                            }
                                            String profile = sender.profile;
                                            if (user.settings.private_profile) {
                                                profile = "";
                                            }
                                            return notificationRepository.sendMessageNotification(
                                                    sender.key, profile, body, params.conversation.key, params.messageType.ordinal(), user, integer)
                                                    .flatMap(aBoolean -> userRepository.increaseBadgeNumber(user.key, params.conversation.key));
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
        //private Message message;
        private MessageType messageType;
        private String message;

        public Params(Conversation conversation, String message, MessageType messageType) {
            this.conversation = conversation;
            this.message = message;
            this.messageType = messageType;
        }
    }
}
