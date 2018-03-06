package com.ping.android.domain.usecase.message;

import android.text.TextUtils;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.domain.repository.CommonRepository;
import com.ping.android.domain.repository.ConversationRepository;
import com.ping.android.domain.repository.StorageRepository;
import com.ping.android.domain.repository.UserRepository;
import com.ping.android.model.Conversation;
import com.ping.android.model.Message;
import com.ping.android.model.User;
import com.ping.android.model.enums.GameType;
import com.ping.android.model.enums.MessageType;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.Observable;

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
    SendMessageUseCase.Params.Builder builder;

    @Inject
    public SendGameMessageUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<Message> buildUseCaseObservable(Params params) {
        builder = new SendMessageUseCase.Params.Builder()
                .setMessageType(params.messageType)
                .setConversation(params.conversation)
                .setMarkStatus(params.markStatus)
                .setCurrentUser(params.currentUser)
                .setGameType(params.gameType);
        builder.setCacheImage(params.filePath);
        builder.setImageUrl("PPhtotoMessageIdentifier");
        Message cachedMessage = builder.build().getMessage();
        cachedMessage.isCached = true;
        cachedMessage.localImage = params.filePath;
        return conversationRepository.getMessageKey(params.conversation.key)
                .zipWith(Observable.just(cachedMessage), (s, message) -> {
                    message.key = s;
                    builder.setMessageKey(s);
                    return message;
                })
                .flatMap(message -> sendMessageUseCase.buildUseCaseObservable(builder.build())
                        .map(message1 -> {
                            message1.isCached = true;
                            message1.localImage = params.filePath;
                            message1.currentUserId = params.currentUser.key;
                            return message1;
                        }))
                .concatWith(sendMessage(params));
    }

    private Observable<Message> sendMessage(Params params) {
        return this.uploadImage(params.conversation.key, params.filePath)
                .map(s -> {
                    builder.setImageUrl(s);
                    return builder.build();
                })
                .flatMap(params1 -> {
                    Message message = params1.getMessage();
                    Map<String, Object> updateValue = new HashMap<>();
                    updateValue.put(String.format("messages/%s/%s/gameUrl", params1.getConversation().key, message.key), message.gameUrl);
                    return commonRepository.updateBatchData(updateValue).map(aBoolean -> message);
                });
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
        public GameType gameType;
        public boolean markStatus;
    }
}
