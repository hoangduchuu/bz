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

public class LoadMoreMessagesUseCase extends UseCase<LoadMoreMessagesUseCase.Output, LoadMoreMessagesUseCase.Params> {
    @Inject
    MessageRepository messageRepository;
    @Inject
    UserRepository userRepository;

    @Inject
    public LoadMoreMessagesUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<Output> buildUseCaseObservable(Params params) {
        return userRepository.getCurrentUser()
                .flatMap(user -> messageRepository.loadMoreMessages(params.conversation.key, params.endTimestamp)
                        .map(dataSnapshot -> {
                            if (dataSnapshot.getChildrenCount() > 0) {
                                List<Message> messages = new ArrayList<>();
                                for (DataSnapshot child : dataSnapshot.getChildren()) {
                                    Message message = Message.from(child);
                                    boolean isDeleted = CommonMethod.getBooleanFrom(message.deleteStatuses, user.key);
                                    if (isDeleted) {
                                        continue;
                                    }

                                    if (message.readAllowed != null && message.readAllowed.size() > 0
                                            && !message.readAllowed.containsKey(user.key))
                                        continue;

                                    if (message.timestamp < getLastDeleteTimeStamp(params.conversation, user)) {
                                        continue;
                                    }

                                    message.sender = getUser(message.senderId, params.conversation);
                                    message.currentUserId = user.key;
                                    messages.add(message);
                                }
                                Output output = new Output();
                                output.messages = messages;
                                output.canLoadMore = dataSnapshot.getChildrenCount() >= Constant.LOAD_MORE_MESSAGE_AMOUNT;
                                return output;
                            }
                            throw new NullPointerException();
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

    public static class Params {
        public Conversation conversation;
        public double endTimestamp;

        public Params(Conversation conversation, double oldestTimestamp) {
            this.conversation = conversation;
            this.endTimestamp = oldestTimestamp;
        }
    }

    public static class Output {
        public List<Message> messages;
        public boolean canLoadMore;
    }
}
