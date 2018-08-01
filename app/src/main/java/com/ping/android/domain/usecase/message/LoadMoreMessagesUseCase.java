package com.ping.android.domain.usecase.message;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.data.mappers.MessageMapper;
import com.ping.android.domain.repository.MessageRepository;
import com.ping.android.domain.repository.UserRepository;
import com.ping.android.managers.UserManager;
import com.ping.android.model.Conversation;
import com.ping.android.model.Message;
import com.ping.android.data.entity.MessageEntity;
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

public class LoadMoreMessagesUseCase extends UseCase<LoadMoreMessagesUseCase.Output, LoadMoreMessagesUseCase.Params> {
    @Inject
    MessageRepository messageRepository;
    @Inject
    UserRepository userRepository;
    @Inject
    UserManager userManager;
    @Inject
    MessageMapper messageMapper;

    @Inject
    public LoadMoreMessagesUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<Output> buildUseCaseObservable(Params params) {
        if (params.endTimestamp < params.conversation.deleteTimestamp) {
            Output output = new Output();
            output.messages = new ArrayList<>();
            output.canLoadMore = false;
            return Observable.just(output);
        }
        return userManager.getCurrentUser()
                .flatMap(user -> messageRepository.loadMoreMessages(params.conversation.key, params.endTimestamp)
                        .map(entities -> {
                            if (entities.size() > 0) {
                                double lastTimestamp = Double.MAX_VALUE;
                                List<Message> messages = new ArrayList<>();
                                for (MessageEntity entity : entities) {
                                    Message message = messageMapper.transform(entity, user);
                                    if (lastTimestamp > message.timestamp) {
                                        lastTimestamp = message.timestamp;
                                    }
                                    boolean isDeleted = CommonMethod.getBooleanFrom(entity.deleteStatuses, user.key);
                                    if (isDeleted) {
                                        continue;
                                    }

                                    if (!entity.isReadable(user.key))
                                        continue;

                                    if (message.timestamp < params.conversation.deleteTimestamp) {
                                        continue;
                                    }
                                    messages.add(message);
                                }
                                Output output = new Output();
                                output.messages = messages;
                                output.canLoadMore = entities.size() >= Constant.LOAD_MORE_MESSAGE_AMOUNT
                                        && lastTimestamp > params.conversation.deleteTimestamp;
                                return output;
                            } else {
                                Output output = new Output();
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
