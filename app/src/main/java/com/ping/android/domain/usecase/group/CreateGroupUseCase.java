package com.ping.android.domain.usecase.group;

import android.text.TextUtils;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.domain.repository.CommonRepository;
import com.ping.android.domain.repository.ConversationRepository;
import com.ping.android.domain.repository.GroupRepository;
import com.ping.android.domain.repository.StorageRepository;
import com.ping.android.domain.repository.UserRepository;
import com.ping.android.domain.usecase.message.SendMessageUseCase;
import com.ping.android.model.Conversation;
import com.ping.android.model.Group;
import com.ping.android.model.User;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 2/10/18.
 */

public class CreateGroupUseCase extends UseCase<String, CreateGroupUseCase.Params> {
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
    private User currentUser;

    @Inject
    public CreateGroupUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<String> buildUseCaseObservable(Params params) {
        return userRepository.getCurrentUser()
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
                            updateValue.put(String.format("groups/%s", group.key), group.toMap());
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
                            updateValue.put(String.format("conversations/%s", conversation.key), conversation.toMap());
                            for (String userKey : conversation.memberIDs.keySet()) {
                                updateValue.put(String.format("conversations/%s/%s", userKey, conversation.key), conversation.toMap());
                            }
                            return commonRepository.updateBatchData(updateValue)
                                    .zipWith(groupRepository.updateGroupConversationId(conversation.groupID, conversation.key),
                                            (aBoolean, aBoolean2) -> conversation);
                        })
                )
                .flatMap(conversation -> {
                    if (TextUtils.isEmpty(params.message)) {
                        return Observable.just(conversation.key);
                    } else {
                        SendMessageUseCase.Params sendMessageParams = new SendMessageUseCase.Params();
                        sendMessageParams.currentUser = currentUser;
                        sendMessageParams.conversation = conversation;
                        sendMessageParams.markStatus = false;
                        sendMessageParams.text = params.message;
                        return sendMessageUseCase.buildUseCaseObservable(sendMessageParams)
                                .map(aBoolean -> conversation.key);
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
