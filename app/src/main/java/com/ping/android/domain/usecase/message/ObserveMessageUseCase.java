package com.ping.android.domain.usecase.message;

import android.text.TextUtils;

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

import org.apache.commons.collections4.MapUtils;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 2/26/18.
 */

public class ObserveMessageUseCase extends UseCase<ChildData<Message>, ObserveMessageUseCase.Params> {
    @Inject
    MessageRepository messageRepository;
    @Inject
    UserRepository userRepository;

    User currentUser;

    @Inject
    public ObserveMessageUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<ChildData<Message>> buildUseCaseObservable(Params params) {
        currentUser = params.user;
        return messageRepository.observeMessageUpdate(params.conversation.key)
                .map(childEvent -> {
                    if (childEvent.dataSnapshot.exists()) {
                        Message message = Message.from(childEvent.dataSnapshot);
                        message.currentUserId = currentUser.key;
                        return new ChildData<>(message, childEvent.type);
                    } else {
                        throw new NullPointerException();
                    }
                })
                .onErrorResumeNext(Observable.empty())
                .flatMap(childData -> {
                    Message message = childData.data;
                    boolean unReadable = message.readAllowed != null && message.readAllowed.size() > 0
                            && !message.readAllowed.containsKey(currentUser.key);
                    boolean isOldMessage = message.timestamp < getLastDeleteTimeStamp(params.conversation);
                    if (isOldMessage || unReadable) {
                        return Observable.empty();
                    }

                    boolean isDeleted = CommonMethod.getBooleanFrom(childData.data.deleteStatuses, currentUser.key);
                    if (isDeleted) {
                        if (childData.type == ChildEvent.Type.CHILD_CHANGED) {
                            childData.type = ChildEvent.Type.CHILD_REMOVED;
                            return Observable.just(childData);
                        } else {
                            return Observable.empty();
                        }
                    } else {
                        if (childData.type == ChildEvent.Type.CHILD_CHANGED) {
                            if (message.messageType == Constant.MSG_TYPE_GAME) {
                                // Update status of game if not update
                                int status = CommonMethod.getIntFrom(message.status, currentUser.key);
                                if (!TextUtils.isEmpty(message.gameUrl)
                                        && !message.gameUrl.equals("PPhtotoMessageIdentifier")
                                        && status == Constant.MESSAGE_STATUS_ERROR) {
                                    messageRepository.updateMessageStatus(params.conversation.key,
                                            message.key, currentUser.key, Constant.MESSAGE_STATUS_DELIVERED)
                                            .subscribe();
                                }
                            }
                        }
                        return userRepository.getUser(childData.data.senderId)
                                .map(user -> {
                                    childData.data.sender = user;
                                    return childData;
                                });
                    }
                });
    }

    private Double getLastDeleteTimeStamp(Conversation conversation) {
        if (MapUtils.isEmpty(conversation.deleteTimestamps) || !conversation.deleteTimestamps.containsKey(currentUser.key)) {
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
