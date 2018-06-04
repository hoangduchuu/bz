package com.ping.android.domain.usecase.conversation;

import android.text.TextUtils;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.google.firebase.database.DataSnapshot;
import com.ping.android.domain.repository.MessageRepository;
import com.ping.android.domain.repository.UserRepository;
import com.ping.android.managers.UserManager;
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

public class LoadConversationMediaUseCase extends UseCase<LoadConversationMediaUseCase.Output, LoadConversationMediaUseCase.Params> {
    @Inject
    MessageRepository messageRepository;
    @Inject
    UserRepository userRepository;
    @Inject
    UserManager userManager;

    @Inject
    public LoadConversationMediaUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<Output> buildUseCaseObservable(Params params) {
        if (params.conversation.deleteTimestamp > params.lastTimestamp) {
            LoadConversationMediaUseCase.Output output = new LoadConversationMediaUseCase.Output();
            output.messages = new ArrayList<>();
            output.canLoadMore = false;
            return Observable.just(output);
        }
        return userManager.getCurrentUser()
                .flatMap(user -> messageRepository.loadConversationMedia(params.conversation.key, params.lastTimestamp)
                        .map(dataSnapshot -> {
                            if (dataSnapshot.getChildrenCount() > 0) {
                                List<Message> messages = new ArrayList<>();
                                for (DataSnapshot child : dataSnapshot.getChildren()) {
                                    Message message = Message.from(child);
                                    message.isMask = CommonMethod.getBooleanFrom(message.markStatuses, user.key);
                                    boolean isDeleted = CommonMethod.getBooleanFrom(message.deleteStatuses, user.key);
                                    if (isDeleted || TextUtils.isEmpty(message.senderId)) {
                                        continue;
                                    }

                                    if (message.readAllowed != null && message.readAllowed.size() > 0
                                            && !message.readAllowed.containsKey(user.key))
                                        continue;

                                    if (message.timestamp < params.conversation.deleteTimestamp) {
                                        continue;
                                    }

                                    message.sender = getUser(message.senderId, params.conversation);
                                    message.currentUserId = user.key;
                                    String nickName = params.conversation.nickNames.get(message.senderId);
                                    if (!TextUtils.isEmpty(nickName)) {
                                        message.senderName = nickName;
                                    }
                                    int status = CommonMethod.getIntFrom(message.status, user.key);
                                    if (message.messageType == Constant.MSG_TYPE_GAME && !TextUtils.equals(message.senderId, user.key)) {
                                        if (status == Constant.MESSAGE_STATUS_GAME_PASS) {
                                            messages.add(message);
                                        }
                                    } else {
                                        messages.add(message);
                                    }
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
