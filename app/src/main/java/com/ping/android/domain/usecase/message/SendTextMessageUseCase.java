package com.ping.android.domain.usecase.message;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.domain.repository.ConversationRepository;
import com.ping.android.domain.repository.MessageRepository;
import com.ping.android.model.Message;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 2/8/18.
 */

public class SendTextMessageUseCase extends UseCase<Message, SendMessageUseCase.Params> {
    @Inject
    ConversationRepository conversationRepository;
    @Inject
    SendMessageUseCase sendMessageUseCase;

    @Inject
    MessageRepository messageRepository;

    @Inject
    public SendTextMessageUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<Message> buildUseCaseObservable(SendMessageUseCase.Params params) {
        return conversationRepository.getMessageKey(params.getConversation().key)
                .map(s -> {
                    params.setMessageKey(s);
                    return params.getMessage();
                })
                .flatMap(message ->
                        sendMessageUseCase.buildUseCaseObservable(params))
                .flatMap(message -> messageRepository.markSenderMessageStatusAsDelivered(params.getConversation().key, message.key, message.currentUserId, "")
                        .map(
                                s -> message
                        ));
    }
}
