package com.ping.android.domain.usecase.message;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.bzzzchat.rxfirebase.database.ChildEvent;
import com.ping.android.domain.repository.MessageRepository;
import com.ping.android.domain.repository.UserRepository;
import com.ping.android.model.ChildData;
import com.ping.android.model.Conversation;
import com.ping.android.model.Message;
import com.ping.android.model.User;
import com.ping.android.ultility.CommonMethod;
import com.ping.android.ultility.Constant;

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
                .map(childEvent -> {
                    if (childEvent.dataSnapshot.exists() && childEvent.type == ChildEvent.Type.CHILD_ADDED) {
                        Message message = Message.from(childEvent.dataSnapshot);
                        message.currentUserId = currentUser.key;
                        message.isMask = CommonMethod.getBooleanFrom(message.markStatuses, currentUser.key);
                        return new ChildData<>(message, childEvent.type);
                    } else {
                        return new ChildData<Message>(null, childEvent.type);
                    }
                })
                //.onErrorResumeNext(Observable.empty())
                .flatMap(childData -> {
                    if (childData.type != ChildEvent.Type.CHILD_ADDED) return Observable.empty();
                    Message message = childData.data;
                    boolean isReadable = message.isReadable(currentUser.key);
                    boolean isOldMessage = message.timestamp < getLastDeleteTimeStamp(params.conversation);
                    boolean isDeleted = CommonMethod.getBooleanFrom(message.deleteStatuses, currentUser.key);
                    if (isDeleted || isOldMessage || !isReadable) {
                        return Observable.empty();
                    }
                    /*int status = CommonMethod.getIntFrom(message.status, currentUser.key);
                    updateReadStatus(message, params.conversation, status);*/
                    return userRepository.getUser(childData.data.senderId)
                            .map(user -> {
                                childData.data.sender = user;
                                return childData;
                            });
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
