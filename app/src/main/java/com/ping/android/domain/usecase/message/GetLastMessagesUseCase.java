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
import com.ping.android.utils.CommonMethod;
import com.ping.android.utils.configs.Constant;

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
                                double lastTimestamp = Double.MAX_VALUE;
                                List<Message> messages = new ArrayList<>();
                                for (DataSnapshot child : dataSnapshot.getChildren()) {
                                    if (!child.exists()) continue;
                                    Message message = Message.from(child);
                                    if (lastTimestamp > message.timestamp) {
                                        lastTimestamp = message.timestamp;
                                    }
                                    boolean isDeleted = CommonMethod.getBooleanFrom(message.deleteStatuses, user.key);
                                    boolean isOld = message.timestamp < conversation.deleteTimestamp;
                                    boolean isReadable = message.isReadable(user.key);
                                    if (isDeleted || isOld || !isReadable) {
                                        continue;
                                    }

                                    if (message.readAllowed != null
                                            && (message.readAllowed.containsKey(user.key) && !message.readAllowed.get(user.key)))
                                        continue;

                                    message.isMask = CommonMethod.getBooleanFrom(message.markStatuses, user.key);
                                    message.sender = getUser(message.senderId, conversation);
                                    message.currentUserId = user.key;
                                    messages.add(message);
                                }

                                output.messages = messages;
                                output.canLoadMore = dataSnapshot.getChildrenCount() >= Constant.LATEST_RECENT_MESSAGES
                                        && lastTimestamp > conversation.deleteTimestamp;
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

    public static class Output {
        public List<Message> messages;
        public boolean canLoadMore;
    }
}
