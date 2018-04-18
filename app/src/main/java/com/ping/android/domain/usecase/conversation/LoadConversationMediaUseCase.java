package com.ping.android.domain.usecase.conversation;

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

public class LoadConversationMediaUseCase extends UseCase<LoadConversationMediaUseCase.Output, LoadConversationMediaUseCase.Params> {
    @Inject
    MessageRepository messageRepository;
    @Inject
    UserRepository userRepository;

    @Inject
    public LoadConversationMediaUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<Output> buildUseCaseObservable(Params params) {
        return userRepository.getCurrentUser()
        .flatMap(user -> messageRepository.loadConversationMedia(params.conversation.key, params.lastTimestamp)
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
                        LoadConversationMediaUseCase.Output output = new LoadConversationMediaUseCase.Output();
                        output.messages = messages;
                        output.canLoadMore = dataSnapshot.getChildrenCount() >= Constant.LOAD_MORE_MESSAGE_AMOUNT;
                        return output;
                    }
                    throw new NullPointerException("");
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
        private Conversation conversation;
        private Double lastTimestamp;

        public Params(Conversation conversation, Double lastTimestamp) {
            this.conversation = conversation;
            this.lastTimestamp = lastTimestamp;
        }
    }

    public static class Output {
        public List<Message> messages;
        public boolean canLoadMore;
        public double lastTimestamp;
    }
}
