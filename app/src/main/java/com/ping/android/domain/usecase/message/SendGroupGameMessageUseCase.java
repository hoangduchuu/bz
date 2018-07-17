package com.ping.android.domain.usecase.message;

import android.text.TextUtils;
import android.util.Pair;

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
import com.ping.android.model.enums.GameType;
import com.ping.android.model.enums.MessageType;
import com.ping.android.utils.Utils;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by tuanluong on 2/28/18.
 */

public class SendGroupGameMessageUseCase extends UseCase<Message, SendGroupGameMessageUseCase.Params> {
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
    SendMessageUseCase sendMessageUseCase;

    @Inject
    public SendGroupGameMessageUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<Message> buildUseCaseObservable(Params params) {
        SendMessageUseCase.Params.Builder builder = new SendMessageUseCase.Params.Builder()
                .setMessageType(MessageType.IMAGE_GROUP)
                .setConversation(params.conversation)
                .setMarkStatus(params.markStatus)
                .setCurrentUser(params.currentUser)
                .setChildCount(params.items.size());
        return conversationRepository.getMessageKey(params.conversation.key)
                .map(messageKey -> {
                    builder.setMessageKey(messageKey);
                    return builder;
                })
                .flatMap(builder1 -> sendMessageUseCase.buildUseCaseObservable(builder1.build())
                        .flatMap(message -> sendMediaMessage(params.conversation.key, message))
                        .flatMap(message -> buildCacheChildMessages(params, message.key)
                                .map(messages -> {
                                    message.isCached = true;
                                    message.isMask = params.markStatus;
                                    message.childMessages = messages;
                                    Collections.sort(messages, (o1, o2) -> Double.compare(o2.timestamp, o1.timestamp));
                                    return message;
                                })
                                .flatMap(message1 -> Observable.just(message1)
                                        .concatWith(sendChildMessages(params.conversation.key, message1)
                                                .map(messages -> {
                                                    // Need set isCached to false in order to notify presenter to trigger send notification
                                                    message1.isCached = false;
                                                    message1.childMessages = messages;
                                                    Collections.sort(messages, (o1, o2) -> Double.compare(o2.timestamp, o1.timestamp));
                                                    return message1;
                                                })
                                        )
                                )
                        )
                );
    }

    private Observable<List<Message>> buildCacheChildMessages(SendGroupGameMessageUseCase.Params params, String messageKey) {
        PhotoItem[] photoArray = new PhotoItem[params.items.size()];
        return Observable.fromArray(params.items.toArray(photoArray))
                .flatMap(photoItem -> {
                    SendMessageUseCase.Params.Builder builder = new SendMessageUseCase.Params.Builder()
                            .setMessageType(MessageType.GAME)
                            .setGameType(params.gameType)
                            .setConversation(params.conversation)
                            .setMarkStatus(params.markStatus)
                            .setCurrentUser(params.currentUser)
                            .setCacheImage(photoItem.getImagePath());
                    String childKey = messageRepository.populateChildMessageKey(params.conversation.key, messageKey);
                    Message message = builder.build().getMessage();
                    message.isCached = true;
                    message.parentKey = messageKey;
                    message.key = childKey;
                    message.localFilePath = photoItem.getImagePath();
                    return messageRepository.addChildMessage(params.conversation.key, messageKey, message)
                            .map(message1 -> {
                                // Should set those params to make the UI action right cause this is cached message
                                message1.photoUrl = message1.localFilePath;
                                message1.isMask = params.markStatus;
                                return message1;
                            });
                })
                .take(params.items.size())
                .toList()
                .toObservable();
    }

    private Observable<List<Message>> sendChildMessages(String conversationId, Message parentMessage) {
        Message[] messages = new Message[parentMessage.childMessages.size()];
        return Observable.fromArray(parentMessage.childMessages.toArray(messages))
                .observeOn(Schedulers.io())
                .flatMap(message -> uploadImage(conversationId, message.localFilePath)
                        .flatMap(gameUrl -> messageRepository.updateChildMessageGame(conversationId,
                                parentMessage.key, message.key, gameUrl)
                                .map(aBoolean -> message))
                )
                .take(messages.length)
                .toList()
                .toObservable();

    }

    private Observable<String> uploadImage(String conversationKey, String filePath) {
        if (TextUtils.isEmpty(filePath)) return Observable.just("");
        String fileName = System.currentTimeMillis() + new File(filePath).getName();
        return storageRepository.uploadFile(conversationKey, fileName, Utils.getImageData(filePath, 512, 512));
    }

    private Observable<Message> sendMediaMessage(String conversationId, Message message) {
        return messageRepository.sendMediaMessage(conversationId, message);
    }

    public static class Params {
        public List<PhotoItem> items;
        public Conversation conversation;
        public User currentUser;
        public GameType gameType;
        public boolean markStatus;
    }
}
