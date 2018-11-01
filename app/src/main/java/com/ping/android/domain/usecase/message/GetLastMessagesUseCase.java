package com.ping.android.domain.usecase.message;

import android.text.TextUtils;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.data.entity.MessageEntity;
import com.ping.android.data.mappers.MessageMapper;
import com.ping.android.domain.repository.MessageRepository;
import com.ping.android.managers.UserManager;
import com.ping.android.model.Conversation;
import com.ping.android.model.Message;
import com.ping.android.model.User;
import com.ping.android.utils.CommonMethod;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 2/27/18.
 */

public class GetLastMessagesUseCase extends UseCase<GetLastMessagesUseCase.Output, Conversation> {
    @Inject
    MessageRepository messageRepository;
    @Inject
    UserManager userManager;
    @Inject
    MessageMapper messageMapper;

    @Inject
    public GetLastMessagesUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<Output> buildUseCaseObservable(Conversation conversation) {
        return userManager.getCurrentUser()
                .flatMap(user -> messageRepository.getCachedMessages(conversation.key)
                        .map(messageEntities -> {
                            Output output = new Output();
                            List<Message> messages = new ArrayList<>();
                            for (MessageEntity entity : messageEntities) {
                                Message message = messageMapper.transform(entity, user);
                                message.messageStatusCode = entity.messageStatusCode;
                                message.isMask = entity.isMask;
                                User sender = getUser(message.senderId, conversation);
                                if (sender != null) {
                                    message.senderProfile = sender.profileImage();
                                    message.senderName = TextUtils.isEmpty(sender.nickName) ? sender.getDisplayName() : sender.nickName;
                                }
                                messages.add(message);
                            }
                            output.messages = messages;
                            output.isCached = true;
                            output.canLoadMore = true;
                            return output;
                        }).concatWith(messageRepository.getLastMessages(conversation.key)
                                .map(entities -> {
                                    Output output = new Output();
                                    List<Message> messages = new ArrayList<>();
                                    double lastTimestamp = Double.MAX_VALUE;
                                    List<MessageEntity> availableEntities = new ArrayList<>();
                                    for (MessageEntity entity : entities) {
                                        Message message = messageMapper.transform(entity, user);
                                        if (lastTimestamp > message.timestamp) {
                                            lastTimestamp = message.timestamp;
                                        }
                                        boolean isDeleted = CommonMethod.getBooleanFrom(entity.deleteStatuses, user.key);
                                        boolean isOld = message.timestamp < conversation.deleteTimestamp;
                                        boolean isReadable = entity.isReadable(user.key);
                                        if (isDeleted || isOld || !isReadable) {
                                            continue;
                                        }
                                        User sender = getUser(message.senderId, conversation);
                                        if (sender != null) {
                                            message.senderProfile = sender.profileImage();
                                            message.senderName = TextUtils.isEmpty(sender.nickName) ? sender.getDisplayName() : sender.nickName;
                                        }
                                        messages.add(message);

                                        entity.isMask = message.isMask;
                                        entity.messageStatusCode = message.messageStatusCode;
                                        availableEntities.add(entity);
                                    }
                                    messageRepository.deleteCacheMessages(conversation.key);
                                    messageRepository.saveMessages(availableEntities);
                                    output.messages = messages;
                                    output.isCached = false;
                                    output.canLoadMore = messages.size() > 0;
                                    return output;
                                })
                        ));
    }

    private User getUser(String userId, Conversation conversation) {
        for (User user : conversation.members) {
            if (userId.equals(user.key)) {
                return user;
            }
        }
        return null;
    }

    public static class Output {
        public List<Message> messages;
        public boolean canLoadMore;
        public boolean isCached;
    }
}
