package com.ping.android.domain.usecase.conversation;

import android.text.TextUtils;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.data.mappers.MessageMapper;
import com.ping.android.domain.repository.MessageRepository;
import com.ping.android.domain.repository.UserRepository;
import com.ping.android.managers.UserManager;
import com.ping.android.model.Conversation;
import com.ping.android.model.Message;
import com.ping.android.model.User;
import com.ping.android.model.enums.MessageType;
import com.ping.android.utils.CommonMethod;
import com.ping.android.utils.configs.Constant;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import io.reactivex.Observable;

public class ObserveMediaChangeUseCase extends UseCase<Message, Conversation> {
    @Inject
    UserRepository userRepository;
    @Inject
    MessageRepository messageRepository;
    @Inject
    UserManager userManager;
    @Inject
    MessageMapper messageMapper;

    @Inject
    public ObserveMediaChangeUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<Message> buildUseCaseObservable(Conversation conversation) {
        return userManager.getCurrentUser()
                .flatMap(user -> messageRepository.observeMediaUpdate(conversation.key)
                        .flatMap(childData -> {
                            if (childData.getData() == null) return Observable.empty();
                            Message message = messageMapper.transform(childData.getData(), user);
                            boolean isDeleted = CommonMethod.getBooleanFrom(childData.getData().deleteStatuses, user.key);
                            if (isDeleted || TextUtils.isEmpty(message.senderId)) {
                                return Observable.empty();
                            }

                            if (!childData.getData().isReadable(user.key))
                                return Observable.empty();

                            if (message.timestamp < conversation.deleteTimestamp) {
                                return Observable.empty();
                            }

                            User sender = getUser(message.senderId, conversation);
                            if (sender != null) {
                                message.senderProfile = sender.profile;
                                message.senderName = TextUtils.isEmpty(sender.nickName) ? sender.getDisplayName() : sender.nickName;
                            }
                            message.currentUserId = user.key;
                            if (message.type == MessageType.GAME && !TextUtils.equals(message.senderId, user.key)) {
                                if (message.messageStatusCode == Constant.MESSAGE_STATUS_GAME_PASS) {
                                    return Observable.just(message);
                                }
                            }
                            return Observable.just(message);
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
