package com.ping.android.domain.usecase.message;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
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

import java.io.ByteArrayOutputStream;
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
    // builder;

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
                .setGameType(params.gameType);
        builder.setCacheImage(params.filePath);
        builder.setFileUrl("PPhtotoMessageIdentifier");
        Message cachedMessage = builder.build().getMessage();
        cachedMessage.isCached = true;
        cachedMessage.localFilePath = params.filePath;
        return conversationRepository.getMessageKey(params.conversation.key)
                .zipWith(Observable.just(cachedMessage), (s, message) -> {
                    message.key = s;
                    builder.setMessageKey(s);
                    return message;
                })
                .flatMap(message -> sendMessageUseCase.buildUseCaseObservable(builder.build())
                        .map(message1 -> {
                            message1.isCached = true;
                            message1.localFilePath = params.filePath;
                            message1.currentUserId = params.currentUser.key;
                            return message1;
                        })
                        .concatWith(sendMessage(message, params.conversation.key, message.key, params.filePath)
                                .flatMap(message1 -> {
                                    message.isCached = false;
                                    Map<String, Object> updateValue = new HashMap<>();
                                    updateValue.put(String.format("messages/%s/%s/photoUrl", params.conversation.key, message.key), message.photoUrl);
                                    updateValue.put(String.format("messages/%s/%s/thumbUrl", params.conversation.key, message.key), message.thumbUrl);
                                    updateValue.put(String.format("media/%s/%s", params.conversation.key, message.key), message.toMap());
                                    return commonRepository.updateBatchData(updateValue)
                                            .map(aBoolean -> message);
                                }))
                );
    }

    private Observable<Message> sendMessage(Message message, String conversationId, String messageId, String filePath) {
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
        String fileName = new File(filePath).getName();
        return storageRepository.uploadFile(conversationKey, fileName, Utils.getImageData(filePath, 512, 512))
                .flatMap(s -> messageRepository.updateImage(conversationKey, messageKey, s));
    }

    private Observable<String> uploadThumbnail(String conversationKey, String messageKey, String filePath) {
        if (TextUtils.isEmpty(filePath)) return Observable.just("");
        String fileName = "thumb_" + new File(filePath).getName();
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
