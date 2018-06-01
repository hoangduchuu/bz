package com.ping.android.domain.usecase.notification;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.domain.repository.ConversationRepository;
import com.ping.android.domain.repository.UserRepository;
import com.ping.android.domain.usecase.message.SendMessageUseCase;
import com.ping.android.domain.usecase.message.SendTextMessageUseCase;
import com.ping.android.managers.UserManager;
import com.ping.android.model.User;
import com.ping.android.model.enums.MessageType;
import com.ping.android.utils.CommonMethod;
import com.ping.android.utils.configs.Constant;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 3/22/18.
 */

public class ReplyMessageFromNotificationUseCase extends UseCase<Boolean, ReplyMessageFromNotificationUseCase.Params> {
    @Inject
    ConversationRepository conversationRepository;
    @Inject
    UserRepository userRepository;
    @Inject
    UserManager userManager;
    @Inject
    SendTextMessageUseCase sendTextMessageUseCase;
    @Inject
    SendMessageNotificationUseCase sendMessageNotificationUseCase;

    @Inject
    public ReplyMessageFromNotificationUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<Boolean> buildUseCaseObservable(Params params) {
        return userManager.getCurrentUser()
                .flatMap(user -> conversationRepository.getConversation(user, params.conversationId)
                        .flatMap(conversation -> userRepository.getUserList(conversation.memberIDs)
                                .map(users -> {
                                    conversation.members = users;
                                    if (conversation.conversationType == Constant.CONVERSATION_TYPE_INDIVIDUAL) {
                                        for (User u : users) {
                                            if (!u.key.equals(user.key)) {
                                                conversation.opponentUser = u;
                                            }
                                        }
                                    }
                                    return conversation;
                                }))
                        .flatMap(conversation -> {
                            boolean maskStatus = CommonMethod.getBooleanFrom(conversation.maskOutputs, user.key);
                            SendMessageUseCase.Params messageParams = new SendMessageUseCase.Params.Builder()
                                    .setMessageType(MessageType.TEXT)
                                    .setConversation(conversation)
                                    .setCurrentUser(user)
                                    .setText(params.message)
                                    .setMarkStatus(maskStatus)
                                    .build();
                            return sendTextMessageUseCase.buildUseCaseObservable(messageParams)
                                    .flatMap(message -> {
                                        SendMessageNotificationUseCase.Params notificationParams =
                                                new SendMessageNotificationUseCase.Params(conversation, message);
                                        return sendMessageNotificationUseCase.buildUseCaseObservable(notificationParams);
                                    });
                        })
                );
    }

    public static class Params {
        private String message;
        private String conversationId;

        public Params(String message, String conversationId) {
            this.message = message;
            this.conversationId = conversationId;
        }
    }
}
