package com.ping.android.domain.usecase.message;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.domain.repository.CommonRepository;
import com.ping.android.domain.repository.ConversationRepository;
import com.ping.android.domain.repository.UserRepository;
import com.ping.android.model.Conversation;
import com.ping.android.model.Message;
import com.ping.android.model.User;
import com.ping.android.service.NotificationHelper;
import com.ping.android.ultility.Constant;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 2/8/18.
 */

public class SendMessageUseCase extends UseCase<Boolean, SendMessageUseCase.Params> {
    @Inject
    ConversationRepository conversationRepository;
    @Inject
    CommonRepository commonRepository;
    @Inject
    UserRepository userRepository;

    @Inject
    public SendMessageUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<Boolean> buildUseCaseObservable(Params params) {
        return userRepository.getCurrentUser()
                .flatMap(user -> conversationRepository.getMessageKey(params.conversation.key)
                        .flatMap(key -> {
                            Message message = params.buildMessage(user);
                            message.key = key;
                            return conversationRepository.sendMessage(params.conversation.key, message);
                        }))
                .flatMap(message -> {
                    Conversation conversation = params.updateConversationFollowMessage(message);
                    Map<String, Object> updateData = new HashMap<>();
                    updateData.put(String.format("conversations/%s", conversation.key), conversation.toMap());
                    // Update message for conversation for each user
                    for (String toUser : conversation.memberIDs.keySet()) {
                        if (!message.readAllowed.containsKey(toUser)) continue;
                        updateData.put(String.format("conversations/%s/%s", toUser, conversation.key), conversation.toMap());
                    }
                    return commonRepository.updateBatchData(updateData);
                });
    }

    public static class Params {
        public Conversation conversation;
        public boolean markStatus;
        public User currentUser;
        public String text;

        public Message buildMessage(User currentUser) {
            this.currentUser = currentUser;
            double timestamp = System.currentTimeMillis() / 1000d;
            Map<String, Boolean> allowance = getAllowance();
            Message message = Message.createTextMessage(text, currentUser.key, currentUser.getDisplayName(),
                    timestamp, getStatuses(), getMessageMarkStatuses(), getMessageDeleteStatuses(), allowance);
            return message;
        }

        public Conversation updateConversationFollowMessage(Message message) {
            Conversation newConversation = new Conversation(conversation.conversationType, message.messageType,
                    text, conversation.groupID, currentUser.key, getMemberIDs(), getMessageMarkStatuses(),
                    getMessageReadStatuses(), message.timestamp, conversation);
            return newConversation;
        }

        private Map<String, Boolean> getMemberIDs() {
            Map<String, Boolean> memberIDs = new HashMap<>();
            for (String toUser : conversation.memberIDs.keySet()) {
                memberIDs.put(toUser, true);
            }
            return memberIDs;
        }

        private Map<String, Boolean> getMessageReadStatuses() {
            Map<String, Boolean> markStatuses = new HashMap<>();
            for (String toUser : conversation.memberIDs.keySet()) {
                markStatuses.put(toUser, false);
            }
            markStatuses.put(currentUser.key, true);
            return markStatuses;
        }

        private Map<String, Boolean> getAllowance() {
            Map<String, Boolean> ret = new HashMap<>();
            ret.put(currentUser.key, true);
            if (conversation.conversationType == Constant.CONVERSATION_TYPE_INDIVIDUAL) {
                // Check whether sender is in block list of receiver
                if (!currentUser.blockBys.containsKey(conversation.opponentUser.key)) {
                    ret.put(conversation.opponentUser.key, true);
                }
            } else {
                for (String toUser : conversation.memberIDs.keySet()) {
                    if (toUser.equals(currentUser.key)
                            || currentUser.blocks.containsKey(toUser)
                            || currentUser.blockBys.containsKey(toUser)) continue;
                    ret.put(toUser, true);
                }
            }
            return ret;
        }

        private Map<String, Integer> getStatuses() {
            Map<String, Integer> deleteStatuses = new HashMap<>();
            for (String toUser : conversation.memberIDs.keySet()) {
                deleteStatuses.put(toUser, Constant.MESSAGE_STATUS_SENT);
            }
            deleteStatuses.put(currentUser.key, Constant.MESSAGE_STATUS_SENT);
            return deleteStatuses;
        }

        private Map<String, Boolean> getMessageMarkStatuses() {
            Map<String, Boolean> markStatuses = new HashMap<>();
            if (conversation.maskMessages != null) {
                markStatuses.putAll(conversation.maskMessages);
            }
            markStatuses.put(currentUser.key, markStatus);
            return markStatuses;
        }

        private Map<String, Boolean> getMessageDeleteStatuses() {
            Map<String, Boolean> deleteStatuses = new HashMap<>();
            for (String toUser : conversation.memberIDs.keySet()) {
                deleteStatuses.put(toUser, false);
            }
            deleteStatuses.put(currentUser.key, false);
            return deleteStatuses;
        }
    }
}
