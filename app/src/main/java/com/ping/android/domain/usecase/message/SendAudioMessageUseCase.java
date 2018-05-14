package com.ping.android.domain.usecase.message;

import android.text.TextUtils;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.domain.repository.ConversationRepository;
import com.ping.android.domain.repository.StorageRepository;
import com.ping.android.domain.repository.UserRepository;
import com.ping.android.model.Conversation;
import com.ping.android.model.Message;
import com.ping.android.model.User;
import com.ping.android.model.enums.GameType;
import com.ping.android.model.enums.MessageType;
import com.ping.android.model.enums.VoiceType;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 2/28/18.
 */

public class SendAudioMessageUseCase extends UseCase<Message, SendAudioMessageUseCase.Params> {
    @Inject
    UserRepository userRepository;
    @Inject
    StorageRepository storageRepository;
    @Inject
    ConversationRepository conversationRepository;
    @Inject
    SendMessageUseCase sendMessageUseCase;
    SendMessageUseCase.Params.Builder builder;

    @Inject
    public SendAudioMessageUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<Message> buildUseCaseObservable(Params params) {
        return conversationRepository.getMessageKey(params.conversation.key)
                .flatMap(s -> {
                    builder = new SendMessageUseCase.Params.Builder()
                            .setMessageType(params.messageType)
                            .setConversation(params.conversation)
                            .setVoiceType(params.voiceType)
                            .setCurrentUser(params.currentUser)
                            .setMessageKey(s);
                    return sendMessageUseCase.buildUseCaseObservable(builder.build())
                            .map(message -> {
                                message.isCached = true;
                                return message;
                            })
                            .concatWith(sendMessage(params));
                });

    }

    private Observable<Message> sendMessage(Params params) {
        return this.uploadImage(params.conversation.key, params.filePath)
                .map(s -> {
                    builder.setImageUrl(s);
                    return builder.build();
                })
                .flatMap(params1 -> sendMessageUseCase.buildUseCaseObservable(params1));
    }

    private Observable<String> uploadImage(String conversationKey, String filePath) {
        if (TextUtils.isEmpty(filePath)) return Observable.just("");
        return storageRepository.uploadImageMessage(conversationKey, filePath);
    }

    public static class Params {
        public String filePath;
        public Conversation conversation;
        public User currentUser;
        public MessageType messageType;
        public VoiceType voiceType;
    }
}
