package com.ping.android.domain.usecase.message;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.domain.repository.ConversationRepository;
import com.ping.android.domain.repository.UserRepository;
import com.ping.android.model.Message;
import com.ping.android.model.User;
import com.ping.android.model.enums.MessageType;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 2/8/18.
 */

public class SendCallMessageUseCase extends UseCase<Message, SendCallMessageUseCase.Params> {
    @Inject
    ConversationRepository conversationRepository;
    @Inject
    UserRepository userRepository;
    @Inject
    SendMessageUseCase sendMessageUseCase;
    SendMessageUseCase.Params.Builder builder;

    @Inject
    public SendCallMessageUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<Message> buildUseCaseObservable(SendCallMessageUseCase.Params params) {
        return userRepository.getCurrentUser()
                .flatMap(user -> {
                    String conversationID = user.key.compareTo(params.toUser.key) > 0 ? user.key + params.toUser.key : params.toUser.key + user.key;
                    return conversationRepository.getConversation(user, conversationID)
                            .zipWith(conversationRepository.getMessageKey(conversationID), (conversation, messageKey) -> {
                                conversation.opponentUser = params.toUser;
                                builder = new SendMessageUseCase.Params.Builder()
                                        .setMessageType(params.getMessageType())
                                        .setConversation(conversation)
                                        .setCurrentUser(user)
                                        .setMessageKey(messageKey);
                                return builder.build();
                            })
                            .flatMap(messageParams -> sendMessageUseCase.buildUseCaseObservable(messageParams));
                });
    }

    public static class Params {
        private boolean isMissedCall;
        private User toUser;

        public Params(User toUser, boolean isMissedCall) {
            this.toUser = toUser;
            this.isMissedCall = isMissedCall;
        }

        public MessageType getMessageType() {
            return isMissedCall ? MessageType.MISSED_CALL : MessageType.CALL;
        }
    }
}
