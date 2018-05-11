package com.ping.android.domain.usecase.conversation;

import android.text.TextUtils;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.bzzzchat.rxfirebase.database.ChildEvent;
import com.ping.android.domain.repository.ConversationRepository;
import com.ping.android.domain.repository.MessageRepository;
import com.ping.android.domain.repository.UserRepository;
import com.ping.android.model.Conversation;
import com.ping.android.model.Message;
import com.ping.android.model.User;
import com.ping.android.ultility.CommonMethod;
import com.ping.android.ultility.Constant;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import io.reactivex.Observable;

public class ObserveMediaChangeUseCase extends UseCase<Message, Conversation> {
    @Inject
    UserRepository userRepository;
    @Inject
    MessageRepository messageRepository;

    @Inject
    public ObserveMediaChangeUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<Message> buildUseCaseObservable(Conversation conversation) {
        return userRepository.getCurrentUser()
                .flatMap(user -> messageRepository.observeMediaUpdate(conversation.key)
                        .flatMap(childEvent -> {
                            if (childEvent.type == ChildEvent.Type.CHILD_CHANGED) {
                                Message message = Message.from(childEvent.dataSnapshot);
                                message.isMask = CommonMethod.getBooleanFrom(message.markStatuses, user.key);
                                boolean isDeleted = CommonMethod.getBooleanFrom(message.deleteStatuses, user.key);
                                if (isDeleted || TextUtils.isEmpty(message.senderId)) {
                                    return Observable.empty();
                                }

                                if (message.readAllowed != null && message.readAllowed.size() > 0
                                        && !message.readAllowed.containsKey(user.key))
                                    return Observable.empty();

                                if (message.timestamp < conversation.deleteTimestamp) {
                                    return Observable.empty();
                                }

                                message.sender = getUser(message.senderId, conversation);
                                message.currentUserId = user.key;
                                String nickName = conversation.nickNames.get(message.senderId);
                                if (!TextUtils.isEmpty(nickName)) {
                                    message.senderName = nickName;
                                }
                                int status = CommonMethod.getIntFrom(message.status, user.key);
                                if (message.messageType == Constant.MSG_TYPE_GAME && !TextUtils.equals(message.senderId, user.key)) {
                                    if (status == Constant.MESSAGE_STATUS_GAME_PASS) {
                                        return Observable.just(message);
                                    }
                                }
                                return Observable.just(message);
                            } else {
                                return Observable.empty();
                            }
                        }));
    }

    private User getUser(String userId, Conversation conversation) {
        for (User user : conversation.members) {
            if (userId.equals(user.key)) {
                return user;
            }
        }
        return null;
    }

    class Params {

    }
}
