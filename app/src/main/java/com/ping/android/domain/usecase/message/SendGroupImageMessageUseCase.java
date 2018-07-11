package com.ping.android.domain.usecase.message;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import com.bumptech.glide.util.Util;
import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.bzzzchat.videorecorder.view.PhotoItem;
import com.ping.android.domain.repository.CommonRepository;
import com.ping.android.domain.repository.ConversationRepository;
import com.ping.android.domain.repository.MessageRepository;
import com.ping.android.domain.repository.StorageRepository;
import com.ping.android.domain.repository.UserRepository;
import com.ping.android.model.Conversation;
import com.ping.android.model.Message;
import com.ping.android.model.User;
import com.ping.android.model.enums.MessageType;
import com.ping.android.utils.Utils;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 2/28/18.
 */

public class SendGroupImageMessageUseCase extends UseCase<Message, SendGroupImageMessageUseCase.Params> {
    @Inject
    UserRepository userRepository;
    @Inject
    CommonRepository commonRepository;
    @Inject
    StorageRepository storageRepository;
    @Inject
    ConversationRepository conversationRepository;
    @Inject
    MessageRepository messageRepository;
    @Inject
    SendImageMessageUseCase sendImageMessageUseCase;
    @Inject
    SendMessageUseCase sendMessageUseCase;

    @Inject
    public SendGroupImageMessageUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<Message> buildUseCaseObservable(Params params) {
        SendMessageUseCase.Params.Builder builder = new SendMessageUseCase.Params.Builder()
                .setMessageType(MessageType.IMAGE_GROUP)
                .setConversation(params.conversation)
                .setMarkStatus(params.markStatus)
                .setCurrentUser(params.currentUser);
        return conversationRepository.getMessageKey(params.conversation.key)
                .map(messageKey -> {
                    builder.setMessageKey(messageKey);
                    return builder;
                })
                .flatMap(builder1 -> sendMessageUseCase.buildUseCaseObservable(builder1.build()))
                .flatMap(message -> sendChildMessages(params, message.key)
                        .map(messages -> {
                            message.childMessages = messages;
                            return message;
                        }));
    }

    private Observable<List<Message>> sendChildMessages(SendGroupImageMessageUseCase.Params params, String messageKey) {
        PhotoItem[] photoArray = new PhotoItem[params.items.size()];
        return Observable.fromArray(params.items.toArray(photoArray))
                .map(photoItem -> {
                    SendMessageUseCase.Params.Builder builder = new SendMessageUseCase.Params.Builder()
                            .setMessageType(params.messageType)
                            .setConversation(params.conversation)
                            .setMarkStatus(params.markStatus)
                            .setCurrentUser(params.currentUser)
                            .setCacheImage(photoItem.getImagePath())
                            .setMessageType(MessageType.IMAGE);
                    return builder.build().getMessage();
                })
                .toList()
                .toObservable()
                .concatWith(Observable.fromArray(params.items.toArray(photoArray))
                        .flatMap(photoItem -> uploadThumbnail(params.conversation.key, photoItem.getImagePath())
                                .zipWith(uploadImage(params.conversation.key, photoItem.getImagePath()), (s, s2) -> {
                                    SendMessageUseCase.Params.Builder builder = new SendMessageUseCase.Params.Builder()
                                            .setMessageType(params.messageType)
                                            .setConversation(params.conversation)
                                            .setMarkStatus(params.markStatus)
                                            .setCurrentUser(params.currentUser)
                                            .setFileUrl(s2)
                                            .setThumbUrl(s)
                                            .setMessageType(MessageType.IMAGE);
                                    return builder.build().getMessage();
                                })
                                .flatMap(message -> messageRepository.addChildMessage(params.conversation.key, messageKey, message))
                        )
                        .take(params.items.size())
                        .toList());

    }

    private Observable<String> uploadImage(String conversationKey, String filePath) {
        if (TextUtils.isEmpty(filePath)) return Observable.just("");
        String fileName = new File(filePath).getName();
        return storageRepository.uploadFile(conversationKey, fileName, Utils.getImageData(filePath, 512, 512));
    }

    private Observable<String> uploadThumbnail(String conversationKey, String filePath) {
        if (TextUtils.isEmpty(filePath)) return Observable.just("");
        String fileName = "thumb_" + new File(filePath).getName();
        return storageRepository.uploadFile(conversationKey, fileName, Utils.getImageData(filePath, 128, 128));
    }

    public static class Params {
        public List<PhotoItem> items;
        public Conversation conversation;
        public User currentUser;
        public MessageType messageType;
        public boolean markStatus;

        public List<Message> getChildMessages() {
            List<Message> messages = new ArrayList<>(items.size());
            for (PhotoItem item : items) {
                SendMessageUseCase.Params.Builder builder = new SendMessageUseCase.Params.Builder()
                        .setMessageType(messageType)
                        .setConversation(conversation)
                        .setMarkStatus(markStatus)
                        .setCurrentUser(currentUser)
                        .setFileUrl(item.getImagePath())
                        .setThumbUrl(item.getThumbnailPath())
                        .setMessageType(MessageType.IMAGE);
                messages.add(builder.build().getMessage());
            }
            return messages;
        }
    }
}
