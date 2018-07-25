package com.ping.android.domain.usecase.message;

import android.text.TextUtils;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.data.mappers.MessageMapper;
import com.ping.android.domain.repository.CommonRepository;
import com.ping.android.domain.repository.ConversationRepository;
import com.ping.android.domain.repository.StorageRepository;
import com.ping.android.domain.repository.UserRepository;
import com.ping.android.model.Conversation;
import com.ping.android.model.Message;
import com.ping.android.data.entity.MessageEntity;
import com.ping.android.model.User;
import com.ping.android.model.enums.GameType;
import com.ping.android.model.enums.MessageType;
import com.ping.android.utils.Utils;
import com.ping.android.utils.configs.Constant;

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

public class SendGameMessageUseCase extends UseCase<Message, SendGameMessageUseCase.Params> {
    @Inject
    UserRepository userRepository;
    @Inject
    StorageRepository storageRepository;
    @Inject
    ConversationRepository conversationRepository;
    @Inject
    CommonRepository commonRepository;
    @Inject
    SendMessageUseCase sendMessageUseCase;
    @Inject
    MessageMapper messageMapper;
    SendMessageUseCase.Params.Builder builder;

    @Inject
    public SendGameMessageUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<Message> buildUseCaseObservable(Params params) {
        builder = new SendMessageUseCase.Params.Builder()
                .setMessageType(MessageType.GAME)
                .setConversation(params.conversation)
                .setMarkStatus(params.markStatus)
                .setCurrentUser(params.currentUser)
                .setGameType(params.gameType);
        builder.setCacheImage(params.filePath);
        builder.setFileUrl("PPhtotoMessageIdentifier");
        MessageEntity cachedMessage = builder.build().getMessage();
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
                            message1.days = (long) (message.timestamp * 1000 / Constant.MILLISECOND_PER_DAY);
                            return message1;
                        }))
                .concatWith(sendMessage(params));
    }

    private Observable<Message> sendMessage(Params params) {
        return this.uploadImage(params.conversation.key, params.filePath)
                .observeOn(Schedulers.io())
                .map(s -> {
                    builder.setFileUrl(s);
                    return builder.build();
                })
                .flatMap(params1 -> {
                    MessageEntity message = params1.getMessage();
                    Map<String, Object> updateValue = new HashMap<>();
                    updateValue.put(String.format("messages/%s/%s/gameUrl", params1.getConversation().key, message.key), message.gameUrl);
                    updateValue.put(String.format("media/%s/%s", params1.getConversation().key, message.key), message.toMap());
                    return commonRepository.updateBatchData(updateValue).map(aBoolean -> messageMapper.transform(message, params.currentUser));
                });
    }

    private Observable<String> uploadImage(String conversationKey, String filePath) {
        if (TextUtils.isEmpty(filePath)) return Observable.just("");
        String fileName = new File(filePath).getName();
        return storageRepository.uploadFile(conversationKey, fileName, Utils.getImageData(filePath, 512, 512));
    }

    public static class Params {
        public String filePath;
        public Conversation conversation;
        public User currentUser;
        public GameType gameType;
        public boolean markStatus;
    }
}
