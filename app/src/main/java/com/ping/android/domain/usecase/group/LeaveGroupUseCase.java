package com.ping.android.domain.usecase.group;

import com.bzzzchat.cleanarchitecture.DefaultObserver;
import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.data.mappers.UserMapper;
import com.ping.android.domain.repository.CommonRepository;
import com.ping.android.domain.repository.UserRepository;
import com.ping.android.domain.usecase.message.SendMessageUseCase;
import com.ping.android.domain.usecase.message.SendTextMessageUseCase;
import com.ping.android.managers.UserManager;
import com.ping.android.model.Conversation;
import com.ping.android.model.Group;
import com.ping.android.model.Message;
import com.ping.android.model.User;
import com.ping.android.model.enums.MessageType;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 2/1/18.
 */

/**
 * Send left message status before leave group.
 */
public class LeaveGroupUseCase extends UseCase<Boolean, Conversation> {
    @Inject
    CommonRepository commonRepository;
    @Inject
    UserRepository userRepository;
    @Inject
    UserManager userManager;

    @Inject
    SendTextMessageUseCase sendTextMessageUseCase;

    @Inject
    UserMapper userMapper;

    @Inject
    public LeaveGroupUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<Boolean> buildUseCaseObservable(Conversation conversation) {
        return userManager.getCurrentUser()
                .flatMap(user -> {
                    String userId = user.key;
                    Map<String, Object> updateValue = new HashMap<>();
                    conversation.group.memberIDs.remove(userId);
                    conversation.group.deleteStatuses.put(userId, true);

                    // 1. Remove group and conversation for current opponentUser
                    updateValue.put(String.format("groups/%s/%s", userId, conversation.group.key), null);
                    updateValue.put(String.format("conversations/%s/%s", userId, conversation.group.conversationID), null);

                    // 2. Update members for group & conversation
//        updateValue.put(String.format("groups/%s/deleteStatuses", group.key), group.deleteStatuses);
//        updateValue.put(String.format("conversations/%s/deleteStatuses", group.conversationID), group.deleteStatuses);
//        updateValue.put(String.format("groups/%s/memberIDs", group.key), group.memberIDs);
//        updateValue.put(String.format("conversations/%s/memberIDs", group.conversationID), group.memberIDs);
                    for (String id : conversation.group.memberIDs.keySet()) {
                        if (id.equals(userId)) continue;
                        updateValue.put(String.format("groups/%s/%s/deleteStatuses", id, conversation.group.key), conversation.group.deleteStatuses);
                        updateValue.put(String.format("conversations/%s/%s/deleteStatuses", id, conversation.group.conversationID),conversation.group.deleteStatuses);
                        updateValue.put(String.format("groups/%s/%s/memberIDs", id, conversation.group.key), conversation.group.memberIDs);
                        updateValue.put(String.format("conversations/%s/%s/memberIDs", id, conversation.group.conversationID), conversation.group.memberIDs);
                        updateValue.put(String.format("conversations/%s/%s/notifications/%s",id,conversation.key,id),null);
                    }



                    String mesessage = userMapper.getUserDisPlay(user,conversation) + " has left";
                    SendMessageUseCase.Params params = new SendMessageUseCase.Params.Builder()
                            .setMessageType(MessageType.SYSTEM)
                            .setConversation(conversation)
                            .setCurrentUser(user)
                            .setText(mesessage)
                            .setMarkStatus(false)
                            .build();

                  return  sendTextMessageUseCase.buildUseCaseObservable(params)
                          .flatMap(s-> commonRepository.updateBatchData(updateValue));
                  });
    }


}
