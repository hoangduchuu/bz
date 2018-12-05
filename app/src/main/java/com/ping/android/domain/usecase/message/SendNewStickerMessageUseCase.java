package com.ping.android.domain.usecase.message;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.domain.repository.ConversationRepository;
import com.ping.android.domain.repository.MessageRepository;
import com.ping.android.model.Message;
import com.ping.android.utils.BzLog;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by Huu Hoang on 05/12/2018
 */

public class SendNewStickerMessageUseCase extends UseCase<Message, SendMessageUseCase.Params> {
    @Inject
    ConversationRepository conversationRepository;
    @Inject
    SendMessageUseCase sendMessageUseCase;

    @Inject
    MessageRepository messageRepository;

    @Inject
    public SendNewStickerMessageUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
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
                        sendMessageUseCase.buildUseCaseObservable(params));


    }
}
