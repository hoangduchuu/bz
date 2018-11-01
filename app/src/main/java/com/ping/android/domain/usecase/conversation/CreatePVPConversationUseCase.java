package com.ping.android.domain.usecase.conversation;

import android.text.TextUtils;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.domain.repository.CommonRepository;
import com.ping.android.domain.repository.ConversationRepository;
import com.ping.android.domain.repository.UserRepository;
import com.ping.android.domain.usecase.message.SendMessageUseCase;
import com.ping.android.managers.UserManager;
import com.ping.android.model.Conversation;
import com.ping.android.model.User;
import com.ping.android.model.enums.MessageType;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 2/24/18.
 */

public class CreatePVPConversationUseCase extends UseCase<String, CreatePVPConversationUseCase.Params> {
    @Inject
    UserRepository userRepository;
    @Inject
    ConversationRepository conversationRepository;
    @Inject
    CommonRepository commonRepository;
    @Inject
    UserManager userManager;
    @Inject
    SendMessageUseCase sendMessageUseCase;

    @Inject
    public CreatePVPConversationUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<String> buildUseCaseObservable(Params params) {
        return userManager.getCurrentUser()
                .flatMap(user -> {
                    List<User> members = new ArrayList<>();
                    members.add(params.toUser);
                    members.add(user);
                    String conversationID = user.key.compareTo(params.toUser.key) > 0 ? user.key + params.toUser.key : params.toUser.key + user.key;
                    return conversationRepository
                            .getConversation(user.key, conversationID)
                            .onErrorResumeNext(observer -> {
                                Conversation newConversation = Conversation.createNewConversation(user.key, params.toUser.key);
                                newConversation.key = conversationID;
                                newConversation.opponentUser = params.toUser;
                                newConversation.members = members;

                                Map<String, Object> updateValue = new HashMap<>();
                                //updateValue.put(String.format("conversations/%s", key), conversation.toMap());
                                for (String userKey : newConversation.memberIDs.keySet()) {
                                    updateValue.put(String.format("conversations/%s/%s", userKey, conversationID), newConversation.toMap());
                                }
                                return commonRepository.updateBatchData(updateValue)
                                        .map(aBoolean -> newConversation);
                            })
                            .flatMap(conversation -> {
                                conversation.opponentUser = params.toUser;
                                conversation.members = members;
                                // Turn notifications on for this opponentUser
                                Map<String, Object> updateValue = new HashMap<>();
                                //updateValue.put(String.format("conversations/%s/notifications/%s", conversation, userId), value);
                                updateValue.put(String.format("conversations/%s/%s/notifications/%s", user.key, conversationID, user.key), true);
                                return commonRepository.updateBatchData(updateValue)
                                        .map(aBoolean -> conversation);
                            })
                            .flatMap(conversation -> {
                                if (TextUtils.isEmpty(params.message)) {
                                    return Observable.just(conversation.key);
                                } else {
                                    return conversationRepository.getMessageKey(conversation.key)
                                            .map(messageKey -> new SendMessageUseCase.Params.Builder()
                                                    .setMessageType(MessageType.TEXT)
                                                    .setConversation(conversation)
                                                    .setMarkStatus(false)
                                                    .setCurrentUser(user)
                                                    .setText(params.message)
                                                    .setMessageKey(messageKey)
                                                    .build())
                                            .flatMap(params1 -> sendMessageUseCase.buildUseCaseObservable(params1)
                                                    .map(aBoolean -> conversation.key));
                                }
                            });
                });
    }

    public static class Params {
        public User toUser;
        public String message;
    }
}
