package com.ping.android.domain.usecase.message;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.google.firebase.database.DataSnapshot;
import com.ping.android.domain.repository.MessageRepository;
import com.ping.android.domain.repository.UserRepository;
import com.ping.android.model.Conversation;
import com.ping.android.model.Message;
import com.ping.android.model.User;
import com.ping.android.ultility.CommonMethod;
import com.ping.android.ultility.Constant;

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
    UserRepository userRepository;

    @Inject
    public GetLastMessagesUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<Output> buildUseCaseObservable(Conversation conversation) {
        return userRepository.getCurrentUser()
                .flatMap(user -> messageRepository.getLastMessages(conversation.key)
                        .map(dataSnapshot -> {
                            Output output = new Output();
                            if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                                List<Message> messages = new ArrayList<>();
                                for (DataSnapshot child : dataSnapshot.getChildren()) {
                                    if (!child.exists()) continue;
                                    Message message = Message.from(child);
                                    boolean isDeleted = CommonMethod.getBooleanFrom(message.deleteStatuses, user.key);
                                    if (isDeleted) {
                                        continue;
                                    }

                                    if (message.readAllowed != null && message.readAllowed.size() > 0
                                            && !message.readAllowed.containsKey(user.key))
                                        continue;

                                    if (message.timestamp < getLastDeleteTimeStamp(conversation, user)) {
                                        continue;
                                    }

                                    message.sender = getUser(message.senderId, conversation);
                                    message.currentUserId = user.key;
                                    messages.add(message);
                                }

                                output.messages = messages;
                                output.canLoadMore = messages.size() >= Constant.LATEST_RECENT_MESSAGES;
                                return output;
                            } else {
                                output.messages = new ArrayList<>();
                                output.canLoadMore = false;
                                return output;
                            }
                        })
                );
    }

    private User getUser(String userId, Conversation conversation) {
        for (User user : conversation.members) {
            if (userId.equals(user.key)) {
                return user;
            }
        }
        return null;
    }

    public Double getLastDeleteTimeStamp(Conversation conversation, User user) {
        if (conversation.deleteTimestamps == null || !conversation.deleteTimestamps.containsKey(user.key)) {
            return 0.0d;
        }
        Object value = conversation.deleteTimestamps.get(user.key);
        if (value instanceof Long) {
            return ((Long) value).doubleValue();
        } else {
            return (Double) value;
        }
    }

    public static class Output {
        public List<Message> messages;
        public boolean canLoadMore;
    }
}
