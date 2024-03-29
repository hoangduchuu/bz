package com.ping.android.domain.usecase.message;

import android.text.TextUtils;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.data.entity.MessageEntity;
import com.ping.android.data.mappers.MessageMapper;
import com.ping.android.domain.repository.CommonRepository;
import com.ping.android.domain.repository.ConversationRepository;
import com.ping.android.domain.repository.MessageRepository;
import com.ping.android.domain.repository.StorageRepository;
import com.ping.android.domain.repository.UserRepository;
import com.ping.android.model.Conversation;
import com.ping.android.model.Message;
import com.ping.android.model.User;
import com.ping.android.model.enums.GameType;
import com.ping.android.model.enums.MessageType;
import com.ping.android.utils.Utils;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by tuanluong on 2/28/18.
 */

public class SendImageMessageUseCase extends UseCase<Message, SendImageMessageUseCase.Params> {
    @Inject
    UserRepository userRepository;
    @Inject
    CommonRepository commonRepository;
    @Inject
    StorageRepository storageRepository;
    @Inject
    ConversationRepository conversationRepository;
    @Inject
    SendMessageUseCase sendMessageUseCase;
    @Inject
    MessageRepository messageRepository;
    @Inject
    MessageMapper messageMapper;

    @Inject
    public SendImageMessageUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<Message> buildUseCaseObservable(Params params) {
        SendMessageUseCase.Params.Builder builder = new SendMessageUseCase.Params.Builder()
                .setMessageType(params.messageType)
                .setConversation(params.conversation)
                .setMarkStatus(params.markStatus)
                .setCurrentUser(params.currentUser)
                .setGameType(params.gameType)
                .setFileUrl(params.filePath)
                .setThumbUrl(params.thumbFilePath);
        MessageEntity cachedMessage = builder.build().getMessage();
        return conversationRepository.getMessageKey(params.conversation.key)
                .flatMap(messageKey -> {
                    cachedMessage.key = messageKey;
                    builder.setMessageKey(messageKey);
                    Message temp = messageMapper.transform(cachedMessage, params.currentUser);
                    temp.isCached = true;
                    temp.localFilePath = params.filePath;
                    temp.currentUserId = params.currentUser.key;
                    return Observable.just(temp)
                            .concatWith(sendMessageUseCase.buildUseCaseObservable(builder.build())
                                    .flatMap(msg -> uploadImages(cachedMessage, params.conversation.key, cachedMessage.key, params.filePath)
                                            .flatMap(message1 -> {
                                                Map<String, Object> updateValue = new HashMap<>();
                                                updateValue.put(String.format("messages/%s/%s/photoUrl", params.conversation.key, cachedMessage.key), message1.photoUrl);
                                                updateValue.put(String.format("messages/%s/%s/thumbUrl", params.conversation.key, cachedMessage.key), message1.thumbUrl);
                                                updateValue.put(String.format("media/%s/%s/photoUrl", params.conversation.key, cachedMessage.key), message1.photoUrl);
                                                updateValue.put(String.format("media/%s/%s/thumbUrl", params.conversation.key, cachedMessage.key), message1.thumbUrl);
                                                return commonRepository.updateBatchData(updateValue)
                                                        .map(aBoolean -> messageMapper.transform(message1, params.currentUser));
                                            }))
                            );
                });
    }

    private Observable<MessageEntity> uploadImages(MessageEntity message, String conversationId, String messageId, String filePath) {
        return this.uploadThumbnail(conversationId, messageId, filePath)
                .zipWith(uploadImage(conversationId, messageId, filePath), (s, s2) -> {
                    message.thumbUrl = s;
                    message.photoUrl = s2;
                    return message;
                })
                .observeOn(Schedulers.io());
    }

    private Observable<String> uploadImage(String conversationKey, String messageKey, String filePath) {
        if (TextUtils.isEmpty(filePath)) return Observable.just("");
        String fileName = System.currentTimeMillis() + new File(filePath).getName();
        return storageRepository.uploadFile(conversationKey, fileName, Utils.getImageData(filePath, 512, 512))
                .flatMap(s -> messageRepository.updateImage(conversationKey, messageKey, s));
    }

    private Observable<String> uploadThumbnail(String conversationKey, String messageKey, String filePath) {
        if (TextUtils.isEmpty(filePath)) return Observable.just("");
        String fileName = "thumb_" + System.currentTimeMillis() + new File(filePath).getName();
        return storageRepository.uploadFile(conversationKey, fileName, Utils.getImageData(filePath, 128, 128))
                .flatMap(s -> messageRepository.updateThumbnailImage(conversationKey, messageKey, s));
    }

    public static class Params {
        public String filePath;
        public String thumbFilePath;
        public Conversation conversation;
        public User currentUser;
        public MessageType messageType;
        public GameType gameType;
        public boolean markStatus;
    }
}
