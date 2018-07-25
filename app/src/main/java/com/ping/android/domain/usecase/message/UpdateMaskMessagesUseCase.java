package com.ping.android.domain.usecase.message;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.domain.repository.CommonRepository;
import com.ping.android.domain.repository.UserRepository;
import com.ping.android.managers.UserManager;
import com.ping.android.model.Message;
import com.ping.android.model.enums.MessageType;
import com.ping.android.utils.configs.Constant;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 3/13/18.
 */

public class UpdateMaskMessagesUseCase extends UseCase<Boolean, UpdateMaskMessagesUseCase.Params> {
    @Inject
    UserRepository userRepository;
    @Inject
    CommonRepository commonRepository;
    @Inject
    UserManager userManager;

    @Inject
    public UpdateMaskMessagesUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<Boolean> buildUseCaseObservable(Params params) {
        return userManager.getCurrentUser()
                .flatMap(user -> {
                    Map<String, Object> updateValue = new HashMap<>();
                    for (String message : params.messageKeys) {
                        updateValue.put(String.format("messages/%s/%s/markStatuses/%s", params.conversationId,
                                message, user.key), params.isMask);
                    }
                    for (String message : params.mediaMessages) {
                        updateValue.put(String.format("media/%s/%s/markStatuses/%s", params.conversationId, message, user.key),
                                params.isMask);
                    }
                    if (params.isLastMessage) {
                        updateValue.put(String.format("conversations/%s/%s/markStatuses/%s/", user.key,
                                params.conversationId, user.key), params.isMask);
                    }
                    return commonRepository.updateBatchData(updateValue);
                });
    }

    public static class Params {
        public List<String> messageKeys = new ArrayList<>();
        public List<String> mediaMessages = new ArrayList<>();
        public String conversationId;
        public boolean isLastMessage;
        public boolean isMask;

        public void setMessages(List<Message> messages) {
            for (Message message : messages) {
                setMessage(message);
            }
        }

        public void setMessage(Message message) {
            this.messageKeys.add(message.key);
            if (message.type == MessageType.IMAGE
                    || message.type == MessageType.GAME) {
                this.mediaMessages = new ArrayList<>();
                this.mediaMessages.add(message.key);
            }
        }

        /**
         * Set message id. Currently call when update game message
         *
         * @param messageId
         */
        public void setMessageId(String messageId) {
            this.messageKeys.add(messageId);
            this.mediaMessages.add(messageId);
        }
    }
}
