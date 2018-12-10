package com.ping.android.domain.usecase.group;

import android.text.TextUtils;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.data.mappers.ConversationMapper;
import com.ping.android.domain.repository.CommonRepository;
import com.ping.android.domain.repository.ConversationRepository;
import com.ping.android.domain.repository.GroupRepository;
import com.ping.android.domain.repository.StorageRepository;
import com.ping.android.domain.repository.UserRepository;
import com.ping.android.domain.usecase.message.SendMessageUseCase;
import com.ping.android.managers.UserManager;
import com.ping.android.model.Conversation;
import com.ping.android.model.Group;
import com.ping.android.model.User;
import com.ping.android.model.enums.MessageType;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 2/10/18.
 */

public class CreateGroupUseCase extends UseCase<Conversation, CreateGroupUseCase.Params> {
    @Inject
    UserRepository userRepository;
    @Inject
    GroupRepository groupRepository;
    @Inject
    ConversationRepository conversationRepository;
    @Inject
    CommonRepository commonRepository;
    @Inject
    StorageRepository storageRepository;
    @Inject
    SendMessageUseCase sendMessageUseCase;
    @Inject
    UserManager userManager;
    private User currentUser;

    @Inject
    ConversationMapper mapper;

    @Inject
    public CreateGroupUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<Conversation> buildUseCaseObservable(Params params) {
        return userManager.getCurrentUser()
                .zipWith(groupRepository.getKey(), (user, s) -> {
                    currentUser = user;
                    params.users.add(user);
                    double timestamp = System.currentTimeMillis() / 1000d;
                    Group group = new Group();
                    group.timestamp = timestamp;
                    group.groupName = params.groupName;
                    group.groupAvatar = params.groupProfileImage;

                    for (User u : params.users) {
                        group.memberIDs.put(u.key, true);
                    }
                    group.key = s;
                    return group;
                })
                .flatMap(group -> storageRepository.uploadGroupProfileImage(group.key, params.groupProfileImage)
                        .flatMap(s -> {
                            group.groupAvatar = s;
                            Map<String, Object> updateValue = new HashMap<>();
                            //updateValue.put(String.format("groups/%s", group.key), group.toMap());
                            for (String userId : group.memberIDs.keySet()) {
                                updateValue.put(String.format("groups/%s/%s", userId, group.key), group.toMap());
                            }
                            return commonRepository.updateBatchData(updateValue)
                                    .zipWith(conversationRepository.getKey(), (aBoolean, conversationId) -> {
                                        Conversation conversation = Conversation.createNewGroupConversation(currentUser.key, group);
                                        conversation.key = conversationId;
                                        return conversation;
                                    });
                        })
                        .flatMap(conversation -> {
                            Map<String, Object> updateValue = new HashMap<>();
                            //updateValue.put(String.format("conversations/%s", conversation.key), conversation.toMap());
                            for (String userKey : conversation.memberIDs.keySet()) {
                                updateValue.put(String.format("conversations/%s/%s", userKey, conversation.key), conversation.toMap());
                                updateValue.put(String.format("groups/%s/%s/conversationID", userKey, conversation.groupID), conversation.key);
                            }
                            return commonRepository.updateBatchData(updateValue)
                                    .map(aBoolean -> conversation);
                                    //.zipWith(groupRepository.updateGroupConversationId(conversation.groupID, conversation.key),
                                    //        (aBoolean, aBoolean2) -> conversation);
                        })
                )
                .flatMap(conversation -> {
                    if (TextUtils.isEmpty(params.message)) {
                        return Observable.just(conversation);
                    } else {
                        return conversationRepository.getMessageKey(conversation.key)
                                .map(messageKey -> new SendMessageUseCase.Params.Builder()
                                        .setMessageType(MessageType.TEXT)
                                        .setConversation(conversation)
                                        .setMarkStatus(false)
                                        .setCurrentUser(currentUser)
                                        .setText(params.message)
                                        .setMessageKey(messageKey)
                                        .build())
                                .flatMap(params1 -> sendMessageUseCase.buildUseCaseObservable(params1)
                                    .map(message -> mapper.combineMessageParamToConversation(message,conversation)));
                    }
                });

    }

    public static class Params {
        public List<User> users;
        public String groupProfileImage;
        public String message;
        public String groupName;
    }
}
