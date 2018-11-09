package com.ping.android.domain.usecase.message;

import android.content.Context;
import android.graphics.Bitmap;
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
import com.ping.android.utils.UiUtils;
import com.ping.android.utils.Utils;

import org.jetbrains.annotations.NotNull;

import java.io.File;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by tuanluong on 2/28/18.
 */

public class SendVideoMessageUseCase extends UseCase<Message, SendVideoMessageUseCase.Params> {
    @Inject
    UserRepository userRepository;
    @Inject
    StorageRepository storageRepository;
    @Inject
    ConversationRepository conversationRepository;
    @Inject
    MessageRepository messageRepository;
    @Inject
    MessageMapper messageMapper;
    @Inject
    SendMessageUseCase sendMessageUseCase;
    @Inject
    Context context;

    @Inject
    public SendVideoMessageUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<Message> buildUseCaseObservable(Params params) {
        return conversationRepository.getMessageKey(params.conversation.key)
                .flatMap(s -> {
                    SendMessageUseCase.Params.Builder builder = new SendMessageUseCase.Params.Builder()
                            .setMessageType(params.messageType)
                            .setConversation(params.conversation)
                            .setCurrentUser(params.currentUser)
                            .setFileUrl(params.filePath)
                            .setMessageKey(s);
                    MessageEntity cachedMessage = builder.build().getMessage();
                    Message temp = messageMapper.transform(cachedMessage, params.currentUser);
                    temp.isCached = true;
                    temp.localFilePath = params.filePath;
                    temp.currentUserId = params.currentUser.key;
                    return Observable.just(temp)
                            .concatWith(sendMessageUseCase.buildUseCaseObservable(builder.build())
                                    .flatMap(message -> sendMessage(params, message)
                                            .map(s1 -> message)));
                });

    }

    private Observable<String> sendMessage(Params params, Message message) {
        return uploadThumbnail(params.conversation.key, params.filePath)
                .flatMap(thumb -> messageRepository.updateThumbnailImage(params.conversation.key, message.key, thumb))
                .flatMap(s->this.uploadFile(params.conversation.key, params.filePath)
                        .flatMap(filePath -> messageRepository.updateVideoUrl(params.conversation.key, message.key, filePath)));
    }

    private Observable<String> uploadThumbnail(String conversationKey, String videoPath) {
        String fileName = new File(videoPath).getName() + ".jpg";
        Bitmap thumbnail = UiUtils.retrieveVideoFrameFromVideo(context, videoPath);
        return storageRepository.uploadFile(conversationKey, fileName, Utils.getImageData(thumbnail, 128, 128));
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
    }
}
