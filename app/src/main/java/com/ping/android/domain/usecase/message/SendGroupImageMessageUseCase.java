package com.ping.android.domain.usecase.message;

import android.text.TextUtils;
import android.util.Pair;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.bzzzchat.videorecorder.view.PhotoItem;
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
import com.ping.android.model.enums.MessageType;
import com.ping.android.utils.NetworkConnectionChecker;
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
    SendMessageUseCase sendMessageUseCase;
    @Inject
    MessageMapper messageMapper;
    @Inject
    NetworkConnectionChecker networkConnectionChecker;

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
                .flatMap(messageKey -> {
                    List<MessageEntity> childMessages = buildChildMessages(messageKey, params, networkConnectionChecker.isConnected());
                    Collections.sort(childMessages, (o1, o2) -> Double.compare(o2.timestamp, o1.timestamp));
                    builder.setMessageKey(messageKey);
                    builder.setChildMessages(childMessages);
                    MessageEntity entity = builder.build().getMessage();
                    Message temp = messageMapper.transform(entity, params.currentUser);
                    temp.isCached = true;
                    return Observable.just(temp)
                            .concatWith(sendMessageUseCase.buildUseCaseObservable(builder.build())
                                    .flatMap(message -> sendChildMessages(params.conversation.key, message)
                                            .map(messages -> {
                                                // Need set isCached to false in order to notify presenter to trigger send notification
                                                message.isCached = false;
                                                message.childMessages = messages;
                                                Collections.sort(messages, (o1, o2) -> Double.compare(o2.timestamp, o1.timestamp));
                                                return message;
                                            })
                                    )
                            );
                });
    }

    private List<MessageEntity> buildChildMessages(String parentKey, SendGroupImageMessageUseCase.Params params, boolean isConnected) {
        List<MessageEntity> childMessages = new ArrayList<>();
        for (PhotoItem item : params.items) {
            SendMessageUseCase.Params.Builder builder = new SendMessageUseCase.Params.Builder()
                    .setMessageType(MessageType.IMAGE)
                    .setConversation(params.conversation)
                    .setMarkStatus(params.markStatus)
                    .setCurrentUser(params.currentUser)
                    .setMessageType(MessageType.IMAGE)
                    .setFileUrl(item.getImagePath())
                    .setThumbUrl(item.getImagePath());
            String childKey = messageRepository.populateChildMessageKey(params.conversation.key, parentKey);
            MessageEntity message = builder.build().getMessage();
            message.key = childKey;
            if (!isConnected) {
                message.photoUrl = item.getImagePath();
            }
            childMessages.add(message);
        }
        return childMessages;
    }

    private Observable<List<Message>> buildCacheChildMessages(SendGroupImageMessageUseCase.Params params, String messageKey) {
        PhotoItem[] photoArray = new PhotoItem[params.items.size()];
        return Observable.fromArray(params.items.toArray(photoArray))
                .flatMap(photoItem -> {
                    SendMessageUseCase.Params.Builder builder = new SendMessageUseCase.Params.Builder()
                            .setMessageType(MessageType.IMAGE)
                            .setConversation(params.conversation)
                            .setMarkStatus(params.markStatus)
                            .setCurrentUser(params.currentUser)
                            .setMessageType(MessageType.IMAGE);
                    String childKey = messageRepository.populateChildMessageKey(params.conversation.key, messageKey);
                    MessageEntity message = builder.build().getMessage();
                    message.key = childKey;
                    return messageRepository.addChildMessage(params.conversation.key, messageKey, message)
                            .map(entity -> {
                                Message mess = messageMapper.transform(entity, params.currentUser);
                                mess.isCached = true;
                                mess.parentKey = messageKey;
                                mess.localFilePath = photoItem.getImagePath();
                                // Should set those params to make the UI action right cause this is cached message
                                //mess.mediaUrl = photoItem.getImagePath();
                                mess.isMask = params.markStatus;
                                return mess;
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
                .flatMap(message -> uploadThumbnail(conversationId, message.localFilePath)
                        .zipWith(uploadImage(conversationId, message.localFilePath), Pair::create)
                        .flatMap(pair -> messageRepository.updateChildMessageImage(conversationId,
                                parentMessage.key, message.key, pair.first, pair.second)
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

    private Observable<String> uploadThumbnail(String conversationKey, String filePath) {
        if (TextUtils.isEmpty(filePath)) return Observable.just("");
        String fileName = "thumb_" + System.currentTimeMillis() + new File(filePath).getName();
        return storageRepository.uploadFile(conversationKey, fileName, Utils.getImageData(filePath, 128, 128));
    }

    public static class Params {
        public List<PhotoItem> items;
        public Conversation conversation;
        public User currentUser;
        public boolean markStatus;
    }
}
