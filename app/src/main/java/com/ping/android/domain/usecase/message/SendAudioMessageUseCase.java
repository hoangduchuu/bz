package com.ping.android.domain.usecase.message;

import android.text.TextUtils;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.data.entity.MessageEntity;
import com.ping.android.data.mappers.MessageMapper;
import com.ping.android.domain.repository.ConversationRepository;
import com.ping.android.domain.repository.MessageRepository;
import com.ping.android.domain.repository.StorageRepository;
import com.ping.android.domain.repository.UserRepository;
import com.ping.android.model.Conversation;
import com.ping.android.model.Message;
import com.ping.android.model.User;
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
    @Inject
    MessageMapper messageMapper;
    @Inject
    MessageRepository messageRepository;
    SendMessageUseCase.Params.Builder builder;

    @Inject
    public SendAudioMessageUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<Message> buildUseCaseObservable(Params params) {
        return conversationRepository.getMessageKey(params.conversation.key)
                .flatMap(messageKey -> {
                    builder = new SendMessageUseCase.Params.Builder()
                            .setMessageType(params.messageType)
                            .setConversation(params.conversation)
                            .setVoiceType(params.voiceType)
                            .setCurrentUser(params.currentUser)
                            .setFileUrl(params.filePath)
                            .setMessageKey(messageKey);
                    MessageEntity cachedMessage = builder.build().getMessage();
                    Message temp = messageMapper.transform(cachedMessage, params.currentUser);
                    temp.isCached = true;
                    temp.localFilePath = params.filePath;
                    temp.currentUserId = params.currentUser.key;
                    return Observable.just(temp)
                            .concatWith(sendMessageUseCase.buildUseCaseObservable(builder.build())
                                    .flatMap(message -> sendMessage(params.conversation.key, message.key, params.filePath, message.currentUserId)
                                            .map(s -> message)));
                });

    }

    private Observable<String> sendMessage(String conversationId, String messageId, String filePath,String currentUserKey) {
        return this.uploadFile(conversationId, filePath)
                .flatMap(s -> messageRepository.updateAudioUrl(conversationId, messageId, s))
                .flatMap(s-> messageRepository.markSenderMessageStatusAsDelivered(conversationId,messageId,currentUserKey,s));
    }

    private Observable<String> uploadFile(String conversationKey, String filePath) {
        if (TextUtils.isEmpty(filePath)) return Observable.just("");
        return storageRepository.uploadFile(conversationKey, filePath);
    }

    public static class Params {
        public String filePath;
        public Conversation conversation;
        public User currentUser;
        public MessageType messageType;
        public VoiceType voiceType;
    }
}
