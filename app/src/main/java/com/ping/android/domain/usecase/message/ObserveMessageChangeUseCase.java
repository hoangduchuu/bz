package com.ping.android.domain.usecase.message;

import android.text.TextUtils;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.bzzzchat.rxfirebase.database.ChildEvent;
import com.ping.android.domain.repository.MessageRepository;
import com.ping.android.domain.repository.UserRepository;
import com.ping.android.data.entity.ChildData;
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

public class ObserveMessageChangeUseCase extends UseCase<ChildData<Message>, ObserveMessageChangeUseCase.Params> {
    @Inject
    MessageRepository messageRepository;
    @Inject
    UserRepository userRepository;
    @Inject
    UserManager userManager;

    User currentUser;

    @Inject
    public ObserveMessageChangeUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
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
                        message.isMask = CommonMethod.getBooleanFrom(message.markStatuses, currentUser.key);
                        return new ChildData<>(message, childEvent.type);
                    } else {
                        throw new NullPointerException();
                    }
                })
                .flatMap(childData -> {
                    if (childData.getType() != ChildData.Type.CHILD_CHANGED) {
                        return Observable.empty();
                    }
                    Message message = childData.getData();
                    boolean isReadable = message.isReadable(currentUser.key);
                    boolean isOldMessage = message.timestamp < getLastDeleteTimeStamp(params.conversation);
                    if (isOldMessage || !isReadable) {
                        return Observable.empty();
                    }
                    boolean isDeleted = CommonMethod.getBooleanFrom(childData.getData().deleteStatuses, currentUser.key);
                    if (isDeleted) {
                        if (childData.getType() == ChildData.Type.CHILD_CHANGED) {
                            childData.setType(ChildData.Type.CHILD_REMOVED);
                            return Observable.just(childData);
                        } else {
                            return Observable.empty();
                        }
                    } else {
                        int status = CommonMethod.getIntFrom(message.status, currentUser.key);
                        if (childData.getType() == ChildData.Type.CHILD_CHANGED) {
                            if (message.messageType == Constant.MSG_TYPE_GAME) {
                                // Update status of game if not update
                                if (!TextUtils.isEmpty(message.gameUrl)
                                        && !message.gameUrl.equals("PPhtotoMessageIdentifier")
                                        && status == Constant.MESSAGE_STATUS_ERROR) {
                                    messageRepository.updateMessageStatus(params.conversation.key,
                                            message.key, currentUser.key, Constant.MESSAGE_STATUS_DELIVERED)
                                            .subscribe();
                                }
                            }
                            //updateReadStatus(message, params.conversation, status);
                        } else if (childData.getType() == ChildData.Type.CHILD_ADDED) {
                            //updateReadStatus(message, params.conversation, status);
                        }
                        return getUser(childData.getData().senderId)
                                .map(user -> {
                                    childData.getData().sender = user;
                                    return childData;
                                });
                    }
                })
                .onErrorResumeNext(Observable.empty());
    }

    /**
     * Update message status to Read
     * @param message
     * @param conversation
     * @param status message status of current opponentUser. -1 if not exists on message's status
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