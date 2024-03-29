package com.ping.android.domain.usecase.message;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.data.entity.ChildData;
import com.ping.android.data.entity.MessageEntity;
import com.ping.android.data.mappers.MessageMapper;
import com.ping.android.domain.repository.MessageRepository;
import com.ping.android.domain.repository.UserRepository;
import com.ping.android.managers.UserManager;
import com.ping.android.model.Conversation;
import com.ping.android.model.Message;
import com.ping.android.model.User;
import com.ping.android.utils.CommonMethod;
import com.ping.android.utils.configs.Constant;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 2/26/18.
 */

public class ObserveLastMessageUseCase extends UseCase<ChildData<Message>, ObserveLastMessageUseCase.Params> {
    @Inject
    MessageRepository messageRepository;
    @Inject
    UserRepository userRepository;
    @Inject
    UserManager userManager;
    @Inject
    MessageMapper messageMapper;

    User currentUser;

    @Inject
    public ObserveLastMessageUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<ChildData<Message>> buildUseCaseObservable(Params params) {
        currentUser = params.user;
        return messageRepository.observeLastMessage(params.conversation.key)
                .flatMap(childData -> {
                    if (childData.getType() != ChildData.Type.CHILD_ADDED) return Observable.empty();
                    Message message = messageMapper.transform(childData.getData(), currentUser);
                    ChildData<Message> data = new ChildData<>(message, childData.getType());
                    boolean isReadable = childData.getData().isReadable(currentUser.key);
                    boolean isOldMessage = message.timestamp < getLastDeleteTimeStamp(params.conversation);
                    boolean isDeleted = CommonMethod.getBooleanFrom(childData.getData().deleteStatuses, currentUser.key);
                    if (isDeleted || isOldMessage || !isReadable) {
                        return Observable.empty();
                    }

                    MessageEntity entity = childData.getData();
                    entity.isMask = message.isMask;
                    entity.messageStatusCode = message.messageStatusCode;
                    messageRepository.saveMessage(entity);

                    /*int status = CommonMethod.getIntFrom(message.status, currentUser.key);
                    updateReadStatus(message, params.conversation, status);*/
                    User sender = params.conversation.getUser(message.senderId);
                    if (sender != null) {
                        message.senderProfile = sender.profileImage();
                        message.senderName = sender.nickName.isEmpty() ? sender.getDisplayName() : sender.nickName;
                    }
                    return Observable.just(data);
                });
    }

    /**
     * Update message status to Read
     *
     * @param message
     * @param conversation
     * @param status       message status of current opponentUser. -1 if not exists on message's status
     */
    private void updateReadStatus(Message message, Conversation conversation, int status) {
        if (!message.senderId.equals(currentUser.key)) {
            if (status == Constant.MESSAGE_STATUS_SENT || status == -1) {
                //for (String userId : conversation.memberIDs.keySet()) {
                messageRepository.updateMessageStatus(conversation.key, message.key,
                        currentUser.key, Constant.MESSAGE_STATUS_READ)
                        .subscribe();
                //}
            }
        }
    }

    private Observable<User> getUser(String userId) {
        User user = userManager.getCacheUser(userId);
        if (user != null) {
            return Observable.just(user);
        }
        return userRepository.getUser(userId);
    }

    private Double getLastDeleteTimeStamp(Conversation conversation) {
        if (conversation.deleteTimestamps == null || !conversation.deleteTimestamps.containsKey(currentUser.key)) {
            return 0.0d;
        }
        Object value = conversation.deleteTimestamps.get(currentUser.key);
        if (value instanceof Long) {
            return ((Long) value).doubleValue();
        } else {
            return (Double) value;
        }
    }

    public static class Params {
        public Conversation conversation;
        public User user;

        public Params(Conversation conversation, User currentUser) {
            this.conversation = conversation;
            this.user = currentUser;
        }
    }
}
